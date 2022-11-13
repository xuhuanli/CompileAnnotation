### APT
运行时注解是在程序运行时通过反射获取注解然后处理的，编译时注解是程序在编译期间通过注解处理器处理的

#### APT技术的作用
用来生成模版代码

### APT的两个要素
1. 注解
2. 注解处理器

```
kapt "com.google.dagger:hilt-compiler:2.44"
annotationProcessor "com.google.dagger:hilt-compiler:2.44"
```
kapt/annotationProcessor关键字依赖了一个库，javac在<u>编译的过程中</u>就会去这个库中找符合注解处理器规则的类。 找到这个类后就把这个类认为是一个注解处理器。 但是一个大型项目中 往往会存在许多个apt库 那么这么多个处理器它是怎么被加载出来的

#### SPI
>Java SPI全称Service Provider Interface，是Java提供的一套用来被第三方实现或者扩展的API，它可以用来启用框架扩展和替换组件。实际上是“基于接口的编程＋策略模式＋配置文件”组合实现的动态加载机制.
>
>1.定义一个接口文件
2.写出多个该接口文件的实现
3.在 src/main/resources/ 下建立 /META-INF/services 目录， 新增一个以接口命名的文件 , 内容是要接口的实现类全路径
4.使用ServiceLoader类 来获取到这些实现的接口

写法有点像什么 aidl
相比AIDL的方式，缺点是不支持跨进程，需要在接口实现的地方自己处理。

#### AutoService
他可以帮我们在编译的时候动态去生成这些东西
通过`@AutoService`省下最麻烦的第3步

#### javac是如何调用注解处理器的
javac源码 idea中打开

1. com.sun.tools.javac.Main
2. 调用compile方法 返回Result枚举类 注意processors参数null
3. 最后走到五个参数的compile方法 找到 `comp = JavaCompiler.instance(context);`和`comp.compile(fileObjects,
   classnames.toList(),
   processors);`
4. 进到JavaCompiler类中的compile方法里面 找到`initProcessAnnotations(processors);`
5. 在initProcessAnnotations方法中找到`procEnvImpl.setProcessors(processors);`然后进入`initProcessorIterator`
6. 最后找到`new ServiceIterator(processorClassLoader, log);`
   由于processorNames == null，进到else
   然后看到`ServiceLoader.load(Processor.class, classLoader);`
   上面6步就是processor的加载
7. 加载完成后返回 继续执行 `discoveredProcs = new DiscoveredProcessors(processorIterator);` 然后在JavaCompiler类中继续执行 找到`procEnvImpl.doProcessing`
8. 找到`round.run(false, false);`观察注释 明确这里执行processors 内部执行`discoverAndRunProcs`
9. 找到`callProcessor(ps.processor, typeElements, renv);`这个方法内调用了process方法 传入了Set<? extends TypeElement> tes,和RoundEnvironment
   上面3步就是process方法的执行
10. 观察do-while的条件 moreToDo表示是否有新的文件生成 errorStatus表示错误状态 第一次执行的是否moreToDO为true 如果不出错 errorStatus为false 满足while条件 继续执行 第二次
11. 第二次的时候没有新的文件生成 跳出while
12. 跳出while后继续执行 有看到一行代码
    `round.run(true, errorStatus);`和注释`// run last round`
13. 所以apt里面process方法执行次数是2+N

### 核心process方法

* RoundEnvironment
  容器 装了所有我们声明这个注解处理器能处理的注解的内容(类、方法、成员变量)
  在用反射的时候 我们用Class Method Field来表示类方法和成员变量
  在注解处理器中也有一个类似的东西 Element