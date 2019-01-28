package com.ltlovezh.avpractice.common.structure;

import java.util.LinkedHashMap;

/**
 * Created by leontli on 2018/2/1.
 * 简单的LRU
 */

public class LruLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = -2367997738582731201L;

    //定义缓存的容量
    private int mCapacity;

    public LruLinkedHashMap(int capacity) {
        super(16, 0.5f, true);
        this.mCapacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Entry eldest) {
        return size() > mCapacity;
    }
}
