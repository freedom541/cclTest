package com.ccl.jersey.service;

import com.ccl.jersey.model.User;

import java.util.List;

/**
 * Created by ccl on 17/8/14.
 */
public interface UserService {
    List<User> getUser();
    List<User> getUserData();
    List<User> findAllUser();
    void addUser();
}
