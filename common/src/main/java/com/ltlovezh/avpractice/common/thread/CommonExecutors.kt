package com.ltlovezh.avpractice.common.thread

import com.ltlovezh.avpractice.common.Logger
import java.util.concurrent.*

class CommonExecutors {
    companion object {
        private const val TAG = "BachExecutors";
        private val DBExecutor = DBTreadPoolExecutor()

        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
        // We want at least 2 threads and at most 4 threads in the core pool,
        // preferring to have 1 less than the CPU count to avoid saturating
        // the CPU with background work
        private val CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4))
        private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
        private val KEEP_ALIVE_SECONDS = 30L
        val sPoolWorkQueue = LinkedBlockingQueue<Runnable>(128)
        // 下面的两个是同一个线程池，只不过适配的接口不同
        val limitedTreadPoolExecutor = LocalTreadPoolExecutor()

        /**
         * 判断当前线程是否是数据库线程
         * */
        fun isInDBThread(): Boolean {
            return Thread.currentThread().id == LocalDBFactory.getDbThreadId()
        }
    }

    private object LocalFactory : ThreadFactory {
        override fun newThread(r: Runnable?): Thread {
            return Thread(r, "limit schedule")
        }
    }

    private object LocalDBFactory : ThreadFactory {

        private var currentThread: Thread? = null

        fun getDbThreadId() = currentThread?.id ?: -1

        override fun newThread(r: Runnable?): Thread {
            val dbThread = Thread(r, "db schedule")
            currentThread = dbThread
            return dbThread
        }
    }

    class DBTreadPoolExecutor : ThreadPoolExecutor(
        1,
        1,
        0,
        TimeUnit.SECONDS,
        LinkedBlockingQueue(),
        LocalDBFactory
    ) {

        override fun execute(command: Runnable?) {
            Logger.d(TAG, "DBTreadPoolExecutor: ${command.hashCode()} started")
            super.execute(command)
            Logger.d(TAG, "DBTreadPoolExecutor: ${command.hashCode()} end")
        }
    }

    class LocalTreadPoolExecutor : ThreadPoolExecutor(
        CORE_POOL_SIZE,
        MAXIMUM_POOL_SIZE,
        KEEP_ALIVE_SECONDS,
        TimeUnit.SECONDS,
        sPoolWorkQueue,
        LocalFactory
    ) {
        override fun execute(command: Runnable?) {
            Logger.d(TAG, "thread_id:${Thread.currentThread().id}  start@${command.hashCode()}")
            super.execute(command)
            Logger.d(TAG, "thread_id:${Thread.currentThread().id} end@${command.hashCode()}")
        }
    }

}

