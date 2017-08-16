package com.ccl.jersey.service.impl;

import com.ccl.jersey.codegen.Filter;
import com.ccl.jersey.codegen.Sort;
import com.ccl.jersey.service.QLUserService;
import com.ccl.querydsl.data.model.User;
import com.ccl.querydsl.data.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

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
        User user = new User();
        String uuid = UUID.randomUUID().toString().substring(0,6);
        user.setName(uuid);
        userRepository.createModel(user);
    }

    @Override
    public void updateUser() {
        User user = new User();
        String uuid = UUID.randomUUID().toString().substring(0,6);
        user.setName(uuid);
        user.setId(4);
        userRepository.updateModelWithNotNull(user);
    }

    @Override
    public void deleteUser() {
        Random r = new Random();
        int id = r.nextInt(40) + 5;
        System.out.println("id = " + id);
        userRepository.deleteModel(id);
    }
}
