package com.ccl.jersey.repository;

import com.ccl.jersey.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by ccl on 17/8/15.
 */
@Repository
public class SimpleUserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<User> findAll() {

        // Find all customers, thanks Java 8, you can create a custom RowMapper like this :
        List<User> result = jdbcTemplate.query(
                "SELECT id, name FROM user",
                (rs, rowNum) -> new User(rs.getInt("id"),rs.getString("name"))
        );

        return result;

    }

    public void addUser(String name) {
        int temp = jdbcTemplate.update("INSERT INTO user(name) VALUES (?)", name);
        if (temp > 0) {
            System.out.println("插入成功！");

        }else{
            System.out.println("插入失败");
        }
    }

    public void updateUser(int id, String name) {
        int temp = jdbcTemplate.update("UPDATE user SET name = ? where id = ?", name, id);
        if (temp > 0) {
            System.out.println("修改成功！");

        }else{
            System.out.println("修改失败");
        }
    }

    public void deleteUser(int id) {
        int temp = jdbcTemplate.update("DELETE FROM user where id = ?", id);
        if (temp > 0) {
            System.out.println("删除成功！");

        }else{
            System.out.println("删除失败");
        }
    }
}
