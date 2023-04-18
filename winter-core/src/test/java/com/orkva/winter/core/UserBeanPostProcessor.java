package com.orkva.winter.core;

import com.orkva.winter.core.annotation.Component;
import com.orkva.winter.core.factory.BeanPostProcessor;
import com.orkva.winter.core.service.UserService;

/**
 * UserBeanPostProcessor
 *
 * @author Shepherd Xie
 * @version 2023/4/18
 */
@Component
public class UserBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("before");
        if ("userService".equals(beanName)) {
            ((UserService) bean).setName("USER-SERVICE");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("after");
        return bean;
    }

}
