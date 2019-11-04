package org.log.server;

import org.common.log.annotation.EnableLogServer;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringCloudApplication
@ComponentScan("org.common")
@EnableLogServer
public class LogServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogServerApplication.class, args);
    }

}
