package com.lollitech.spitest

import java.util.ServiceLoader

/**
 * Copyright (c) 2022-11, lollitech
 * All rights reserved
 * Author: xuhuanli@lollitech.com
 */

fun main(args: Array<String>) {
    val loader = ServiceLoader.load(IBook::class.java)
    for (book in loader) {
        println(book.name())
    }
}