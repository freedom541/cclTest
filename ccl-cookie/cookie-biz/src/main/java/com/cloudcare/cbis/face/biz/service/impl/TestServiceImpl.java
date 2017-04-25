package com.cloudcare.cbis.face.biz.service.impl;

import com.cloudcare.cbis.face.biz.service.TestService;
import org.springframework.stereotype.Service;

/**
 * Created by ccl on 17/4/7.
 */
@Service
public class TestServiceImpl implements TestService {

    public String hello(String name) {
        return "你好 " + name;
    }

}
