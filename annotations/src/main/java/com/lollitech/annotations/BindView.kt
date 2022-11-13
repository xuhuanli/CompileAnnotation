package com.lollitech.annotations

/**
 * Copyright (c) 2022-11, lollitech
 * All rights reserved
 * Author: xuhuanli@lollitech.com
 */

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class BindView(val value: Int)
