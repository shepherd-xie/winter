package com.orkva.winter.core.service;

import com.orkva.winter.core.annotation.Autowired;
import com.orkva.winter.core.annotation.Component;
import com.orkva.winter.core.aware.BeanNameAware;
import com.orkva.winter.core.repository.UserRepository;

/**
 * UserService
 *
 * @author Shepherd Xie
 * @version 2023/4/17
 */
@Component("userService")
public class UserService implements BeanNameAware {

    @Autowired
    private UserRepository userRepository;

    private String beanName;

    public void test() {
        System.out.println(userRepository);
        System.out.println(beanName);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

}
