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

private class MapExt2<T, R>(private val mapper: (T?) -> R?) : Converter<T> {

    override fun convert(value: T?): Any? = mapper(value)
}

fun <T, R> LiveData<T>.map2(mapper: (T?) -> R?): LiveData<R> = SupportLiveData<T, R>(this, MapExt2(mapper))
fun <T, R> NonNullLiveData<T>.map2(mapper: (T) -> R): NonNullLiveData<R> = SupportLiveData<T, R>(this, MapExt2<T, R>({
    return@MapExt2 mapper(it!!)!!
}))

/**
 * nonNull
 */

private class NonNullExt2<T> : Converter<T> {

    override fun convert(value: T?): Any? = value ?: NOT_SET
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

private val NOT_SET = Any()

open class NonNullLiveData<T> : LiveData<T>()

private interface Converter<IN> {

    fun convert(value: IN?): Any?
}

private class SupportLiveData<IN, OUT>(private val source: LiveData<IN>, private val converter: Converter<IN>) : NonNullLiveData<OUT>() {

    @Suppress("UNCHECKED_CAST")
    private val observer = Observer<IN> {
        val convertedValue = converter.convert(it)
        if (convertedValue != NOT_SET) {
            value = convertedValue as? OUT
        }
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