package com.ccl.jersey.action;

/**
 * Created by ccl on 17/8/10.
 */

import com.alibaba.fastjson.JSON;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;



@Path("/leo")
public class JerseyDemo {

    private String name = "saltwater_leo";

    @GET
    @Path("/non_spring/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public String test1(@PathParam("name") String name){
        if(name !=null && !"".equals(name)){
            this.name = name ;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", this.name);
        return JSON.toJSONString(map);
    }


    @GET
    @Path("/spring")
    @Produces(MediaType.APPLICATION_JSON)
    public String test2( ){
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "zhangsan");
        return JSON.toJSONString(map);
    }
}
