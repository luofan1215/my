package org.test.server;

import org.common.log.annotation.EnableLogClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringCloudApplication	
@ComponentScan({"org.common","org.test"})
@MapperScan("org.test.server.mapper")
@EnableLogClient
public class TestServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestServerApplication.class, args);
    }

}
