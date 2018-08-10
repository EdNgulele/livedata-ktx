/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Shopify Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.shopify.livedataktx

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer

/**
 * map
 */

private class MapExt2<T, R>(private val mapper: (T?) -> R?) : Operator<T, R> {

    override fun run(value: T?): Result<R>? = mapper(value)?.let { Result(it) }
}

fun <T, R> LiveData<T>.map2(mapper: (T?) -> R?): LiveData<R> = SupportLiveData<T, R>(this, MapExt2(mapper))
fun <T, R> NonNullLiveData<T>.map2(mapper: (T) -> R): NonNullLiveData<R> = SupportLiveData<T, R>(this, MapExt2<T, R>({
    return@MapExt2 mapper(it!!)!!
}))

/**
 * nonNull
 */

private class NonNullExt2<T> : Operator<T, T> {

    override fun run(value: T?): Result<T>? = value?.let { Result(it) }
}

fun <T> LiveData<T>.nonNull2(): NonNullLiveData<T> = SupportLiveData(this, NonNullExt2())

/**
 * observers
 */
fun <T> LiveData<T>.observe2(owner: LifecycleOwner, observer: (t: T?) -> Unit) {
    observe(owner, Observer { observer(it) })
}

fun <T> LiveData<T>.observe2(observer: (t: T?) -> Unit) {
    observeForever({ observer(it) })
}

@Suppress("DEPRECATION")
fun <T> NonNullLiveData<T>.observe2(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    observe(owner, Observer { observer(it!!) })
}

@Suppress("DEPRECATION")
fun <T> NonNullLiveData<T>.observe2(observer: (t: T) -> Unit) {
    observeForever({ observer(it!!) })
}

/**
 * Supporting classes
 */

open class NonNullLiveData<T> : LiveData<T>()

interface Operator<IN, OUT> {

    fun run(value: IN?): Result<OUT>?
}

data class Result<T>(val value: T)

private class SupportLiveData<IN, OUT>(private val source: LiveData<IN>, private val operator: Operator<IN, OUT>) : NonNullLiveData<OUT>() {

    private val observer = Observer<IN> {
        val convertedValue = operator.run(it)
        convertedValue?.value?.let { value = it }
    }

    override fun onActive() {
        super.onActive()
        source.observeForever(observer)
    }

    override fun onInactive() {
        super.onInactive()
        source.removeObserver(observer)
    }
}