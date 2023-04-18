package com.orkva.winter.core;

/**
 * WinterApplication
 *
 * @author Shepherd Xie
 * @version 2023/4/17
 */
public class WinterApplicationTest {
    public static void main(String[] args) {

        AnnotationApplicationContext annotationApplicationContext = new AnnotationApplicationContext(AppConfig.class);

        Object userService1 = annotationApplicationContext.getBean("userService");
        Object userService2 = annotationApplicationContext.getBean("userService");
        Object userService3 = annotationApplicationContext.getBean("userService");

        System.out.println(userService1);
        System.out.println(userService2);
        System.out.println(userService3);
    }
}
