package com.ccl.service;

import com.ccl.bean.User;
import com.ccl.mapper.UserMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("userService")

public class UserService extends BaseService<User> {
	private final static Logger log = Logger.getLogger(UserService.class);

	@Autowired
	private UserMapper<User> mapper;

	public UserMapper<User> getMapper() {
		return mapper;
	}


}
