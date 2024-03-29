package com.orkva.winter.core.factory;

/**
 * BeanNameAware
 *
 * @author Shepherd Xie
 * @version 2023/4/18
 */
public interface BeanNameAware extends Aware {

    void setBeanName(String beanName);

}
