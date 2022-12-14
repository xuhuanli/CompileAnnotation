package com.lollitech.annotations

import kotlin.reflect.KClass

/**
 * Copyright (c) 2022-11, lollitech
 * All rights reserved
 * Author: xuhuanli@lollitech.com
 */

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE)
annotation class BmAndroidApp(val value: KClass<*> = Void::class)
