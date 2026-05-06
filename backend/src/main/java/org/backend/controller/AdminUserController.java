package org.backend.controller;

import org.backend.model.Dto.AdminUserCreateRequest;
import org.backend.model.Dto.AdminUserUpdateRequest;
import org.backend.model.Dto.PageResult;
import org.backend.model.Dto.UserListItemDto;
import org.backend.service.UserService;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统管理 / 用户管理 (ADMIN 专用)
 *
 * 注意:Spring Security 中 /admin/** 当前不限制角色,需在 SecurityConfig 中加 @PreAuthorize 或 hasRole 限制。
 *      短期由前端路由守卫(meta.roles=[ADMIN])拦截,后续可在 SecurityConfig 加 RBAC 限制。
 */
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * GET /admin/users?page=1&size=10&keyword=xxx
     */
    @GetMapping
    public Result<PageResult<UserListItemDto>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.listUsers(page, size, keyword));
    }

    /**
     * POST /admin/users
     */
    @PostMapping
    public Result<String> create(@RequestBody AdminUserCreateRequest request) {
        String err = userService.createUser(request);
        return err == null ? Result.success("创建成功") : Result.error(err);
    }

    /**
     * PUT /admin/users/{id}
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody AdminUserUpdateRequest request) {
        String err = userService.updateUser(id, request);
        return err == null ? Result.success("更新成功") : Result.error(err);
    }

    /**
     * PATCH /admin/users/{id}/password  body: { "password": "xxx" }
     */
    @PatchMapping("/{id}/password")
    public Result<String> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String pwd = body.get("password");
        String err = userService.resetPassword(id, pwd);
        return err == null ? Result.success("密码已重置") : Result.error(err);
    }
}
