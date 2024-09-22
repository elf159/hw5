package com.example.accounts.config;


import converter.ConverterServiceGrpc;
import org.springframework.context.annotation.Configuration;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
@Configuration(proxyBeanMethods = false)
@GrpcClientBean(
        clazz = ConverterServiceGrpc.ConverterServiceBlockingStub.class,
        beanName = "grpcStub",
        client = @GrpcClient(
                value = "grpcClient"
        )
)
public class Config {
}
