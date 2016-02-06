package com.zhwb.classloader;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author zhangwenbin
 * @since 2016/2/5.
 */
public class ClassResources {
    public static void main(String[] args) throws Exception {

        InputStream inputStream1 = new FileInputStream("D:\\projects\\riskassessment-plugin\\riskassessment-socialdb\\target\\riskassessment-socialdb-1.0-SNAPSHOT.jar");

        CustomClassLoaderV1 customClassLoader = CustomClassLoaderV1.GLOBAL_CLASSLOADER;
        Class<?> aClass = customClassLoader.loadJarStream(inputStream1).loadClass("com.iqiyi.riskassessment.plugin.socialdb.comparator.SocialDBComparatorImpl");

        System.out.println(aClass.getMethod("compareWithSocialDB", new Class[]{String.class, String.class}).invoke(aClass, new String[]{"kid_zhwb", "zhwb123"}));
        System.out.println(aClass.getMethod("compareWithSocialDB", new Class[]{String.class, String.class}).invoke(aClass, new String[]{"kid_zhwb", "zhwb123"}));


//        InputStream inputStream1 = new FileInputStream("D:\\projects\\riskassessment-plugin\\riskassessment-socialdb\\target\\riskassessment-socialdb-1.0-SNAPSHOT.jar");
//
//        CustomClassLoader customClassLoader = CustomClassLoader.GLOBAL_CLASSLOADER;
//        customClassLoader.add(inputStream1);
//        Class<?> aClass = customClassLoader.reloadClass("com.iqiyi.riskassessment.plugin.socialdb.comparator.SocialDBComparatorImpl");
//
//        System.out.println(aClass.getMethod("compareWithSocialDB", new Class[]{String.class, String.class}).invoke(aClass, new String[]{"kid_zhwb", "zhwb123"}));
//        System.out.println(aClass.getMethod("compareWithSocialDB", new Class[]{String.class, String.class}).invoke(aClass, new String[]{"kid_zhwb", "zhwb123"}));

//        InputStream inputStream1 = new FileInputStream("D:\\projects\\riskassessment-plugin\\riskassessment-socialdb\\target\\riskassessment-socialdb-1.0-SNAPSHOT.jar");
//
//        NormalCustomClassLoader customClassLoader = NormalCustomClassLoader.GLOBAL_CLASSLOADER;
//        customClassLoader.add(inputStream1);
//        Class<?> aClass = customClassLoader.loadClass("com.iqiyi.riskassessment.plugin.socialdb.comparator.SocialDBComparatorImpl");
//
//        System.out.println(aClass.getMethod("compareWithSocialDB", new Class[]{String.class, String.class}).invoke(aClass, new String[]{"kid_zhwb", "zhwb123"}));

    }


}
