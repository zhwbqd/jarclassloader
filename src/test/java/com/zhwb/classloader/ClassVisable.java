package com.zhwb.classloader;

import com.google.common.io.ByteStreams;

import java.io.FileInputStream;

/**
 * @author zhangwenbin
 * @since 2016/2/5.
 */
public class ClassVisable {
    public static void main(String[] args) throws Exception {
        FileInputStream inputStream1 = new FileInputStream("javasource/Domain.class");
        CustomClassLoaderV1 loader1 = new CustomClassLoaderV1(Thread.currentThread().getContextClassLoader());
        CustomClassLoaderV1 customClassLoaderV1 = loader1.loadSingleClassBytes("com.zhwb.learn.groovy.Domain", ByteStreams.toByteArray(inputStream1));

        inputStream1 = new FileInputStream("javasource/Utils.class");
        CustomClassLoaderV1 customClassLoaderV11 = loader1.loadSingleClassBytes("com.zhwb.learn.groovy.Utils", ByteStreams.toByteArray(inputStream1));

        Class<?> aClass = loader1.loadClass("com.zhwb.learn.groovy.Domain");
        aClass.getDeclaredMethod("doIt").invoke(aClass.newInstance());

        System.out.println(loader1.loadClass("com.zhwb.learn.groovy.Domain").getClassLoader());
        System.out.println(loader1.loadClass("com.zhwb.learn.groovy.Domain").getClassLoader());



    }



}
