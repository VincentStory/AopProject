# AopProject
深入理解Aop面向切面编程，熟悉AspectJ使用用法

Android AOP 面向切面编程实现方案

#### 1.首先要了解什么是AOP
AOP是Aspect Oriented Programming的缩写，即“面向切面编程”，就是针对同一类问题的统一处理，通过预编译方式和运行期动态代理实现程序功能的统一维护的一种技术。AOP是Spring框架中的一个重要内容，是函数式编程的一种衍生范型。利用AOP可以对业务逻辑的各个部分进行隔离，从而使得业务逻辑各部分之间的耦合度降低，提高程序的可重用性，同时提高了开发的效率。

#### 2.了解一下AspectJ
AspectJ是对AOP编程思想的一个实践方案，AOP是一种思想，它在各个语言中都有各自的实践方案，那AspectJ是Java中比较火的实践方案，它可以完全兼容Java。当然，除了使用AspectJ特殊的语言外，AspectJ还支持原生的Java，只要加上对应的AspectJ注解就好。所以，使用AspectJ有两种方法：

（1）完全使用AspectJ的语言，和Java几乎一样，也能在AspectJ中调用Java的任何类库。AspectJ只是多了一些关键词。
（2）使用纯Java语言开发，然后使用AspectJ注解，简称@AspectJ。

基础概念：
Aspect 切面：切面是切入点和通知的集合。

PointCut 切入点：切入点是指那些通过使用一些特定的表达式过滤出来的想要切入Advice的连接点。

Advice 通知：通知是向切点中注入的代码实现方法。

Joint Point 连接点：所有的目标方法都是连接点.

Weaving 编织：主要是在编译期使用AJC将切面的代码注入到目标中, 并生成出代码混合过的.class的过程

