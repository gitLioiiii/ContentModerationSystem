package com.example.demo.controller;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.utils.ResultTemplate;

@RestController
public class UploadController {

    @Value("${application.upload-root}")
    private String uploadRoot;

    @PostMapping("/upload/{type}")
    public ResultTemplate upload(
        @PathVariable("type") String type,
        @RequestParam("file") MultipartFile multipartFile
    ) throws IllegalStateException, IOException {
        ResultTemplate result   = new ResultTemplate();

        // File.separator是获取操作系统分隔符，如Windows是\，Linux是/
        // 还可以使用 Paths.get() 更现代和跨平台兼容
        File path   = new File(this.uploadRoot + File.separator + type);
        if (!path.exists()) {
            path.mkdirs();
        }

        // 使用UUID生成唯一文件名
        String filename     = UUID.randomUUID().toString();
        String extension    = multipartFile.getOriginalFilename();

        if (extension != null) {
            // 如(uuid)254e3a04-7f59-48d3-bcfe-52a801c4919b(extension)黑色背景.(分隔符)png
            filename    = filename + extension.substring(extension.indexOf("."));
        }
        
        multipartFile.transferTo(new File(path, filename));
        // 访问图片：http://localhost:8080/images/c53fb114-f711-4bde-a3b3-9b0b345a1e7f.png
        result.putPayload("filename", "/" + type + "/" + filename);
        result.putPayload("originalFilename", multipartFile.getOriginalFilename());

        return result;
    }

}
