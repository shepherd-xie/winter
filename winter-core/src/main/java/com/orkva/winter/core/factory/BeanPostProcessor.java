package com.orkva.winter.core.factory;

/**
 * BeanPostProcessor
 *
 * @author Shepherd Xie
 * @version 2023/4/18
 */
public interface BeanPostProcessor {

    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

}
