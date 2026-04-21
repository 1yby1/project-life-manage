package org.backend.controller;

import org.backend.model.CustomUserDetails;
import org.backend.model.Dto.LoginResponse;
import org.backend.model.Dto.RegisterRequest;
import org.backend.service.UserService;
import org.backend.util.JwtUtil;
import org.backend.util.Result;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        System.out.println("登录请求: " + username);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("用户名或密码错误");
        }


        // 2. 验证通过后生成 Token
        String token = jwtUtil.generateToken(username);

        // 3. 获取自定义用户信息并返回
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(username);
        response.setUserId(userDetails.getUserId());
        response.setRoleId(userDetails.getRoleId());

        System.out.println("登录成功: " + username);
        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("注销成功");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest registerRequest) {
        String errorMsg = userService.register(registerRequest);
        if (errorMsg == null) {
            return Result.success("注册成功");
        } else {
            return Result.error(errorMsg);
        }
    }
}
