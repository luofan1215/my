package org.common.log.jms;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;

import lombok.extern.slf4j.Slf4j;

/**
 * 日志客户端
 * 
 * @author luofan
 *
 */
@Slf4j
@Aspect
public class LogClient {
	
	private final DefaultMQProducer producer = new DefaultMQProducer(LogConfig.LOG_PRODUCER_GROUP);

	@Value("${rocketmq.namesrvAddr}")
	private String namesrvAddr;

	@Value("${spring.application.name}")
	private String serverName;

	@Pointcut("execution(public * org.test..controller.*.*(..))||execution(public * org.test..service.*.*(..))||execution(public * org.test..mapper.*.*(..))")
	public void pointCut1() {
	}
	
	@Pointcut("execution(public * org.test..controller.LoginController.login(..)))")
	public void pointCut2() {
	}

	@Before("pointCut1()")
	public void doBefore(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		List<String> names = Arrays.asList(signature.getParameterNames());
		List<Object> args = Arrays.asList(joinPoint.getArgs());
		String className = joinPoint.getTarget().getClass().getName();
		Log log = new Log();
		/* 此处在实际使用时，需要从Redis中读取 */
		LoginCacheDTO currentUser = new LoginCacheDTO();
		currentUser.setCompany("huiyi2");
		currentUser.setName("admin");
		BeanUtils.copyProperties(currentUser, log);
		log.setCreateTime(new Date());
		log.setCreateTimeMillis(System.currentTimeMillis());
		log.setTarget(getRealName(joinPoint));
		log.setMethod(joinPoint.getSignature().getName());
		Map<String, Object> params = names.stream()
				.collect(Collectors.toMap(name -> name, name -> args.get(names.indexOf(name))));
		log.setParameters(params);
		if (className.contains("controller")) {
			RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
			HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
			log.setServerName(serverName);
			log.setUrl(request.getRequestURL().toString());
		}
		sendMessage(log);
	}
	
	@AfterReturning("pointCut2()")
	public void doAfterReturning(JoinPoint joinPoint) {
		Log log = new Log();
		LoginCacheDTO currentUser = (LoginCacheDTO)joinPoint.getArgs()[0];
		BeanUtils.copyProperties(currentUser, log);
		log.setCreateTime(new Date());
		log.setCreateTimeMillis(System.currentTimeMillis());
		log.setFlag(1);
		sendMessage(log);
	}

	private String getRealName(JoinPoint joinPoint) {
		Class<? extends Object> clazz = joinPoint.getTarget().getClass();
		String realName = clazz.getName();
		if (!realName.contains("controller") && !realName.contains("service")) {
			for (Class<?> tmp : clazz.getInterfaces()) {
				if (tmp.getName().contains("mapper")) {
					realName = tmp.getName();
					break;
				}
			}
		}
		return realName;
	}

	@PostConstruct
	public void start() {
		try {
			log.info("日志模块启动MQ：  生产者");
			producer.setNamesrvAddr(namesrvAddr);
			producer.start();
		} catch (MQClientException e) {
			log.error("日志模块启动MQ： 生产者失败：{}-{}", e.getResponseCode(), e.getErrorMessage());
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void sendMessage(Object object) {
		try {
			String data = JSON.toJSONString(object);
			log.info("发送日志消息内容 ： {}", data);
			byte[] messageBody = data.getBytes("UTF-8");
			Message message = new Message(LogConfig.LOG_TOPIC, LogConfig.LOG_TAG, messageBody);
			producer.send(message, new SendCallback() {
				@Override
				public void onSuccess(SendResult sendResult) {
					log.info("日志模块MQ：生产者发送消息 {}", sendResult);
				}

				@Override
				public void onException(Throwable throwable) {
					log.error(throwable.getMessage(), throwable);
				}
			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@PreDestroy
	public void stop() {
		producer.shutdown();
		log.info("日志模块MQ：关闭生产者");
	}
}
