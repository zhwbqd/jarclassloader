package com.zhwb.classloader;

import com.google.common.io.ByteStreams;

import java.io.FileInputStream;

import static java.lang.Thread.sleep;

/**
 * @author zhangwenbin
 * @since 2016/2/5.
 */
public class ClassReload {
    public static void main(String[] args) throws Exception {
        //reload test

        FileInputStream inputStream1 = new FileInputStream("javasource/Utils.class");
        CustomClassLoader loader1 = new CustomClassLoader(Thread.currentThread().getContextClassLoader());
        loader1.add("com.zhwb.learn.groovy.Utils", ByteStreams.toByteArray(inputStream1));
        Class<?> aClass = loader1.reloadClass("com.zhwb.learn.groovy.Utils");
        System.out.println(aClass.getDeclaredMethod("isOk", new Class[]{String.class}).invoke(aClass, new Object[]{"fuck"}));

        FileInputStream inputStream = new FileInputStream("javasource/v1/Utils.class");
        loader1.add("com.zhwb.learn.groovy.Utils", ByteStreams.toByteArray(inputStream));
        aClass = loader1.reloadClass("com.zhwb.learn.groovy.Utils");
        System.out.println(aClass.getDeclaredMethod("isOk", new Class[]{String.class}).invoke(aClass, new Object[]{"fuck"}));


        //perm gen oom test

//        for (int i = 0; i < Integer.MAX_VALUE; i++) {
//            System.out.println(i);
//            aClass = loader1.reloadClass("com.zhwb.learn.groovy.Utils");
//        }
//        sleep(Long.MAX_VALUE);
    }



}
