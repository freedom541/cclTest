package com.cloudcare.cbis.face.biz.action;

import com.cloudcare.common.lang.annotation.Label;
import com.cloudcare.web.api.annotation.Action;
import com.cloudcare.web.api.support.AbstractActionFactory;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import com.cloudcare.cbis.face.biz.service.TestService;

@Controller
@Validated
@Label("Test")
public class TestActionFactory extends AbstractActionFactory {

    @Autowired
    private TestService testService;

    @Action
    @Label("Test")
    public String myTest(@Label("名字") @NotBlank String name) {
        return testService.hello(name);
    }

}
