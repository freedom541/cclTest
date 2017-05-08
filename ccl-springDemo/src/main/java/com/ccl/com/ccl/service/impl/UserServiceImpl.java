package com.ccl.com.ccl.service.impl;

import com.ccl.com.ccl.service.UserService;
import org.springframework.stereotype.Service;

/**
 * Created by ccl on 17/4/25.
 */
@Service
public class UserServiceImpl implements UserService {
    @Override
    public void say() {
        System.out.println("jjj");
    }
}
