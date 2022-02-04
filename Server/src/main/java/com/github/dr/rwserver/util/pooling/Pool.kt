/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util.pooling

import com.github.dr.rwserver.struct.Seq
import kotlin.math.max


/**
 * A pool of objects that can be reused to avoid allocation.
 * @author Nathan Sweet
 * @see Pools
 */
abstract class Pool<T> @JvmOverloads constructor(
    initialCapacity: Int = 16,
    /** The maximum number of objects that will be pooled.  */
    val max: Int = Int.MAX_VALUE) {

    private val freeObjects: Seq<T>

    /** The highest number of free objects. Can be reset any time.  */
    private var peak = 0
    protected abstract fun newObject(): T

    /**
     * Returns an object from this pool. The object may be new (from [.newObject]) or reused (previously
     * [freed][.free]).
     */
    fun obtain(): T {
        return if (freeObjects.size() == 0) newObject() else freeObjects.pop()
    }

    /**
     * Puts the specified object in the pool, making it eligible to be returned by [.obtain]. If the pool already contains
     * [.max] free objects, the specified object is reset but not added to the pool.
     *
     *
     * The pool does not check if an object is already freed, so the same object must not be freed multiple times.
     */
    fun free(`object`: T) {
        if (freeObjects.size() < max) {
            freeObjects.add(`object`)
            peak = max(peak, freeObjects.size())
        }
        reset(`object`)
    }

    /**
     * Called when an object is freed to clear the state of the object for possible later reuse. The default implementation calls
     * [Poolable.reset] if the object is [Poolable].
     */
    protected fun reset(`object`: T) {
        if (`object` is Poolable) (`object` as Poolable).reset()
    }

    /**
     * Puts the specified objects in the pool. Null objects within the array are silently ignored.
     *
     *
     * The pool does not check if an object is already freed, so the same object must not be freed multiple times.
     * @see .free
     */
    fun freeAll(objects: Seq<T>) {
        val freeObjects = freeObjects
        val max = max
        for (i in 0 until objects.size()) {
            val `object` = objects[i] ?: continue
            if (freeObjects.size() < max) freeObjects.add(`object`)
            reset(`object`)
        }
        peak = max(peak, freeObjects.size())
    }

    /** Removes all free objects from this pool.  */
    fun clear() {
        freeObjects.clear()
    }

    /** The number of objects available to be obtained.  */
    val free: Int
        get() = freeObjects.size()

    /** Objects implementing this interface will have [.reset] called when passed to [Pool.free].  */
    interface Poolable {
        /** Resets the object for reuse. Object references should be nulled and fields may be set to default values.  */
        fun reset()
    }

    /** @param max The maximum number of free objects to store in this pool. */
    /** Creates a pool with an initial capacity of 16 and no maximum.  */
    /** Creates a pool with the specified initial capacity and no maximum.  */
    init {
        freeObjects = Seq(false, initialCapacity)
    }
}
