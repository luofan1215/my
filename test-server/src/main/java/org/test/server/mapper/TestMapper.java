package org.test.server.mapper;

import org.test.server.entity.Test;

public interface TestMapper {
	
	public Test findOne(Integer id);
	
	public int insert(Test test);
	
}
