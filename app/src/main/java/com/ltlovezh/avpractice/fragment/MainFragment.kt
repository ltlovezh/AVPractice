package com.ltlovezh.avpractice.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.ltlovezh.avpractice.R
import com.ltlovezh.avpractice.common.Logger
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * Author : litao
 * Time : 2019/1/28 - 2:19 PM
 * Description : This is MainFragment
 */
class MainFragment : BaseFragment() {

    companion object {
        const val TAG = "MainFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")

        initView()
        initData()
    }

    private fun initView() {
        navigation_list.layoutManager = LinearLayoutManager(context)
    }

    private fun initData() {
        val dataList = ArrayList<ItemData>()
        // TODO 添加内容
        dataList.add(ItemData("animated", "image", R.id.animated_image_fragment))

        val adapter = MyAdapter(dataList, this)
        navigation_list.adapter = adapter
    }


    data class ItemData(val title: String, val desc: String, val resId: Int)

    class MyAdapter(val dataList: ArrayList<ItemData>, val fragment: MainFragment) :
        RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val root = LayoutInflater.from(parent.context).inflate(R.layout.navigation_list_item_layout, parent, false)

            return MyViewHolder(root)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val itemData = dataList.get(position)
            holder.titleView.text = itemData.title
            holder.descView.text = itemData.desc
            holder.itemView.setOnClickListener {
                fragment.findNavController().navigate(itemData.resId)
            }
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleView: TextView = itemView.findViewById(R.id.title)
        var descView: TextView = itemView.findViewById(R.id.desc)

    }

}