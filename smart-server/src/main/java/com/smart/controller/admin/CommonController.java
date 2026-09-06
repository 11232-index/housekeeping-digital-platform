package com.smart.controller.admin;

import com.smart.constant.MessageConstant;
import com.smart.result.Result;
import com.smart.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用控制器（文件上传等公共功能）
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class


CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传到阿里云 OSS
     *
     * @param file 上传的文件
     * @return 文件在 OSS 上的访问 URL
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file.getOriginalFilename());

        // 获取原始文件名，提取扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        // 生成唯一文件名，避免覆盖
        String objectName = UUID.randomUUID().toString() + extension;

        try {
            // 上传到阿里云 OSS，返回完整的访问 URL
            String fileUrl = aliOssUtil.upload(file.getBytes(), objectName);
            log.info("文件上传到阿里云OSS成功，访问路径：{}", fileUrl);
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage());
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}
