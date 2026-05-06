package org.backend.service;

import org.backend.model.Dto.AdminUserCreateRequest;
import org.backend.model.Dto.AdminUserUpdateRequest;
import org.backend.model.Dto.PageResult;
import org.backend.model.Dto.RegisterRequest;
import org.backend.model.Dto.UserListItemDto;

import java.util.List;

public interface UserService {
    /**
     * 用户注册逻辑
     * @param registerRequest 注册请求参数
     * @return 错误信息,如果为空则表示注册成功
     */
    String register(RegisterRequest registerRequest);

    /**
     * 分页查询用户列表(ADMIN 专用)
     */
    PageResult<UserListItemDto> listUsers(int page, int size, String keyword);

    /**
     * 创建用户(ADMIN 专用)
     */
    String createUser(AdminUserCreateRequest request);

    /**
     * 更新用户(姓名 / 角色 / 状态)
     */
    String updateUser(Long userId, AdminUserUpdateRequest request);

    /**
     * 重置用户密码到指定值
     */
    String resetPassword(Long userId, String newPassword);

    /**
     * 按角色 code 查询所有该角色下的有效用户(用于 OPP_ADMIN 派单选 CUSTOMER_MANAGER 等场景)
     */
    List<UserListItemDto> listByRoleCode(String roleCode);
}
