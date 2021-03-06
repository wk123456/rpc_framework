package com.acme.rpc.demo;

import com.acme.rpc.demo.framework.client.EnableRpcStudyClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EnableRpcStudyClient(basePackages = {"com.acme.rpc.demo"})
public class DemoApplication {
    //server为0，client为1
    public static Integer mode = 0;

    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(DemoApplication.class);
        //server模式时去掉web容器
        if (mode == 0) {
            application.setWebApplicationType(WebApplicationType.NONE);
        }
        application.run(args);
    }

}
