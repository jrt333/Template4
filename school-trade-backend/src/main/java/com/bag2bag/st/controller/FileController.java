package com.bag2bag.st.controller;

import com.bag2bag.st.enums.ErrorMsg;
import com.bag2bag.st.service.FileService;
import com.bag2bag.st.utils.IdFactoryUtil;
import com.bag2bag.st.vo.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

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

    @Value("${baseUrl:}")
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
    public R uploadFile(
            @RequestParam("file") MultipartFile multipartFile,
            HttpServletRequest request
    ) {
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

    private String resolveBaseUrl(HttpServletRequest request) {
        if (StringUtils.hasText(baseUrl)) {
            return trimTrailingSlash(baseUrl.trim());
        }

        String forwardedProto = firstHeaderValue(request, "X-Forwarded-Proto");
        String scheme = StringUtils.hasText(forwardedProto) ? forwardedProto : request.getScheme();
        if (!StringUtils.hasText(scheme)) {
            scheme = "http";
        }
        scheme = scheme.toLowerCase(Locale.ROOT);

        String forwardedHost = firstHeaderValue(request, "X-Forwarded-Host");
        String host = StringUtils.hasText(forwardedHost) ? forwardedHost : firstHeaderValue(request, "Host");
        if (!StringUtils.hasText(host)) {
            host = request.getServerName();
        }
        if (StringUtils.hasText(host)) {
            host = host.trim();
        } else {
            host = "localhost";
        }

        String forwardedPort = firstHeaderValue(request, "X-Forwarded-Port");
        boolean hostHasPort = host != null && host.contains(":");
        String portPart = "";
        if (StringUtils.hasText(forwardedPort) && !hostHasPort) {
            if (!"80".equals(forwardedPort) && !"443".equals(forwardedPort)) {
                portPart = ":" + forwardedPort;
            }
        } else if (!StringUtils.hasText(forwardedPort) && !StringUtils.hasText(forwardedHost) && !StringUtils.hasText(forwardedProto) && !hostHasPort) {
            int serverPort = request.getServerPort();
            if (serverPort != 80 && serverPort != 443) {
                portPart = ":" + serverPort;
            }
        }

        return scheme + "://" + trimTrailingSlash(host) + portPart;
    }

    private String firstHeaderValue(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        int commaIndex = value.indexOf(',');
        if (commaIndex >= 0) {
            return value.substring(0, commaIndex).trim();
        }
        return value.trim();
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
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

}
