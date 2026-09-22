package com.shiyu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI shiyuOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("食遇 Shiyu API")
                .description("情侣菜谱点餐系统 - 后端接口文档\n\n" +
                    "## 认证方式\n" +
                    "除 `/api/auth/login` 和 `/api/auth/register` 外，所有接口需要在请求头中携带 JWT Token：\n" +
                    "```\nAuthorization: Bearer <token>\n```\n\n" +
                    "## 默认账号\n" +
                    "- 管理员：`admin` / `admin123`")
                .version("1.0.0")
                .contact(new Contact()
                    .name("AllenJiaru")
                    .url("https://github.com/AllenJiaru"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer"))
            .schemaRequirement("Bearer", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Token 认证，从登录接口获取"));
    }
}
