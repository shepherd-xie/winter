package com.orkva.winter.core;

import com.orkva.winter.core.exception.NoBeanException;

/**
 * BeanFactory
 *
 * @author Shepherd Xie
 * @version 2023/3/7
 */
public interface BeanFactory {

    Object getBean(String beanName) throws NoBeanException;

    boolean containsBean(String beanName);

}
