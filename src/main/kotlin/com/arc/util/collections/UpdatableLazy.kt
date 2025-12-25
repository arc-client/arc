
package com.arc.util.collections

/**
 * A lazy-initialized value holder that allows the stored value to be reset and re-initialized on demand.
 */
class UpdatableLazy<T>(private val initializer: () -> T) {
    private var _value: T? = null

    val value: T?
        get() {
            if (_value == null) _value = initializer()
            return _value
        }
    fun update() {
        _value = initializer()
    }
}

fun <T> updatableLazy(initializer: () -> T) = UpdatableLazy(initializer)