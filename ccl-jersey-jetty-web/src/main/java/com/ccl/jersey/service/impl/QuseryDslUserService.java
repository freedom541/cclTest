package com.ccl.jersey.service.impl;

import com.ccl.jersey.codegen.Filter;
import com.ccl.jersey.codegen.Sort;
import com.ccl.jersey.service.QLUserService;
import com.ccl.querydsl.data.model.User;
import com.ccl.querydsl.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by ccl on 17/8/14.
 */
@Service("qlUserService")
public class QuseryDslUserService implements QLUserService {
    @Autowired
    UserRepository userRepository;

    @Override
    public List<User> findAllUser() {
        return userRepository.findAllByModel(Filter.condition(), Sort.condition());
    }

    @Override
    public void addUser() {

    }

    @Override
    public void updateUser() {

    }

    @Override
    public void deleteUser() {

    }
}
