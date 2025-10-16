package com.bag2bag.st.controller;

import com.bag2bag.st.entity.User;
import com.bag2bag.st.enums.ErrorMsg;
import com.bag2bag.st.service.GoogleOAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class GoogleOAuthController {

    @Resource
    private GoogleOAuthService googleOAuthService;

    @Value("${google.oauth2.redirect-uri}")
    private String googleRedirectUri;

    @Value("${app.frontend-origin:http://localhost:8081}")
    private String frontendOrigin;

    @GetMapping("/oauth2/callback")
    public void handleCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String target = resolveTarget(state);

        if (error != null) {
            redirectWithError(response, target, errorDescription != null ? errorDescription : error);
            return;
        }

        if (code == null || code.trim().isEmpty()) {
            redirectWithError(response, target, ErrorMsg.PARAM_ERROR.getMsg());
            return;
        }

        /*String requestRedirectUri = googleRedirectUri;
        if (request != null && request.getRequestURL() != null) {
            requestRedirectUri = request.getRequestURL().toString();
        }*/
        String requestRedirectUri = googleRedirectUri;

        try {
            //User user = googleOAuthService.authenticate(code, requestRedirectUri);
            User user = googleOAuthService.authenticate(code, googleRedirectUri);
            Cookie cookie = new Cookie("shUserId", String.valueOf(user.getId()));
            cookie.setPath("/");
            cookie.setHttpOnly(false);
            response.addCookie(cookie);
            //response.sendRedirect(target);
            response.sendRedirect(frontendOrigin + normalizePath(target));
        } catch (GoogleOAuthService.GoogleOAuthException ex) {
            if (ex.getErrorMsg() == ErrorMsg.INVALID_EMAIL_DOMAIN) {
                clearLoginCookie(response);
                response.setStatus(HttpServletResponse.SC_FOUND); // 302
                response.setHeader("Location", "https://escortify.co.nz/");
                return;
            }
            redirectWithError(response, target, ex.getErrorMsg().getMsg());
        } catch (Exception ex) {
            redirectWithError(response, target, ErrorMsg.GOOGLE_AUTH_ERROR.getMsg());
        }
    }

    /*private String resolveTarget(String state) {
        if (state != null && state.startsWith("/")) {
            return state;
        }
        return "/index";
    }*/
    private String resolveTarget(String state) {
        if (state == null || state.isEmpty()) return "/index";
        String decoded;
        try {
            decoded = java.net.URLDecoder.decode(state, java.nio.charset.StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            decoded = state;
        }
        if (!decoded.startsWith("/")) return "/index";      // 防 open redirect
        return "/".equals(decoded) ? "/index" : decoded;    // 统一把根路径映射到 /index
    }

    private String normalizePath(String path) {
        return "/".equals(path) ? "/" : ("/index".equals(path) ? "/" : path);
    }

    private void clearLoginCookie(HttpServletResponse response) {
        Cookie c = new Cookie("shUserId", "");
        c.setPath("/");
        c.setHttpOnly(true);
        c.setMaxAge(0); // 立即失效
        response.addCookie(c);
    }

    private void redirectWithError(HttpServletResponse response, String target, String errorMessage) throws IOException {
        String safeTarget = resolveTarget(target);
        String message = errorMessage != null ? errorMessage : ErrorMsg.GOOGLE_AUTH_ERROR.getMsg();
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.name());
        String delimiter = safeTarget.contains("?") ? "&" : "?";
        //response.sendRedirect(safeTarget + delimiter + "error=" + encoded);
        response.sendRedirect(frontendOrigin + safeTarget + delimiter + "error=" + encoded);
    }
    /*private String normalizePath(String path) {
        if ("/index".equals(path)) return "/";
        return path;
    }*/
}