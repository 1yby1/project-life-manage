package org.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态资源映射:把本地 {@code uploads/} 目录映射为 HTTP 路径 {@code /uploads/**},
 * 让浏览器可直接通过 {@code crm_contract.file_url} 下载/预览合同附件。
 * <p>SecurityConfig 已把 {@code /uploads/**} 设为 permitAll(允许浏览器点 link 直接打开,无需 token)。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.base-dir:uploads}")
    private String baseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path absolute = Paths.get(baseDir).toAbsolutePath().normalize();
        // file:/// 三斜杠 + 末尾斜杠 — Spring Resource 识别目录
        String location = absolute.toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
