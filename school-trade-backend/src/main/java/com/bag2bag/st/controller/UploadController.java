package com.bag2bag.st.controller;

import com.bag2bag.st.enums.ErrorMsg;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.*;
import java.util.*;
import com.bag2bag.st.vo.R;

@RestController
@RequestMapping("upload")
public class UploadController {

    @PostMapping("/chat-image")
    public R<?> uploadChatImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return R.fail(ErrorMsg.Fail_is_empty);
            }
            if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
                return R.fail(ErrorMsg.Please_upload_image_only);
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                return R.fail(ErrorMsg.your_image_too_large);
            }

            // 創建保存目錄
            Path uploadDir = Paths.get("uploads", "chat");
            Files.createDirectories(uploadDir);

            String ext = Optional.ofNullable(file.getOriginalFilename())
                    .filter(n -> n.contains("."))
                    .map(n -> n.substring(n.lastIndexOf('.')))
                    .orElse(".jpg");

            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path filePath = uploadDir.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/chat/" + fileName;
            Map<String, String> data = new HashMap<>();
            data.put("url", fileUrl);

            return R.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(ErrorMsg.Upload_fail);
        }
    }
}
