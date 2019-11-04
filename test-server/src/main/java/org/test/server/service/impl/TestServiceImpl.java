package org.test.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.test.server.entity.Test;
import org.test.server.mapper.TestMapper;
import org.test.server.service.TestService;

@Service
public class TestServiceImpl implements TestService {

	@Autowired
	private TestMapper testMapper;
	
	@Override
	public void insert(Test test) {
		Test one = testMapper.findOne(test.getId());
		if(ObjectUtils.isEmpty(one)) {
			testMapper.insert(test);
		}
	}

}
