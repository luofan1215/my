package org.test.server.controller;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.common.log.jms.Log;
import org.common.utils.MongoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.test.server.entity.Case1DTO;
import org.test.server.entity.Case2DTO;

import com.alibaba.fastjson.JSON;

@RestController
@RequestMapping("/test")
public class TestController {

	@Autowired
	private MongoUtils mongoUtils;

	@PostMapping("/case1")
	public String case1(@RequestBody Case1DTO case1) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Criteria criteria = Criteria.where("createTime").gte(format.parse(case1.getStartTime()))
				.lte(format.parse(case1.getEndTime())).and("flag").is(1);
		Query query=new Query();
		query.addCriteria(criteria);
		List<Log> list = mongoUtils.findList(query, Log.class);
		Map<String, Long> map = list.stream().collect(Collectors.groupingBy(Log::getName,Collectors.counting()));
		System.out.println("[登录统计]");
		for(Map.Entry<String, Long> entry:map.entrySet()) {
			System.out.println(MessageFormat.format("登录者：{0}，登录次数：{1}",entry.getKey(),entry.getValue()));
		}
		return "SUCCESS";
	}

	@PostMapping("/case2")
	public String case2(@RequestBody Case2DTO case2) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Criteria criteria = Criteria.where("createTime").gte(format.parse(case2.getStartTime()))
				.lte(format.parse(case2.getEndTime())).and("name").is(case2.getName()).and("flag").ne(1);
		Query query=new Query();
		query.addCriteria(criteria).with(Sort.by(Sort.Order.asc("createTimeMillis")));
		List<Log> list = mongoUtils.findList(query, Log.class);
		System.out.println("[方法调用情况]");
		list.forEach(log->{
			if(log.getTarget().contains("Controller")) {
				System.out.println(MessageFormat.format("登录者：{0}，控制器方法：{1}.{2}，参数：{3}，时间：{4}，服务名：{5}，请求地址：{6}",
						log.getName(),log.getTarget(),log.getMethod(),
						JSON.toJSONString(log.getParameters()),log.getCreateTime(),log.getServerName(),log.getUrl()));
			} else {
				System.out.println(MessageFormat.format("登录者：{0}，其他方法：{1}.{2}，参数：{3}，时间：{4}",
						log.getName(),log.getTarget(),log.getMethod(),JSON.toJSONString(log.getParameters()),log.getCreateTime()));
			}
		});
		return "SUCCESS";
	}
}
