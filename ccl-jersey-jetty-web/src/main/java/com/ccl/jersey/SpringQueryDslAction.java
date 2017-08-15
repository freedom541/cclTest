package com.ccl.jersey;

import com.ccl.jersey.service.QLUserService;
import com.ccl.querydsl.data.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by ccl on 17/8/14.
 */
@Path("/querydsl")
public class SpringQueryDslAction {
    @Autowired
    private QLUserService userService;

    @Autowired
    private ServletResponse response;
    @Autowired
    private ServletRequest request;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> findAll(){
        return userService.findAllUser();
    }

    @PUT
    @Path("/user")
    public void add(){
        userService.addUser();
    }

    @POST
    @Path("/user")
    public void update(){
        userService.updateUser();
    }

    @DELETE
    @Path("/user")
    public void delete(){
        userService.deleteUser();
    }
}
