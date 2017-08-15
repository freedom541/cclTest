package com.ccl.jersey.service;

import com.ccl.querydsl.data.model.User;

import java.util.List;

/**
 * Created by ccl on 17/8/14.
 */
public interface QLUserService {
    List<User> findAllUser();
    void addUser();
    void updateUser();
    void deleteUser();

}
