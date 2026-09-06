package com.smart.config;

import com.smart.properties.AliOssProperties;
import com.smart.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置类
 * 将 AliOssUtil 注册为 Spring Bean，方便在 Controller 中注入使用
 */
@Configuration
@Slf4j
public class OssConfiguration {

    @Autowired
    private AliOssProperties aliOssProperties;

    /**
     * 创建 AliOssUtil Bean
     */
    @Bean
    public AliOssUtil aliOssUtil() {
        log.info("初始化阿里云OSS工具类，endpoint: {}", aliOssProperties.getEndpoint());
        return new AliOssUtil(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName()
        );
    }
}
