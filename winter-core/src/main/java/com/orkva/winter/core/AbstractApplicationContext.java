package com.orkva.winter.core;

import com.orkva.winter.core.exception.NoBeanDefinitionException;

import java.util.HashMap;

/**
 * AbstractApplicationContext
 *
 * @author Shepherd Xie
 * @version 2023/3/7
 */
public abstract class AbstractApplicationContext implements BeanFactory {

    private final HashMap<String, Object> beanInstances = new HashMap<>();

    @Override
    public Object getBean(String beanName) {
        if (!containsBean(beanName)) {
            throw new NoBeanDefinitionException();
        }

        return beanInstances.get(beanName);
    }

    @Override
    public boolean containsBean(String beanName) {
        return beanInstances.containsKey(beanName);
    }
}
