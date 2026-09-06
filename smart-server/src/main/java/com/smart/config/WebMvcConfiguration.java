package com.smart.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.smart.interceptor.JwtTokenAdminInterceptor;
import com.smart.interceptor.JwtTokenEmployeeInterceptor;
import com.smart.interceptor.JwtTokenUserInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenEmployeeInterceptor jwtTokenEmployeeInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        // 管理端拦截器
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/auth/login", "/admin/auth/securityQuestion", "/admin/auth/resetPassword", "/admin/common/download/**", "/admin/shop/getStatus", "/admin/category/list");
        // 员工端拦截器
        registry.addInterceptor(jwtTokenEmployeeInterceptor)
                .addPathPatterns("/employee/**")
                .excludePathPatterns("/employee/auth/login", "/employee/service-item/page", "/employee/service-package/page");
        // 用户端拦截器
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/user/login");
    }

    /**
     * 配置跨域
     */
    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        log.info("配置跨域...");
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:5174")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置静态资源映射（用于访问上传的文件）
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        File uploadDir = new File(System.getProperty("user.dir"), "upload");
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(uploadDir.toURI().toString());
    }

    /**
     * 配置 ObjectMapper Bean，全局生效
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 禁用时间戳格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 配置 LocalDateTime 序列化格式
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        objectMapper.registerModule(javaTimeModule);

        // 配置 java.util.Date 格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        log.info("配置全局 ObjectMapper，日期格式：yyyy-MM-dd HH:mm:ss");
        return objectMapper;
    }

    /**
     * 扩展消息转换器
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        converters.add(0, converter);
    }
}