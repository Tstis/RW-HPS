/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.struct;

import com.github.dr.rwserver.func.Cons2;
import com.github.dr.rwserver.func.Prov;
import com.github.dr.rwserver.math.Mathf;
import com.github.dr.rwserver.util.log.exp.VariableException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 无序Map
 * 此实现是一个布谷鸟哈希图
 * 使用3个散列，随机游走和用于问题键的小存储区。空键是不允许的。允许为空值。除了增加表大小时，不进行任何分配
 * 此映射执行非常快的get，containsKey和remove（通常为O（1），最坏情况为O（log（n））） Put可能会变慢，
 * 取决于哈希冲突。负载因子大于0.91大大增加了将地图重新​​哈希化为*下一个更高的POT大小的机会
 * 对于大容量的地图，迭代速度可能非常慢。 {@link #clear（int）}和{@link #shrink（int）}可用于减少容量。 {@link OrderedMap}提供了更快的迭代速度。
 * @author Nathan Sweet
 */
@SuppressWarnings("unchecked")
public class ObjectMap<K, V> implements Iterable<ObjectMap.Entry<K, V>>{
    private static final int PRIME1 = 0xbe1f14b1;
    private static final int PRIME2 = 0xb4b82e39;
    private static final int PRIME3 = 0xced1c241;

    public int size;

    K[] keyTable;
    V[] valueTable;
    int capacity, stashSize;

    private final float loadFactor;
    private int hashShift, mask, threshold;
    private int stashCapacity;
    private int pushIterations;

    Entries entries1, entries2;
    Values values1, values2;
    Keys keys1, keys2;

    public static <K, V> ObjectMap<K, V> of(Object... values){
        ObjectMap<K, V> map = new ObjectMap<>();

        for(int i = 0; i < values.length / 2; i++){
            map.put((K) values[i * 2], (V) values[i * 2 + 1]);
        }

        return map;
    }

    /** Creates a new map with an initial capacity of 51 and a load factor of 0.8. */
    public ObjectMap(){
        this(51, 0.8f);
    }

    /**
     * Creates a new map with a load factor of 0.8.
     * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
     */
    public ObjectMap(int initialCapacity){
        this(initialCapacity, 0.8f);
    }

    /**
     * Creates a new map with the specified initial capacity and load factor. This map will hold initialCapacity items before
     * growing the backing table.
     * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
     */
    public ObjectMap(int initialCapacity, float loadFactor){
        if(initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity must be >= 0: " + initialCapacity);
        }
        initialCapacity = Mathf.nextPowerOfTwo((int)Math.ceil(initialCapacity / loadFactor));
        if(initialCapacity > 1 << 30) {
            throw new IllegalArgumentException("initialCapacity is too large: " + initialCapacity);
        }
        capacity = initialCapacity;

        if(loadFactor <= 0) {
            throw new IllegalArgumentException("loadFactor must be > 0: " + loadFactor);
        }
        this.loadFactor = loadFactor;

        threshold = (int)(capacity * loadFactor);
        mask = capacity - 1;
        hashShift = 31 - Integer.numberOfTrailingZeros(capacity);
        stashCapacity = Math.max(3, (int)Math.ceil(Math.log(capacity)) * 2);
        pushIterations = Math.max(Math.min(capacity, 8), (int)Math.sqrt(capacity) / 8);

        keyTable = (K[])new Object[capacity + stashCapacity];
        valueTable = (V[])new Object[keyTable.length];
    }

    /** Creates a new map identical to the specified map. */
    public ObjectMap(ObjectMap<? extends K, ? extends V> map){
        this((int)Math.floor(map.capacity * map.loadFactor), map.loadFactor);
        stashSize = map.stashSize;
        System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
        System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
        size = map.size;
    }

    /**Iterates through key/value pairs.*/
    public void each(Cons2<K, V> cons){
        for(Entry<K, V> entry : entries()){
            cons.get(entry.key, entry.value);
        }
    }

    /** Returns the old value associated with the specified key, or null. */
    public V put(K key, V value){
        if(key == null) {
            throw new IllegalArgumentException("key cannot be null.");
        }
        K[] keyTable = this.keyTable;

        // Check for existing keys.
        int hashCode = key.hashCode();
        int index1 = hashCode & mask;
        K key1 = keyTable[index1];
        if(key.equals(key1)){
            V oldValue = valueTable[index1];
            valueTable[index1] = value;
            return oldValue;
        }

        int index2 = hash2(hashCode);
        K key2 = keyTable[index2];
        if(key.equals(key2)){
            V oldValue = valueTable[index2];
            valueTable[index2] = value;
            return oldValue;
        }

        int index3 = hash3(hashCode);
        K key3 = keyTable[index3];
        if(key.equals(key3)){
            V oldValue = valueTable[index3];
            valueTable[index3] = value;
            return oldValue;
        }

        // Update key in the stash.
        for(int i = capacity, n = i + stashSize; i < n; i++){
            if(key.equals(keyTable[i])){
                V oldValue = valueTable[i];
                valueTable[i] = value;
                return oldValue;
            }
        }

        // Check for empty buckets.
        if(key1 == null){
            keyTable[index1] = key;
            valueTable[index1] = value;
            if(size++ >= threshold) {
                resize(capacity << 1);
            }
            return null;
        }

        if(key2 == null){
            keyTable[index2] = key;
            valueTable[index2] = value;
            if(size++ >= threshold) {
                resize(capacity << 1);
            }
            return null;
        }

        if(key3 == null){
            keyTable[index3] = key;
            valueTable[index3] = value;
            if(size++ >= threshold) {
                resize(capacity << 1);
            }
            return null;
        }

        push(key, value, index1, key1, index2, key2, index3, key3);
        return null;
    }

    public void putAll(ObjectMap<? extends K, ? extends V> map){
        ensureCapacity(map.size);
        for(Entry<? extends K, ? extends V> entry : map) {
            put(entry.key, entry.value);
        }
    }

    /** Put all the keys of this other map into this map, and return this map for chaining. */
    public ObjectMap<K, V> merge(ObjectMap<? extends K, ? extends V> map){
        putAll(map);
        return this;
    }

    /** Skips checks for existing keys. */
    private void putResize(K key, V value){
        // Check for empty buckets.
        int hashCode = key.hashCode();
        int index1 = hashCode & mask;
        K key1 = keyTable[index1];
        if(key1 == null){
            keyTable[index1] = key;
            valueTable[index1] = value;
            if(size++ >= threshold) {
                resize(capacity << 1);
            }
            return;
        }

        int index2 = hash2(hashCode);
        K key2 = keyTable[index2];
        if(key2 == null){
            keyTable[index2] = key;
            valueTable[index2] = value;
            if(size++ >= threshold) {
                resize(capacity << 1);
            }
            return;
        }

        int index3 = hash3(hashCode);
        K key3 = keyTable[index3];
        if(key3 == null){
            keyTable[index3] = key;
            valueTable[index3] = value;
            if(size++ >= threshold) {
                resize(capacity << 1);
            }
            return;
        }

        push(key, value, index1, key1, index2, key2, index3, key3);
    }

    private void push(K insertKey, V insertValue, int index1, K key1, int index2, K key2, int index3, K key3){
        K[] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        int mask = this.mask;

        // Push keys until an empty bucket is found.
        K evictedKey;
        V evictedValue;
        int i = 0, pushIterations = this.pushIterations;
        do{
            // Replace the key and value for one of the hashes.
            switch(Mathf.random(2)){
                case 0:
                    evictedKey = key1;
                    evictedValue = valueTable[index1];
                    keyTable[index1] = insertKey;
                    valueTable[index1] = insertValue;
                    break;
                case 1:
                    evictedKey = key2;
                    evictedValue = valueTable[index2];
                    keyTable[index2] = insertKey;
                    valueTable[index2] = insertValue;
                    break;
                default:
                    evictedKey = key3;
                    evictedValue = valueTable[index3];
                    keyTable[index3] = insertKey;
                    valueTable[index3] = insertValue;
                    break;
            }

            // If the evicted key hashes to an empty bucket, put it there and stop.
            int hashCode = evictedKey.hashCode();
            index1 = hashCode & mask;
            key1 = keyTable[index1];
            if(key1 == null){
                keyTable[index1] = evictedKey;
                valueTable[index1] = evictedValue;
                if(size++ >= threshold) {
                    resize(capacity << 1);
                }
                return;
            }

            index2 = hash2(hashCode);
            key2 = keyTable[index2];
            if(key2 == null){
                keyTable[index2] = evictedKey;
                valueTable[index2] = evictedValue;
                if(size++ >= threshold) {
                    resize(capacity << 1);
                }
                return;
            }

            index3 = hash3(hashCode);
            key3 = keyTable[index3];
            if(key3 == null){
                keyTable[index3] = evictedKey;
                valueTable[index3] = evictedValue;
                if(size++ >= threshold) {
                    resize(capacity << 1);
                }
                return;
            }

            if(++i == pushIterations) {
                break;
            }

            insertKey = evictedKey;
            insertValue = evictedValue;
        }while(true);

        putStash(evictedKey, evictedValue);
    }

    private void putStash(K key, V value){
        if(stashSize == stashCapacity){
            // Too many pushes occurred and the stash is full, increase the table size.
            resize(capacity << 1);
            putResize(key, value);
            return;
        }
        // Store key in the stash.
        int index = capacity + stashSize;
        keyTable[index] = key;
        valueTable[index] = value;
        stashSize++;
        size++;
    }

    public V getThrow(K key, Prov<? extends RuntimeException> error){
        if(!containsKey(key)){
            throw error.get();
        }
        return get(key);
    }

    /** Tries to get the value. If it does not exist, it creates a new instance using the supplier and places it, returning the value.*/
    public V get(K key, Prov<V> supplier){
        V val = get(key);
        if(val == null){
            put(key, val = supplier.get());
        }
        return val;
    }

    /** Returns the value for the specified key, or null if the key is not in the map. */
    public V get(K key){
        int hashCode = key.hashCode();
        int index = hashCode & mask;
        if(!key.equals(keyTable[index])){
            index = hash2(hashCode);
            if(!key.equals(keyTable[index])){
                index = hash3(hashCode);
                if(!key.equals(keyTable[index])) {
                    return getStash(key, null);
                }
            }
        }
        return valueTable[index];
    }

    /** Returns the value for the specified key, or the default value if the key is not in the map. */
    public V get(K key, V defaultValue){
        int hashCode = key.hashCode();
        int index = hashCode & mask;
        if(!key.equals(keyTable[index])){
            index = hash2(hashCode);
            if(!key.equals(keyTable[index])){
                index = hash3(hashCode);
                if(!key.equals(keyTable[index])) {
                    return getStash(key, defaultValue);
                }
            }
        }
        return valueTable[index];
    }

    /** 尝试获取值。如果它不存在返回值.*/
    public V getNoPut(K key, Prov<V> supplier){
        V val = get(key);
        if(val == null){
            return supplier.get();
        }
        return val;
    }

    /** Get, with a nullable key.*/
    public V getNull(K key){
        return key == null ? null : get(key);
    }

    private V getStash(K key, V defaultValue){
        K[] keyTable = this.keyTable;
        for(int i = capacity, n = i + stashSize; i < n; i++) {
            if(key.equals(keyTable[i])) {
                return valueTable[i];
            }
        }
        return defaultValue;
    }

    /** Returns the value associated with the key, or null. */
    public V remove(K key){
        int hashCode = key.hashCode();
        int index = hashCode & mask;
        if(key.equals(keyTable[index])){
            keyTable[index] = null;
            V oldValue = valueTable[index];
            valueTable[index] = null;
            size--;
            return oldValue;
        }

        index = hash2(hashCode);
        if(key.equals(keyTable[index])){
            keyTable[index] = null;
            V oldValue = valueTable[index];
            valueTable[index] = null;
            size--;
            return oldValue;
        }

        index = hash3(hashCode);
        if(key.equals(keyTable[index])){
            keyTable[index] = null;
            V oldValue = valueTable[index];
            valueTable[index] = null;
            size--;
            return oldValue;
        }

        return removeStash(key);
    }

    V removeStash(K key){
        K[] keyTable = this.keyTable;
        for(int i = capacity, n = i + stashSize; i < n; i++){
            if(key.equals(keyTable[i])){
                V oldValue = valueTable[i];
                removeStashIndex(i);
                size--;
                return oldValue;
            }
        }
        return null;
    }

    void removeStashIndex(int index){
        // If the removed location was not last, move the last tuple to the removed location.
        stashSize--;
        int lastIndex = capacity + stashSize;
        if(index < lastIndex){
            keyTable[index] = keyTable[lastIndex];
            valueTable[index] = valueTable[lastIndex];
            keyTable[lastIndex] = null;
            valueTable[lastIndex] = null;
        }else{
            keyTable[index] = null;
            valueTable[index] = null;
        }
    }

    /** Returns true if the map is empty. */
    public boolean isEmpty(){
        return size == 0;
    }

    /**
     * Reduces the size of the backing arrays to be the specified capacity or less. If the capacity is already less, nothing is
     * done. If the map contains more items than the specified capacity, the next highest power of two capacity is used instead.
     */
    public void shrink(int maximumCapacity){
        if(maximumCapacity < 0) {
            throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
        }
        if(size > maximumCapacity) {
            maximumCapacity = size;
        }
        if(capacity <= maximumCapacity) {
            return;
        }
        maximumCapacity = Mathf.nextPowerOfTwo(maximumCapacity);
        resize(maximumCapacity);
    }

    /**
     * Clears the map and reduces the size of the backing arrays to be the specified capacity, if they are larger. The reduction
     * is done by allocating new arrays, though for large arrays this can be faster than clearing the existing array.
     */
    public void clear(int maximumCapacity){
        if(capacity <= maximumCapacity){
            clear();
            return;
        }
        size = 0;
        resize(maximumCapacity);
    }

    /**
     * Clears the map, leaving the backing arrays at the current capacity. When the capacity is high and the population is low,
     * iteration can be unnecessarily slow. {@link #clear(int)} can be used to reduce the capacity.
     */
    public void clear(){
        if(size == 0) {
            return;
        }
        K[] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        for(int i = capacity + stashSize; i-- > 0; ){
            keyTable[i] = null;
            valueTable[i] = null;
        }
        size = 0;
        stashSize = 0;
    }

    /**
     * Returns true if the specified value is in the map. Note this traverses the entire map and compares every value, which may
     * be an expensive operation.
     * @param identity If true, uses == to compare the specified value with values in the map. If false, uses
     * {@link #equals(Object)}.
     */
    public boolean containsValue(Object value, boolean identity){
        V[] valueTable = this.valueTable;
        if(value == null){
            K[] keyTable = this.keyTable;
            for(int i = capacity + stashSize; i-- > 0; ) {
                if(keyTable[i] != null && valueTable[i] == null) {
                    return true;
                }
            }
        }else if(identity){
            for(int i = capacity + stashSize; i-- > 0; ) {
                if(valueTable[i] == value) {
                    return true;
                }
            }
        }else{
            for(int i = capacity + stashSize; i-- > 0; ) {
                if(value.equals(valueTable[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsKey(K key){
        int hashCode = key.hashCode();
        int index = hashCode & mask;
        if(!key.equals(keyTable[index])){
            index = hash2(hashCode);
            if(!key.equals(keyTable[index])){
                index = hash3(hashCode);
                if(!key.equals(keyTable[index])) {
                    return containsKeyStash(key);
                }
            }
        }
        return true;
    }

    private boolean containsKeyStash(K key){
        K[] keyTable = this.keyTable;
        for(int i = capacity, n = i + stashSize; i < n; i++) {
            if(key.equals(keyTable[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the key for the specified value, or null if it is not in the map. Note this traverses the entire map and compares
     * every value, which may be an expensive operation.
     * @param identity If true, uses == to compare the specified value with values in the map. If false, uses
     * {@link #equals(Object)}.
     */
    public K findKey(Object value, boolean identity){
        V[] valueTable = this.valueTable;
        if(value == null){
            K[] keyTable = this.keyTable;
            for(int i = capacity + stashSize; i-- > 0; ) {
                if(keyTable[i] != null && valueTable[i] == null) {
                    return keyTable[i];
                }
            }
        }else if(identity){
            for(int i = capacity + stashSize; i-- > 0; ) {
                if(valueTable[i] == value) {
                    return keyTable[i];
                }
            }
        }else{
            for(int i = capacity + stashSize; i-- > 0; ) {
                if(value.equals(valueTable[i])) {
                    return keyTable[i];
                }
            }
        }
        return null;
    }

    /**
     * Increases the size of the backing array to accommodate the specified number of additional items. Useful before adding many
     * items to avoid multiple backing array resizes.
     */
    public void ensureCapacity(int additionalCapacity){
        if(additionalCapacity < 0) {
            throw new IllegalArgumentException("additionalCapacity must be >= 0: " + additionalCapacity);
        }
        int sizeNeeded = size + additionalCapacity;
        if(sizeNeeded >= threshold) {
            resize(Mathf.nextPowerOfTwo((int)Math.ceil(sizeNeeded / loadFactor)));
        }
    }

    private void resize(int newSize){
        int oldEndIndex = capacity + stashSize;

        capacity = newSize;
        threshold = (int)(newSize * loadFactor);
        mask = newSize - 1;
        hashShift = 31 - Integer.numberOfTrailingZeros(newSize);
        stashCapacity = Math.max(3, (int)Math.ceil(Math.log(newSize)) * 2);
        pushIterations = Math.max(Math.min(newSize, 8), (int)Math.sqrt(newSize) / 8);

        K[] oldKeyTable = keyTable;
        V[] oldValueTable = valueTable;

        keyTable = (K[])new Object[newSize + stashCapacity];
        valueTable = (V[])new Object[newSize + stashCapacity];

        int oldSize = size;
        size = 0;
        stashSize = 0;
        if(oldSize > 0){
            for(int i = 0; i < oldEndIndex; i++){
                K key = oldKeyTable[i];
                if(key != null) {
                    putResize(key, oldValueTable[i]);
                }
            }
        }
    }

    private int hash2(int h){
        h *= PRIME2;
        return (h ^ h >>> hashShift) & mask;
    }

    private int hash3(int h){
        h *= PRIME3;
        return (h ^ h >>> hashShift) & mask;
    }

    @Override
    public int hashCode(){
        int h = 0;
        K[] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        for(int i = 0, n = capacity + stashSize; i < n; i++){
            K key = keyTable[i];
            if(key != null){
                h += key.hashCode() * 31;

                V value = valueTable[i];
                if(value != null){
                    h += value.hashCode();
                }
            }
        }
        return h;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof ObjectMap)) {
            return false;
        }
        ObjectMap<K, V> other = (ObjectMap)obj;
        if(other.size != size) {
            return false;
        }
        K[] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        for(int i = 0, n = capacity + stashSize; i < n; i++){
            K key = keyTable[i];
            if(key != null){
                V value = valueTable[i];
                if(value == null){
                    if(!other.containsKey(key) || other.get(key) != null) {
                        return false;
                    }
                }else{
                    if(!value.equals(other.get(key))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String toString(String separator){
        return toString(separator, false);
    }

    @Override
    public String toString(){
        return toString(", ", true);
    }

    public String toString(String separator, boolean braces){
        if(size == 0) {
            return braces ? "{}" : "";
        }
        StringBuilder buffer = new StringBuilder(32);
        if(braces) {
            buffer.append('{');
        }
        K[] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        int i = keyTable.length;
        while(i-- > 0){
            K key = keyTable[i];
            if(key == null) {
                continue;
            }
            buffer.append(key);
            buffer.append('=');
            buffer.append(valueTable[i]);
            break;
        }
        while(i-- > 0){
            K key = keyTable[i];
            if(key == null) {
                continue;
            }
            buffer.append(separator);
            buffer.append(key);
            buffer.append('=');
            buffer.append(valueTable[i]);
        }
        if(braces) {
            buffer.append('}');
        }
        return buffer.toString();
    }

    @Override
    public Entries<K, V> iterator(){
        return entries();
    }

    /**
     * Returns an iterator for the entries in the map. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called. Use the {@link Entries} constructor for nested or multithreaded iteration.
     */
    public Entries<K, V> entries(){
        if(entries1 == null){
            entries1 = new Entries(this);
            entries2 = new Entries(this);
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
     * time this method is called. Use the {@link Values} constructor for nested or multithreaded iteration.
     */
    public Values<V> values(){
        if(values1 == null){
            values1 = new Values(this);
            values2 = new Values(this);
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
     * time this method is called. Use the {@link Keys} constructor for nested or multithreaded iteration.
     */
    public Keys<K> keys(){
        if(keys1 == null){
            keys1 = new Keys(this);
            keys2 = new Keys(this);
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

    public static class Entry<K, V>{
        public K key;
        public V value;

        @Override
        public String toString(){
            return key + "=" + value;
        }
    }

    private static abstract class BaseMapIterator<K, V, I> implements Iterable<I>, Iterator<I>{
        final ObjectMap<K, V> map;
        public boolean hasNext;
        int nextIndex, currentIndex;
        boolean valid = true;

        public BaseMapIterator(ObjectMap<K, V> map){
            this.map = map;
            reset();
        }

        public void reset(){
            currentIndex = -1;
            nextIndex = -1;
            findNextIndex();
        }

        void findNextIndex(){
            hasNext = false;
            K[] keyTable = map.keyTable;
            for(int n = map.capacity + map.stashSize; ++nextIndex < n; ){
                if(keyTable[nextIndex] != null){
                    hasNext = true;
                    break;
                }
            }
        }

        @Override
        public void remove(){
            if(currentIndex < 0) {
                throw new IllegalStateException("next must be called before remove.");
            }
            if(currentIndex >= map.capacity){
                map.removeStashIndex(currentIndex);
                nextIndex = currentIndex - 1;
                findNextIndex();
            }else{
                map.keyTable[currentIndex] = null;
                map.valueTable[currentIndex] = null;
            }
            currentIndex = -1;
            map.size--;
        }
    }

    public static class Entries<K, V> extends BaseMapIterator<K, V, Entry<K, V>>{
        final Entry<K, V> entry = new Entry();

        public Entries(ObjectMap<K, V> map){
            super(map);
        }

        /** Note the same entry instance is returned each time this method is called. */
        @Override
        public Entry<K, V> next(){
            if(!hasNext) {
                throw new NoSuchElementException();
            }
            if(!valid) {
                throw new VariableException.ObjectMapRuntimeException("#iterator() cannot be used nested.");
            }
            K[] keyTable = map.keyTable;
            entry.key = keyTable[nextIndex];
            entry.value = map.valueTable[nextIndex];
            currentIndex = nextIndex;
            findNextIndex();
            return entry;
        }

        @Override
        public boolean hasNext(){
            if(!valid) {
                throw new VariableException.ObjectMapRuntimeException("#iterator() cannot be used nested.");
            }
            return hasNext;
        }

        @Override
        public Entries<K, V> iterator(){
            return this;
        }
    }

    public static class Values<V> extends BaseMapIterator<Object, V, V>{
        public Values(ObjectMap<?, V> map){
            super((ObjectMap<Object, V>)map);
        }

        @Override
        public boolean hasNext(){
            if(!valid) {
                throw new VariableException.ObjectMapRuntimeException("#iterator() cannot be used nested.");
            }
            return hasNext;
        }

        @Override
        public V next(){
            if(!hasNext) {
                throw new NoSuchElementException();
            }
            if(!valid) {
                throw new VariableException.ObjectMapRuntimeException("#iterator() cannot be used nested.");
            }
            V value = map.valueTable[nextIndex];
            currentIndex = nextIndex;
            findNextIndex();
            return value;
        }

        @Override
        public Values<V> iterator(){
            return this;
        }

        /** Returns a new array containing the remaining values. */
        public Seq<V> toSeq(){
            return toSeq(new Seq<>(true, map.size));
        }

        /** Adds the remaining values to the specified array. */
        public Seq<V> toSeq(Seq<V> array){
            while(hasNext) {
                array.add(next());
            }
            return array;
        }
    }

    public static class Keys<K> extends BaseMapIterator<K, Object, K>{
        public Keys(ObjectMap<K, ?> map){
            super((ObjectMap<K, Object>)map);
        }

        @Override
        public boolean hasNext(){
            if(!valid) {
                throw new VariableException.ObjectMapRuntimeException("#iterator() cannot be used nested.");
            }
            return hasNext;
        }

        @Override
        public K next(){
            if(!hasNext) {
                throw new NoSuchElementException();
            }
            if(!valid) {
                throw new VariableException.ObjectMapRuntimeException("#iterator() cannot be used nested.");
            }
            K key = map.keyTable[nextIndex];
            currentIndex = nextIndex;
            findNextIndex();
            return key;
        }

        @Override
        public Keys<K> iterator(){
            return this;
        }

        /** Returns a new array containing the remaining keys. */
        public Seq<K> toSeq(){
            return toSeq(new Seq<>(true, map.size));
        }

        /** Adds the remaining keys to the array. */
        public Seq<K> toSeq(Seq<K> array){
            while(hasNext) {
                array.add(next());
            }
            return array;
        }
    }
}
