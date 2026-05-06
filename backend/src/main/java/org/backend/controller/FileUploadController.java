package org.backend.controller;

import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传(本地存储)
 * <p>合同附件等业务文件统一通过本接口上传,返回可直接下载的 URL,
 * 调用方把 URL 写入业务表(如 {@code crm_contract.file_url})。
 * <p>权限: 仅 OPP_ADMIN(与"创建合同"角色一致)。
 * <p>存储: {@code app.upload.base-dir/contracts/{uuid}-{原文件名}},
 * 通过 WebMvcConfig 暴露为 {@code /uploads/contracts/{uuid}-{原文件名}}。
 */
@RestController
@RequestMapping("/files")
public class FileUploadController {

    @Value("${app.upload.base-dir:uploads}")
    private String baseDir;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('OPP_ADMIN')")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "category", defaultValue = "contracts") String category) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请选择文件");
        }
        // 简单分类白名单(防止用户写 ../ 跨目录)
        if (!category.matches("^[a-zA-Z0-9_-]+$")) {
            return Result.error(400, "category 仅支持字母/数字/下划线/连字符");
        }

        try {
            Path dir = Paths.get(baseDir, category).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            // 保留原扩展名,前缀 UUID 避免同名冲突
            String safeName = UUID.randomUUID().toString().replace("-", "") + "-" + sanitize(original);
            Path target = dir.resolve(safeName);
            file.transferTo(target);

            String url = "/uploads/" + category + "/" + safeName;
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("originalName", original);
            data.put("size", String.valueOf(file.getSize()));
            return Result.success(data);
        } catch (IOException e) {
            return Result.error(500, "文件保存失败: " + e.getMessage());
        }
    }

    /** 去掉文件名里的路径分隔符与控制字符,避免路径注入 */
    private static String sanitize(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|\\x00-\\x1F]", "_");
    }
}
