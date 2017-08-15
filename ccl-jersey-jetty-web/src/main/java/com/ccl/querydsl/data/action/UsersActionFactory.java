package com.ccl.querydsl.data.action;

import com.ccl.jersey.codegen.Label;
import org.springframework.validation.annotation.Validated;
import com.ccl.querydsl.data.model.Users;
import com.ccl.jersey.codegen.ParentModule;
import org.springframework.beans.factory.annotation.Autowired;
import com.ccl.querydsl.data.repository.UsersRepository;
import org.springframework.stereotype.Controller;
import com.ccl.jersey.codegen.AbstractCrudModelActionFactory;
import com.ccl.querydsl.data.entity.EUsers;
import com.ccl.jersey.codegen.DataAdminModule;

/**
 * UsersActionFactory is a Codegen action factory type
 */
@ParentModule(DataAdminModule.class)
@Label("Users数据管理")
@Validated({})
@Controller("UsersDataAdmin")
public class UsersActionFactory extends AbstractCrudModelActionFactory<UsersRepository, Users, EUsers, Integer> {

    @Autowired
    public UsersActionFactory(UsersRepository usersRepository) {
        super(usersRepository);
    }

}

