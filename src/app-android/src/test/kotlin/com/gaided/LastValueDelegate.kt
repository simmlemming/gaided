package com.gaided

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlin.reflect.KProperty

internal fun <T> Flow<T>.lastValue(scope: CoroutineScope, initialValue: T): ReadOnlyDelegate<T> =
    LastValueDelegate(this, scope, initialValue)

internal interface ReadOnlyDelegate<T> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
}

private class LastValueDelegate<T>(flow: Flow<T>, scope: CoroutineScope, initialValue: T) : ReadOnlyDelegate<T> {
    private val valueHolder = flow.stateIn(scope, SharingStarted.Eagerly, initialValue)

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return valueHolder.value
    }
}
