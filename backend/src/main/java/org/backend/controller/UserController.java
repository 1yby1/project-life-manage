package org.backend.controller;

import org.backend.model.Dto.UserListItemDto;
import org.backend.service.UserService;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通用用户查询接口(非 ADMIN 专属;ADMIN 用户管理走 /admin/users)
 * <p>本轮(2026/04/30)只提供"按角色查用户"——OPP_ADMIN 派单选客户经理用。
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 全部 CUSTOMER_MANAGER 角色的用户(派单选择列表)
     * 限制 OPP_ADMIN / ADMIN 可调用。
     */
    @GetMapping("/customer-managers")
    @PreAuthorize("hasAnyRole('OPP_ADMIN', 'ADMIN')")
    public Result<List<UserListItemDto>> customerManagers() {
        return Result.success(userService.listByRoleCode("CUSTOMER_MANAGER"));
    }

    /**
     * 全部 PROJECT_MANAGER 角色的用户(线索培育时选商机负责人/项目经理)
     * 任何已登录用户可调用。
     */
    @GetMapping("/project-managers")
    public Result<List<UserListItemDto>> projectManagers() {
        return Result.success(userService.listByRoleCode("PROJECT_MANAGER"));
    }

    /**
     * 按角色查询用户列表(任何已登录用户可调,roleCode 必填)
     * <p>用于商机组员添加等需要"按角色筛选用户"的场景
     */
    @GetMapping("/by-role/{roleCode}")
    public Result<List<UserListItemDto>> byRole(@org.springframework.web.bind.annotation.PathVariable String roleCode) {
        return Result.success(userService.listByRoleCode(roleCode));
    }
}