#### 3.具体实践
(1)首先在project中的build.gradle中配置AspectJ，如下：
```
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath "com.android.tools.build:gradle:7.0.2"

        classpath 'org.aspectj:aspectjtools:1.8.9'
        classpath 'org.aspectj:aspectjweaver:1.8.9'
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```
（2）在app的build.gradle中配置AspectJ编译器
```
buildscript { // 编译时用Aspect专门的编译器，不再使用传统的javac
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'org.aspectj:aspectjtools:1.8.9'
        classpath 'org.aspectj:aspectjweaver:1.8.9'
    }
}

plugins {
    id 'com.android.application'
}

android {
    compileSdk 31

    defaultConfig {
        applicationId "com.vincent.aop.project"
        minSdk 21
        targetSdk 31
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main

final def log = project.logger
final def variants = project.android.applicationVariants

variants.all { variant ->
    if (!variant.buildType.isDebuggable()) {
        log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
        return;
    }

    JavaCompile javaCompile = variant.javaCompile
    javaCompile.doLast {
        String[] args = ["-showWeaveInfo",
                         "-1.8",
                         "-inpath", javaCompile.destinationDir.toString(),
                         "-aspectpath", javaCompile.classpath.asPath,
                         "-d", javaCompile.destinationDir.toString(),
                         "-classpath", javaCompile.classpath.asPath,
                         "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]
        log.debug "ajc args: " + Arrays.toString(args)

        MessageHandler handler = new MessageHandler(true);
        new Main().run(args, handler);
        for (IMessage message : handler.getMessages(null, true)) {
            switch (message.getKind()) {
                case IMessage.ABORT:
                case IMessage.ERROR:
                case IMessage.FAIL:
                    log.error message.message, message.thrown
                    break;
                case IMessage.WARNING:
                    log.warn message.message, message.thrown
                    break;
                case IMessage.INFO:
                    log.info message.message, message.thrown
                    break;
                case IMessage.DEBUG:
                    log.debug message.message, message.thrown
                    break;
            }
        }
    }
}


dependencies {

    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'com.google.android.material:material:1.3.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
    testImplementation 'junit:junit:4.+'
    androidTestImplementation 'androidx.test.ext:junit:1.1.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'

    implementation 'org.aspectj:aspectjrt:1.8.13'
}
```
(3)创建切面AspectJ，具体处理切面统一事件
```

@Aspect // 定义切面类
public class LoginCheckAspect {

    private final static String TAG = "aop_tag >>> ";

    // 1、应用中用到了哪些注解，放到当前的切入点进行处理（找到需要处理的切入点）
    // execution，以方法执行时作为切点，触发Aspect类
    // * *(..)) 可以处理ClickBehavior这个类所有的方法
    @Pointcut("execution(@com.vincent.aop.project.annotation.LoginCheck * *(..))")
    public void pointCut() {
    }

    // 2、对切入点如何处理
    @Around("pointCut()")
    public Object joinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        //1.获取切入点所在目标对象
        Object targetObj =joinPoint.getTarget();
        Log.e(TAG, "类名："+targetObj.getClass().getName());
        // 2.获取切入点方法的名字
        String methodName = joinPoint.getSignature().getName();
        Log.e(TAG, "切入方法名字："+methodName);
        // 3. 获取方法上的注解
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null)
        {
            LoginCheck apiLog=  method.getAnnotation(LoginCheck.class);
            Log.e(TAG, "切入方法注解的title:"+apiLog.title());
        }

        //4. 获取方法的参数
        Object[] args = joinPoint.getArgs();
        for(Object o :args){
            Log.e(TAG, "切入方法的参数："+o);
        }

        Context context = (Context) joinPoint.getThis();
        if (MyApplication.isLogin()) {
            Log.e(TAG, "检测到已登录！跳转其他页面");
            return joinPoint.proceed();
        } else {
            Log.e(TAG, "检测到未登录！");
            Toast.makeText(context, "请先登录！", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, LoginActivity.class));
            return null; // 不再执行方法（切入点）
        }
    }
}
```
上面除了登录检查的逻辑还添加了一些获取类名，方法名，方法参数值，注解参数值的方法，根据项目需要来具体选择是否获取。
(3)在Activity页面使用AspectJ
```
  @LoginCheck
    public void myInfomation(View view) {
        Log.e(TAG, "开始跳转到 -> 我的资料 Activity");
    }

    @LoginCheck
    public void myMoney(View view) {
        Log.e(TAG, "开始跳转到 -> 我的余额 Activity");
    }

    public void myScore(View view) {
        checkLogin("参数1", "参数2");
    }

    @LoginCheck(title = "登录检查", isSaveRequestData = false)
    public void checkLogin(String str, String str2) {
        Log.e(TAG, "开始跳转到 -> 我的积分 Activity");
    }

```
点击跳转之后打印信息如下：
```
2021-11-15 10:53:43.930 6665-6665/com.vincent.aop.project E/aop_tag >>>: 类名：com.vincent.aop.project.MainActivity
2021-11-15 10:53:43.931 6665-6665/com.vincent.aop.project E/aop_tag >>>: 切入方法名字：checkLogin
2021-11-15 10:53:43.932 6665-6665/com.vincent.aop.project E/aop_tag >>>: 切入方法注解的title:登录检查
2021-11-15 10:53:43.933 6665-6665/com.vincent.aop.project E/aop_tag >>>: 切入方法注解的isSaveRequestData：false
2021-11-15 10:53:43.933 6665-6665/com.vincent.aop.project E/aop_tag >>>: 切入方法的参数：参数1
2021-11-15 10:53:43.933 6665-6665/com.vincent.aop.project E/aop_tag >>>: 切入方法的参数：参数2
2021-11-15 10:53:43.933 6665-6665/com.vincent.aop.project E/aop_tag >>>: 检测到未登录！
```

##### 4.总结：
通过上面的例子我们可以发现，AOP解决方案可以使用很简洁的代码实现我们想要的效果，而且对原有代码毫无入侵性，理解这一原理，可以在工作中更好的以架构师的思维去编写代码。
