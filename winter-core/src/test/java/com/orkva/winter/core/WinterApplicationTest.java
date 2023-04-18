package com.orkva.winter.core;

import com.orkva.winter.core.service.UserService;

/**
 * WinterApplication
 *
 * @author Shepherd Xie
 * @version 2023/4/17
 */
public class WinterApplicationTest {
    public static void main(String[] args) {

        AnnotationApplicationContext annotationApplicationContext = new AnnotationApplicationContext(AppConfig.class);

        UserService userService = (UserService) annotationApplicationContext.getBean("userService");

        userService.test();
    }
}
