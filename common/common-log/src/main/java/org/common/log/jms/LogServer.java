package org.common.log.jms;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.common.utils.MongoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;

import lombok.extern.slf4j.Slf4j;

/**
 * 日志服务端
 * 
 * @author luofan
 *
 */
@Slf4j
public class LogServer implements MessageListenerConcurrently {

	private final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(LogConfig.LOG_CONSUMER_GROUP);

	@Value("${rocketmq.namesrvAddr}")
	private String namesrvAddr;

	@Autowired
	private MongoUtils mongoUtil;

	/**
	 * 初始化
	 */
	@PostConstruct
	public void start() {
		try {
			log.info("日志模块MQ：启动消费者");

			consumer.setNamesrvAddr(namesrvAddr);
			// 从消息队列头开始消费
			consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
			// 集群消费模式
			consumer.setMessageModel(MessageModel.CLUSTERING);
			// 订阅主题
			consumer.subscribe(LogConfig.LOG_TOPIC, LogConfig.LOG_TAG);
			// 消费线程池数量
			consumer.setConsumeThreadMin(5);
			consumer.setConsumeThreadMax(5);
			// 注册消息监听器
			consumer.registerMessageListener(this);
			// 启动消费端
			consumer.start();
		} catch (MQClientException e) {
			log.error("日志模块MQ：启动消费者失败：{}-{}", e.getResponseCode(), e.getErrorMessage());
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	/**
	 * 消费消息
	 */
	@Override
	public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext context) {
		try {
			for (MessageExt messageExt : list) {
				String messageBody = new String(messageExt.getBody(), "UTF-8");
				log.info("日志模块MQ：消费者接收新信息: {} {} {} {}", messageExt.getMsgId(), messageExt.getTopic(),
						messageExt.getTags(), messageBody);
				Log logInfo = JSON.parseObject(messageBody, Log.class);
				log.info("msg ====== {}", logInfo);
				mongoUtil.insert(logInfo);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
	}

	@PreDestroy
	public void stop() {
		if (consumer != null) {
			consumer.shutdown();
			log.error("日志模块MQ：关闭消费者");
		}
	}
}