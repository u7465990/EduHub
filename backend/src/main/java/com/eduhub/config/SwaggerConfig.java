package com.eduhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API 文档配置 (Knife4j / Swagger)
 *
 * 面试考点：API 文档自动生成
 * 访问地址: http://localhost:8080/doc.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EduHub API 文档")
                        .version("1.0.0")
                        .description("智能作业搜题平台 - 后端接口文档")
                        .contact(new Contact()
                                .name("EduHub Team")
                                .email("contact@eduhub.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
