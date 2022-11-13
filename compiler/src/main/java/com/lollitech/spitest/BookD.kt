package com.lollitech.spitest

import com.google.auto.service.AutoService

/**
 * Copyright (c) 2022-11, lollitech
 * All rights reserved
 * Author: xuhuanli@lollitech.com
 */

//@AutoService(IBook::class)
class BookD: IBook {
    override fun name(): String {
        return "BookD"
    }
}