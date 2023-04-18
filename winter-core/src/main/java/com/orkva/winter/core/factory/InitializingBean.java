package com.orkva.winter.core.factory;

/**
 * InitializingBean
 *
 * @author Shepherd Xie
 * @version 2023/4/18
 */
public interface InitializingBean {

    void afterPropertiesSet() throws Exception;

}
