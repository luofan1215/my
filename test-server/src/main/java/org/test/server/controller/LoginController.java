package org.test.server.controller;

import org.common.log.jms.LoginCacheDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.test.server.entity.Test;
import org.test.server.service.TestService;

@RestController
@RequestMapping("/user")
public class LoginController {

	@Autowired
	private TestService testService;

	@PostMapping("/login")
	public String login(@RequestBody LoginCacheDTO dto) {
		return "SUCCESS";
	}
	
	@PostMapping("/insert")
	public String insert(@RequestBody Test test) {
		testService.insert(test);
		return "SUCCESS";
	}

}
