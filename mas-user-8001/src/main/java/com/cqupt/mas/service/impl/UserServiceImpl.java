package com.cqupt.mas.service.impl;

import com.cqupt.mas.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author 唐海军
 * @create 2022-10-21 18:58
 */


@Configuration
@EnableSwagger2
public class UserServiceImpl implements UserService {

    @Bean
    public Docket getDocket(){
        ApiInfoBuilder apiInfoBuilder = new ApiInfoBuilder();
        apiInfoBuilder.title("医疗辅助系统")
                .description("【医疗辅助系统】后端接口文档")
                .version("1.0.0");
        ApiInfo build = apiInfoBuilder.build();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(build)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.cqupt.mas.controller"))
                .paths(PathSelectors.any())
                .build();
    }

}
