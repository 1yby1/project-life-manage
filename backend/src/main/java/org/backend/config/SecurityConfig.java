package org.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Spring Security 配置
 * <p>
 * 鉴权策略:
 * <ul>
 *   <li>JWT 无状态会话(SessionCreationPolicy.STATELESS)</li>
 *   <li>JwtAuthenticationFilter 把 token 解析为 CustomUserDetails 并填充 SecurityContext</li>
 *   <li>多角色: CustomUserDetails.authorities 含全部 "ROLE_XXX" 权限,hasRole / hasAnyRole 自动匹配</li>
 *   <li>URL 级粗粒度限制在本类配置,Controller 方法级细粒度用 {@link org.springframework.security.access.prepost.PreAuthorize}</li>
 * </ul>
 * <p>
 * 与需求文档 §四 权限矩阵 + §八 关键业务规则#1 越权拦截 对齐。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. 公开接口
                .requestMatchers("/auth/login", "/auth/register", "/auth/logout", "/uploads/**").permitAll()

                // 2. 系统管理 - 仅 ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 3. 客户管理写操作 - OPP_ADMIN 录入派单, CUSTOMER_MANAGER 走访录入
                .requestMatchers(HttpMethod.POST, "/customers/**").hasAnyRole("OPP_ADMIN", "CUSTOMER_MANAGER")
                .requestMatchers(HttpMethod.PUT,  "/customers/**").hasAnyRole("OPP_ADMIN", "CUSTOMER_MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/customers/**").hasRole("OPP_ADMIN")
                // 客户读操作: 任何登录用户

                // 4. 其余接口 - 已登录即可,Controller 上用 @PreAuthorize 做细粒度限制
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
