package com.lollitech.annotations

/**
 * Copyright (c) 2022-11, lollitech
 * All rights reserved
 * Author: xuhuanli@lollitech.com
 */

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.TYPE)
annotation class BmAndroidEntryPoint(val value: Int = 0)
