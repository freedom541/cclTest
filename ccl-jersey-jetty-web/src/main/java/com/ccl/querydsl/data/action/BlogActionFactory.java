package com.ccl.querydsl.data.action;

import com.ccl.jersey.codegen.Label;
import org.springframework.validation.annotation.Validated;
import com.ccl.querydsl.data.repository.BlogRepository;
import com.ccl.jersey.codegen.ParentModule;
import org.springframework.beans.factory.annotation.Autowired;
import com.ccl.querydsl.data.model.Blog;
import com.ccl.querydsl.data.entity.EBlog;
import org.springframework.stereotype.Controller;
import com.ccl.jersey.codegen.AbstractCrudModelActionFactory;
import com.ccl.jersey.codegen.DataAdminModule;

/**
 * BlogActionFactory is a Codegen action factory type
 */
@ParentModule(DataAdminModule.class)
@Label("Blog数据管理")
@Validated({})
@Controller("BlogDataAdmin")
public class BlogActionFactory extends AbstractCrudModelActionFactory<BlogRepository, Blog, EBlog, Integer> {

    @Autowired
    public BlogActionFactory(BlogRepository blogRepository) {
        super(blogRepository);
    }

}

