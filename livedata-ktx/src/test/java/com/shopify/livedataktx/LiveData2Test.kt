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

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.MutableLiveData
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class LiveData2Test : LifecycleOwner {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    private lateinit var lifecycleRegistry: LifecycleRegistry

    @Before
    fun setup() {
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    @After
    fun teardown() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    @Test
    fun map() {
        val sourceLiveData: MutableLiveData<Boolean> = MutableLiveData()
        val actuals: MutableList<Boolean?> = mutableListOf()
        val observer: (t: Boolean?) -> Unit = { actuals.add(it) }
        val targetLiveData = sourceLiveData.map2 { true }
        targetLiveData.observe2(this, observer)

        sourceLiveData.value = true
        sourceLiveData.value = false
        sourceLiveData.value = false
        sourceLiveData.value = true

        val expecteds = mutableListOf(true, true, true, true)
        assertEquals(expecteds, actuals)
        assertEquals(targetLiveData is MutableLiveData<*>, false)
    }

    @Test
    fun nonNull() {
        val sourceLiveData: MutableLiveData<Boolean> = MutableLiveData()
        val actuals: MutableList<Boolean> = mutableListOf()
        val observer: (t: Boolean) -> Unit = { actuals.add(it) }
        val targetLiveData = sourceLiveData.nonNull2()
        targetLiveData.observe2(this, observer)

        sourceLiveData.value = true
        sourceLiveData.value = null
        sourceLiveData.value = false
        sourceLiveData.value = null
        sourceLiveData.value = false
        sourceLiveData.value = null
        sourceLiveData.value = true
        sourceLiveData.value = null

        val expecteds = mutableListOf(true, false, false, true)
        assertEquals(expecteds, actuals)
        assertEquals(targetLiveData is MutableLiveData<*>, false)
    }
}