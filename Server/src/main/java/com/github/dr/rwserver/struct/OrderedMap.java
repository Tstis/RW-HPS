/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.struct;

import com.github.dr.rwserver.util.log.exp.VariableException.MapRuntimeException;

import java.util.NoSuchElementException;

/**
 * {@link ObjectMap}也使用插入顺序将密钥存储在{@link Seq}中
 * {@link #entries()}，{@link #keys()}和{@link #values()}上的迭代是有序的，并且比无序映射要快
 * 还可以使用{@link #orderedKeys()}
 * 访问键并更改顺序。有一些额外的放置和删除开销
 * 如果将用于与ObjectMap相比具有更快的迭代速度并且顺序实际上并不重要
 * 则可以通过将{@link OrderedMap＃orderedKeys()}的{@link Seq＃ordered}设置为false来大大减少删除期间的复制。
 * @author Nathan Sweet
 */
@SuppressWarnings("unchecked")
public class OrderedMap<K, V> extends ObjectMap<K, V>{
    final Seq<K> keys;

    public static <K, V> OrderedMap<K, V> of(Object... values){
        OrderedMap<K, V> map = new OrderedMap<>();

        for(int i = 0; i < values.length / 2; i++){
            map.put((K)values[i * 2], (V)values[i * 2 + 1]);
        }

        return map;
    }

    public OrderedMap(){
        keys = new Seq<>();
    }

    public OrderedMap(int initialCapacity){
        super(initialCapacity);
        keys = new Seq<>(capacity);
    }

    public OrderedMap(int initialCapacity, float loadFactor){
        super(initialCapacity, loadFactor);
        keys = new Seq<>(capacity);
    }

    public OrderedMap(OrderedMap<? extends K, ? extends V> map){
        super(map);
        keys = new Seq<>(map.keys);
    }

    @Override
    public V put(K key, V value){
        if(!containsKey(key)) {
            keys.add(key);
        }
        return super.put(key, value);
    }

    @Override
    public V remove(K key){
        keys.remove(key, false);
        return super.remove(key);
    }

    public V removeIndex(int index){
        return super.remove(keys.remove(index));
    }

    @Override
    public void clear(int maximumCapacity){
        keys.clear();
        super.clear(maximumCapacity);
    }

    @Override
    public void clear(){
        keys.clear();
        super.clear();
    }

    public Seq<K> orderedKeys(){
        return keys;
    }

    @Override
    public Entries<K, V> iterator(){
        return entries();
    }

    /**
     * Returns an iterator for the entries in the map. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called. Use the {@link OrderedMapEntries} constructor for nested or multithreaded iteration.
     */
    @Override
    public Entries<K, V> entries(){
        if(entries1 == null){
            entries1 = new OrderedMapEntries<>(this);
            entries2 = new OrderedMapEntries<>(this);
        }
        if(!entries1.valid){
            entries1.reset();
            entries1.valid = true;
            entries2.valid = false;
            return entries1;
        }
        entries2.reset();
        entries2.valid = true;
        entries1.valid = false;
        return entries2;
    }

    /**
     * Returns an iterator for the values in the map. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called. Use the {@link OrderedMapValues} constructor for nested or multithreaded iteration.
     */
    @Override
    public Values<V> values(){
        if(values1 == null){
            values1 = new OrderedMapValues<>(this);
            values2 = new OrderedMapValues<>(this);
        }
        if(!values1.valid){
            values1.reset();
            values1.valid = true;
            values2.valid = false;
            return values1;
        }
        values2.reset();
        values2.valid = true;
        values1.valid = false;
        return values2;
    }

    /**
     * Returns an iterator for the keys in the map. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called. Use the {@link OrderedMapKeys} constructor for nested or multithreaded iteration.
     */
    @Override
    public Keys<K> keys(){
        if(keys1 == null){
            keys1 = new OrderedMapKeys<>(this);
            keys2 = new OrderedMapKeys<>(this);
        }
        if(!keys1.valid){
            keys1.reset();
            keys1.valid = true;
            keys2.valid = false;
            return keys1;
        }
        keys2.reset();
        keys2.valid = true;
        keys1.valid = false;
        return keys2;
    }

    @Override
    public String toString(){
        if(size == 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('{');
        Seq<K> keys = this.keys;
        for(int i = 0, n = keys.size(); i < n; i++){
            K key = keys.get(i);
            if(i > 0) {
                buffer.append(", ");
            }
            buffer.append(key);
            buffer.append('=');
            buffer.append(get(key));
        }
        buffer.append('}');
        return buffer.toString();
    }

    public static class OrderedMapEntries<K, V> extends Entries<K, V>{
        private final Seq<K> keys;

        public OrderedMapEntries(OrderedMap<K, V> map){
            super(map);
            keys = map.keys;
        }

        @Override
        public void reset(){
            nextIndex = 0;
            hasNext = map.size > 0;
        }

        @Override
        public Entry<K, V> next(){
            if(!hasNext) {
                throw new NoSuchElementException();
            }
            if(!valid) {
                throw new MapRuntimeException("#iterator() cannot be used nested.");
            }
            entry.key = keys.get(nextIndex);
            entry.value = map.get(entry.key);
            nextIndex++;
            hasNext = nextIndex < map.size;
            return entry;
        }

        @Override
        public void remove(){
            if(currentIndex < 0) {
                throw new IllegalStateException("next must be called before remove.");
            }
            map.remove(entry.key);
            nextIndex--;
        }
    }

    public static class OrderedMapKeys<K> extends Keys<K>{
        private final Seq<K> keys;

        public OrderedMapKeys(OrderedMap<K, ?> map){
            super(map);
            keys = map.keys;
        }

        @Override
        public void reset(){
            nextIndex = 0;
            hasNext = map.size > 0;
        }

        @Override
        public K next(){
            if(!hasNext) {
                throw new NoSuchElementException();
            }
            if(!valid) {
                throw new MapRuntimeException("#iterator() cannot be used nested.");
            }
            K key = keys.get(nextIndex);
            currentIndex = nextIndex;
            nextIndex++;
            hasNext = nextIndex < map.size;
            return key;
        }

        @Override
        public void remove(){
            if(currentIndex < 0) {
                throw new IllegalStateException("next must be called before remove.");
            }
            ((OrderedMap)map).removeIndex(nextIndex - 1);
            nextIndex = currentIndex;
            currentIndex = -1;
        }
    }

    public static class OrderedMapValues<V> extends Values<V>{
        private final Seq keys;

        public OrderedMapValues(OrderedMap<?, V> map){
            super(map);
            keys = map.keys;
        }

        @Override
        public void reset(){
            nextIndex = 0;
            hasNext = map.size > 0;
        }

        @Override
        public V next(){
            if(!hasNext) {
                throw new NoSuchElementException();
            }
            if(!valid) {
                throw new MapRuntimeException("#iterator() cannot be used nested.");
            }
            V value = map.get(keys.get(nextIndex));
            currentIndex = nextIndex;
            nextIndex++;
            hasNext = nextIndex < map.size;
            return value;
        }

        @Override
        public void remove(){
            if(currentIndex < 0) {
                throw new IllegalStateException("next must be called before remove.");
            }
            ((OrderedMap)map).removeIndex(currentIndex);
            nextIndex = currentIndex;
            currentIndex = -1;
        }
    }
}
