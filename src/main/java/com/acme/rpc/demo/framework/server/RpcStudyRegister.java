package com.acme.rpc.demo.framework.server;

import com.acme.rpc.demo.DemoApplication;
import com.acme.rpc.demo.framework.Protocol;
import com.acme.rpc.demo.framework.ProtocolFactory;
import com.acme.rpc.demo.framework.URL;
import com.acme.rpc.demo.register.zk.RegisterForServer;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: K.
 * @date: 2021/1/20 下午3:21
 */
@Component
@Data
public class RpcStudyRegister implements InitializingBean,ApplicationContextAware {
    public static Map<String,Object> serviceMap;
    @Value("${spring.application.name}")
    private String name;

    @Value("${rpcstudy.port}")
    private Integer port;



    @Override
    public void afterPropertiesSet() throws Exception {
        if(DemoApplication.mode==0){
            String hostAddress = InetAddress.getLocalHost().getHostName();
            URL url=new URL(hostAddress,port);
            Protocol server= ProtocolFactory.netty();
            RegisterForServer.getInstance().register(name,url);
            server.start(url,name);
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //把有RpcStudyService注解的bean添加到map里面，key为该注解的接口
        if(DemoApplication.mode==0){
            Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RpcStudyService.class);
            if (beans != null && beans.size() > 0) {
                serviceMap = new HashMap<>(beans.size());
                for (Object o : beans.values()) {
                    RpcStudyService rpcService = o.getClass().getAnnotation(RpcStudyService.class);
                    String interfaceName = rpcService.value().getName();
                    serviceMap.put(interfaceName, o);
                }
                serviceMap = Collections.unmodifiableMap(serviceMap);

            }
        }

    }
}
