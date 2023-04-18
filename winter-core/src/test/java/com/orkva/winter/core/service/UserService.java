package com.orkva.winter.core.service;

import com.orkva.winter.core.annotation.Autowired;
import com.orkva.winter.core.annotation.Component;
import com.orkva.winter.core.factory.BeanNameAware;
import com.orkva.winter.core.factory.InitializingBean;
import com.orkva.winter.core.repository.UserRepository;

/**
 * UserService
 *
 * @author Shepherd Xie
 * @version 2023/4/17
 */
@Component("userService")
public class UserService implements BeanNameAware, InitializingBean {

    @Autowired
    private UserRepository userRepository;

    private String beanName;

    private String name;

    public void test() {
        System.out.println(userRepository);
        System.out.println(beanName);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("afterPropertiesSet");
    }

    public void setName(String name) {
        this.name = name;
    }
}
