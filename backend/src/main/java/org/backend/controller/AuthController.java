package org.backend.controller;

import org.backend.model.CustomUserDetails;
import org.backend.model.Dto.LoginResponse;
import org.backend.model.Dto.RegisterRequest;
import org.backend.service.UserService;
import org.backend.util.JwtUtil;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (Exception e) {
            return Result.error("用户名或密码错误");
        }

        // 验证通过后生成 Token
        String token = jwtUtil.generateToken(username);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(username);
        response.setUserId(userDetails.getUserId());
        response.setRoleId(userDetails.getRoleId());
        response.setRealName(userDetails.getRealName());
        response.setRoles(userDetails.getRoleCodes());

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

    /**
     * 当前登录用户信息(前端 fetchMe 调用)
     * 不返回 token(token 已在请求头中携带,客户端无需再获取)
     */
    @GetMapping("/me")
    public Result<LoginResponse> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return Result.error(401, "未登录");
        }
        LoginResponse resp = new LoginResponse();
        resp.setUserId(userDetails.getUserId());
        resp.setUsername(userDetails.getUsername());
        resp.setRealName(userDetails.getRealName());
        resp.setRoleId(userDetails.getRoleId());
        resp.setRoles(userDetails.getRoleCodes());
        return Result.success(resp);
    }
}
