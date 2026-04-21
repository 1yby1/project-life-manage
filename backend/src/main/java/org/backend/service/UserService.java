package org.backend.service;

import org.backend.model.Dto.RegisterRequest;

public interface UserService {
    /**
     * 用户注册逻辑
     * @param registerRequest 注册请求参数
     * @return 错误信息，如果为空则表示注册成功
     */
    String register(RegisterRequest registerRequest);
}

