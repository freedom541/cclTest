package com.ccl.jersey.service.impl;

import com.ccl.jersey.model.User;
import com.ccl.jersey.repository.UserRepository;
import com.ccl.jersey.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by ccl on 17/8/14.
 */
@Service
public class DefultUserService implements UserService{
    @Autowired
    UserRepository userRepository;

    @Override
    public List<User> getUser() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i<5; i++){
            users.add(new User(i, "name"+i));
        }
        return users;
    }


    @Override
    public List<User> getUserData() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/*.xml");
        DataSource ds = (DataSource) ctx.getBean("dataSource");
        java.sql.Connection conn = null;
        try {
            conn = ds.getConnection();
            String sql = "select * from user";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt(1));
                user.setName(rs.getString(2));
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> findAllUser() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void addUser() {
        String uuid = UUID.randomUUID().toString().substring(0,6);
        userRepository.addUser(uuid);
    }
}
