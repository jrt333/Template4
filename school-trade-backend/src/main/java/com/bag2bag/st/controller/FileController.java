package com.bag2bag.st.controller;

import com.bag2bag.st.enums.ErrorMsg;
import com.bag2bag.st.service.FileService;
import com.bag2bag.st.utils.IdFactoryUtil;
import com.bag2bag.st.vo.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

/**
 * 文件控制层
 *
 * @author: ShanZhu
 * @date: 2024-01-05
 */
@CrossOrigin
@RestController
public class FileController {

    @Value("${userFilePath}")
    private String userFilePath;

    @Value("${baseUrl}")
    private String baseUrl;

    @Resource
    private FileService fileService;

    /**
     * 上传文件
     *
     * @param multipartFile 二进制文件
     * @return 结果
     */
    @PostMapping("/file")
    public R uploadFile(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request) {
        String uuid = "file" + IdFactoryUtil.getFileId();
        String fileName = uuid + multipartFile.getOriginalFilename();
        try {
            if (fileService.uploadFile(multipartFile, fileName)) {
                return R.success(resolveBaseUrl(request) + "/image?imageName=" + fileName);
            }
        } catch (IOException e) {
            return R.fail(ErrorMsg.SYSTEM_ERROR);
        }
        return R.fail(ErrorMsg.FILE_UPLOAD_ERROR);
    }

    /**
     * 获取图片
     *
     * @param imageName 图片名
     * @param response  返回结果
     * @throws IOException io异常
     */
    @GetMapping("/image")
    public void getImage(
            @RequestParam("imageName") String imageName,
            HttpServletResponse response
    ) throws IOException {
        File fileDir = new File(userFilePath);
        File image = new File(fileDir.getAbsolutePath() + "/" + imageName);
        if (image.exists()) {
            FileInputStream fileInputStream = new FileInputStream(image);
            byte[] bytes = new byte[fileInputStream.available()];
            if (fileInputStream.read(bytes) > 0) {
                OutputStream outputStream = response.getOutputStream();
                outputStream.write(bytes);
                outputStream.close();
            }
            fileInputStream.close();
        }
    }
    private String resolveBaseUrl(HttpServletRequest request) {
        String configuredBaseUrl = trimTrailingSlash(baseUrl);
        if (StringUtils.hasText(configuredBaseUrl) && !isLocalhost(configuredBaseUrl)) {
            return configuredBaseUrl;
        }

        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (!StringUtils.hasText(forwardedHost)) {
            return configuredBaseUrl;
        }

        String scheme = Optional.ofNullable(request.getHeader("X-Forwarded-Proto"))
                .filter(StringUtils::hasText)
                .orElse("https");

        String prefix = Optional.ofNullable(request.getHeader("X-Forwarded-Prefix"))
                .filter(StringUtils::hasText)
                .orElse("/api");

        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }

        return scheme + "://" + forwardedHost + trimTrailingSlash(prefix);
    }

    private boolean isLocalhost(String value) {
        String lower = value.toLowerCase();
        return lower.contains("localhost") || lower.contains("127.0.0.1");
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

}
