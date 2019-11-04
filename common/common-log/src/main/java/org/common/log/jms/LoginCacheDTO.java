package org.common.log.jms;

import lombok.Data;

import java.io.Serializable;

import org.springframework.data.annotation.Transient;

/** 
 * 用户信息缓存类
 * @author luofan
 */
@Data
public class LoginCacheDTO implements Serializable {

    private static final long serialVersionUID = -1630111058142144509L;
    //id
    @Transient
    private Integer id;
    //用户名
    private String userName;
    //token
    private String token;
    //当前组
    private Integer groupId;
    //当前组名
    private String groupName;
    //手机号
    private String phone;
    //邮箱
    private String email;
    //是否第一次登陆
    private Integer number;
    //头像地址
    private String img;
    //账户类型 type
    private Integer type;
    //公司
    private String company;
    //联系人
    private String name;
    //租户标识
    private String tenant;
}
