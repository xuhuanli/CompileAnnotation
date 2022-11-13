package com.lollitech.compiler

import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

/**
 * Copyright (c) 2022-11, lollitech
 * All rights reserved
 * Author: xuhuanli@lollitech.com
 * 1. 扫描定义的注解
 * 2. 生成代码
 */

@AutoService(Process::class)
class BmProcessor : AbstractProcessor() {
    companion object {
        const val AROUTER_MODULE_NAME = "AROUTER_MODULE_NAME"
    }

    // 提供一些非常使用的工具如Elements， Filer， Messager，Types
    private lateinit var env: ProcessingEnvironment
    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        env = processingEnv!!
        println(env.options.getOrDefault(AROUTER_MODULE_NAME, "null"))
    }

    override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
        return true
    }

    // 自定义一些参数传给Processor
    override fun getSupportedOptions(): MutableSet<String> {
        val options = super.getSupportedOptions()
        options.add(AROUTER_MODULE_NAME)
        return options
    }
}