package com.lollitech.compiler

import com.google.auto.service.AutoService
import com.lollitech.annotations.BindClass
import com.lollitech.annotations.BindView
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

/**
 * Copyright (c) 2022-11, lollitech
 * All rights reserved
 * Author: xuhuanli@lollitech.com
 * 1. 扫描定义的注解
 * 2. 生成代码
 */

// 简单说下autoservice用来干什么 => spi
@AutoService(Processor::class)
class BmProcessor : AbstractProcessor() {
    private val TAG = "BmProcessor:: "

    companion object {
        const val AROUTER_MODULE_NAME = "AROUTER_MODULE_NAME"
        const val BM_DIR = "BM_DIR"
    }

    // 提供一些非常使用的工具如Elements， Filer， Messager，Types
    private lateinit var env: ProcessingEnvironment

    // 初始化创建这个文件的对象
    private lateinit var filer: Filer

    // 输出编译时日志 = println
    lateinit var messager: Messager

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        env = processingEnv!!
        filer = env.filer
        messager = env.messager
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
//        genJavaFile(roundEnvironment)
        genKotlinFile(roundEnvironment)
        return true
    }

    // 自定义一些参数传给Processor
    override fun getSupportedOptions(): MutableSet<String> {
        val options = mutableSetOf<String>()
        options.add(AROUTER_MODULE_NAME)
        options.add(BM_DIR)
        return options
    }

    /**
     * 声明这个注解处理器能处理的注解
     * 注解处理器依赖注解lib的原因 => 获取到注解类
     *
     * @return
     */
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        val mutableSet = mutableSetOf<String>()
        try {
            mutableSet.add(BindView::class.qualifiedName!!)
            mutableSet.add(BindClass::class.qualifiedName!!)
        } catch (e: Exception) {
            throw e
        }
        return mutableSet
    }


    /**
     * 声明这个注解处理器支持的版本
     * 或者用注解 @SupportedSourceVersion(SourceVersion.RELEASE_8) 替代
     * @return
     */
    override fun getSupportedSourceVersion(): SourceVersion {
        // 初始化的时候父类已经有一个field 可以直接用父类的processingEnv.sourceVersion
        return env.sourceVersion // 直接返回项目的sourceVersion
    }

    /**
     * 生成文件 发现生成出来的是一个java文件
     */
    private fun genJavaFile(roundEnvironment: RoundEnvironment) {
        val elementSet = roundEnvironment.getElementsAnnotatedWith(BindView::class.java)
        // key: activity value: @BindView
        val map = mutableMapOf<String, MutableList<VariableElement>>()
        for (element in elementSet) {
            val variableElement = element as VariableElement
            // enclosingElement 上一层元素 成员变量的上一层 = 类
            val activity = variableElement.enclosingElement.simpleName.toString()
            val list = map[activity]
            list?.let {
                it.add(variableElement)
            } ?: let {
                val variableElements = mutableListOf<VariableElement>()
                map[activity] = variableElements
            }
        }
        val iterator = map.keys.iterator()
        if (iterator.hasNext()) {
            val activityName = iterator.next()
            val variableList = map[activityName]!!
            if (variableList.isNotEmpty()) {
                val packageName = processingEnv.elementUtils.getPackageOf(variableList[0]).toString()
                val fileObject = filer.createSourceFile(packageName + "." + activityName + "_ViewBinding")
                // 手动写 操作writer 也可以通过第三方库javapoet/kotlinpoet来更加快速的写
                val writer = fileObject.openWriter()
                writer.use {
                    val stringBuffer = StringBuffer()
                    // package com.lollitech.spitest
                    //
                    //import java.util.ServiceLoader
                    // 第一行 包名
                    stringBuffer.append("package $packageName\n")
                    stringBuffer.append("\n")
                    // 第二行 导入类
                    stringBuffer.append("import java.util.ServiceLoader\n")
                    stringBuffer.append("class $activityName" + "_ViewBinding : AppCompatActivity() {")
                    for (ele in variableList) {
                        // 拿到成员变量的名字
                        val name = ele.simpleName.toString()
                        // 拿到成员变量的注解
                        val bindView = ele.getAnnotation(BindView::class.java)
                        // 拿到成员变量绑定的控件id
                        val bindId = bindView.value
                        // 控件的类型
                        val typeMirror = ele.asType()
                        messager.printMessage(Diagnostic.Kind.NOTE, "$TAG $name id=$bindId type=${typeMirror}")
//                        stringBuffer.append("($name as $typeMirror).findViewById($bindId)\n")
                    }
                    stringBuffer.append("}\n")
                    writer.write(stringBuffer.toString())
                }
            }
        }
    }

    private fun genKotlinFile(roundEnv: RoundEnvironment) {
        val elements = roundEnv.getElementsAnnotatedWith(BindClass::class.java)
        val elementUtils = processingEnv.elementUtils
        elements.forEach {
            val typeElement = it as TypeElement
            val members = elementUtils!!.getAllMembers(typeElement)

            val bindFunBuilder = FunSpec.builder("bindView").addParameter("activity", typeElement.asClassName())
                .addAnnotation(JvmStatic::class.java)


            members.forEach {
                val find: BindView? = it.getAnnotation(BindView::class.java)
                if (find != null) {
                    bindFunBuilder.addStatement("activity.${it.simpleName} = activity.findViewById(${find.value})")
                }
            }
            val bindFun = bindFunBuilder.build()

            val file = FileSpec.builder(
                processingEnv.elementUtils.getPackageOf(typeElement).qualifiedName.toString(),
                it.simpleName.toString() + "_bindView"
            )
                .addType(
                    TypeSpec.classBuilder(it.simpleName.toString() + "_bindView")
                        .addType(
                            TypeSpec.companionObjectBuilder()
                                .addFunction(bindFun)
                                .build()
                        )
                        .build()
                )
                .build()
            // 指定生成的目录
            try {
                val kaptKotlinGeneratedDir = processingEnv.options["kapt.kotlin.generated"]
                val options = processingEnv.options[AROUTER_MODULE_NAME]
                messager.printMessage(Diagnostic.Kind.NOTE, TAG + options)
                val outputFile = File(kaptKotlinGeneratedDir).apply {
                    mkdirs()
                }
                messager.printMessage(Diagnostic.Kind.NOTE, TAG + outputFile.toPath())
                file.writeTo(outputFile.toPath())
            } catch (e: Exception) {
                messager.printMessage(Diagnostic.Kind.ERROR, TAG + e.message)
            }
        }
    }
}