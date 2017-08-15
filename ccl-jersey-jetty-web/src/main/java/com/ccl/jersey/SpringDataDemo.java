package com.ccl.jersey;

import com.ccl.jersey.model.User;
import com.ccl.jersey.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by ccl on 17/8/14.
 */
@Path("/data")
public class SpringDataDemo {
    @Autowired
    private UserService userService;

    @Autowired
    private ServletResponse response;
    @Autowired
    private ServletRequest request;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUser(){
        return userService.getUserData();
    }


    @GET
    @Path("/findall")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> findAll(){
        return userService.findAllUser();
    }

    @PUT
    @Path("/user")
    public void add(){
        userService.addUser();
    }
}
