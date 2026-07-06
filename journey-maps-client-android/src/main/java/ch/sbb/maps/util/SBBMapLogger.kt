// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.util

import timber.log.Timber

internal class SBBMapLogger(
    private val className: String?,
) {
    fun debug(
        methodName: String,
        message: String,
    ) {
        Timber.tag(className.orEmpty()).d("[$methodName]: $message")
    }

    fun info(
        methodName: String,
        message: String,
    ) {
        Timber.tag(className.orEmpty()).i("[$methodName]: $message")
    }

    fun error(
        methodName: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        Timber.tag(className.orEmpty()).e(throwable, "[$methodName]: $message")
    }
}
