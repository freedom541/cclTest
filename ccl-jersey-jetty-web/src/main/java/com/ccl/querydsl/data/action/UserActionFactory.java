package com.ccl.querydsl.data.action;

import com.ccl.querydsl.data.repository.UserRepository;
import com.ccl.jersey.codegen.Label;
import org.springframework.validation.annotation.Validated;
import com.ccl.jersey.codegen.ParentModule;
import org.springframework.beans.factory.annotation.Autowired;
import com.ccl.querydsl.data.entity.EUser;
import org.springframework.stereotype.Controller;
import com.ccl.querydsl.data.model.User;
import com.ccl.jersey.codegen.AbstractCrudModelActionFactory;
import com.ccl.jersey.codegen.DataAdminModule;

/**
 * UserActionFactory is a Codegen action factory type
 */
@ParentModule(DataAdminModule.class)
@Label("User数据管理")
@Validated({})
@Controller("UserDataAdmin")
public class UserActionFactory extends AbstractCrudModelActionFactory<UserRepository, User, EUser, Integer> {

    @Autowired
    public UserActionFactory(UserRepository userRepository) {
        super(userRepository);
    }

}

