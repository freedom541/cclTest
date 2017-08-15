package com.ccl.querydsl.data.action;

import com.ccl.jersey.codegen.Label;
import org.springframework.validation.annotation.Validated;
import com.ccl.querydsl.data.model.Userb;
import com.ccl.querydsl.data.repository.UserbRepository;
import com.ccl.jersey.codegen.ParentModule;
import org.springframework.beans.factory.annotation.Autowired;
import com.ccl.querydsl.data.entity.EUserb;
import org.springframework.stereotype.Controller;
import com.ccl.jersey.codegen.AbstractCrudModelActionFactory;
import com.ccl.jersey.codegen.DataAdminModule;

/**
 * UserbActionFactory is a Codegen action factory type
 */
@ParentModule(DataAdminModule.class)
@Label("Userb数据管理")
@Validated({})
@Controller("UserbDataAdmin")
public class UserbActionFactory extends AbstractCrudModelActionFactory<UserbRepository, Userb, EUserb, Integer> {

    @Autowired
    public UserbActionFactory(UserbRepository userbRepository) {
        super(userbRepository);
    }

}

