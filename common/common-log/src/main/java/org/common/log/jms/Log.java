package org.common.log.jms;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日志类
 * @author luofan
 * @date 2019.10.29 17:55:21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection="my")
public class Log extends LoginCacheDTO implements Serializable {

	private static final long serialVersionUID = -5030264507504968057L;

	/**
	 * 来源服务
	 */
	public String serverName;
	
	/**
	 * 请求方法
	 */
	private String method;
	
	/**
	 * 请求目标类
	 */
	private String target;
	
	/**
	 * 请求入参
	 */
	private Map<String, Object> parameters;
	
	/**
	 * 请求路径
	 */
	private String url;
	
	/**
	 * 登录人信息
	 */
//	private LoginCacheDTO user;
	
	/**
	 * 创建时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	
	/**
	 * 创建时间毫秒数
	 */
	private Long createTimeMillis;
	
	private Integer flag;
}
