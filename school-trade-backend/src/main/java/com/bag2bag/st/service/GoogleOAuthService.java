package com.bag2bag.st.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bag2bag.st.entity.User;
import com.bag2bag.st.enums.ErrorMsg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GoogleOAuthService {

    @Resource
    private UserService userService;

    @Value("${google.oauth2.client-id}")
    private String googleClientId;

    @Value("${google.oauth2.client-secret}")
    private String googleClientSecret;

    @Value("${google.oauth2.redirect-uri}")
    private String googleRedirectUri;

    @Value("${google.oauth2.token-uri}")
    private String googleTokenUri;

    @Value("${google.oauth2.token-info-uri}")
    private String googleTokenInfoUri;

    @Value("${google.oauth2.user-info-uri}")
    private String googleUserInfoUri;

    public User authenticate(String code, String redirectUri) {
        if (code == null || code.trim().isEmpty()) {
            log.error("[GOOGLE] missing code");
            throw new GoogleOAuthException(ErrorMsg.PARAM_ERROR);
        }

        // 1) 统一 redirect_uri（与控制台一致）
        String finalRedirectUri = (redirectUri != null && !redirectUri.trim().isEmpty())
                ? redirectUri.trim()
                : googleRedirectUri;

        if (blank(googleClientId) || blank(googleClientSecret) || blank(googleTokenUri)
                || blank(googleTokenInfoUri) || blank(googleUserInfoUri) || blank(finalRedirectUri)) {
            log.error("[GOOGLE] config missing: clientId?{} secret?{} tokenUri?{} tokenInfoUri?{} userInfoUri?{} redirectUri?{}",
                    !blank(googleClientId), !blank(googleClientSecret), !blank(googleTokenUri),
                    !blank(googleTokenInfoUri), !blank(googleUserInfoUri), !blank(finalRedirectUri));
            throw new GoogleOAuthException(ErrorMsg.SYSTEM_ERROR);
        }

        try {
            // 2) 用 code 换 token
            Map<String, Object> params = new HashMap<>();
            params.put("code", code.trim());
            params.put("client_id", googleClientId);
            params.put("client_secret", googleClientSecret);
            params.put("redirect_uri", finalRedirectUri);
            params.put("grant_type", "authorization_code");

            log.info("[GOOGLE] exchanging code for token, redirect_uri={}", finalRedirectUri);
            String tokenResponse = HttpUtil.post(googleTokenUri, params);
            log.info("[GOOGLE] token response raw={}", tokenResponse);


            JSONObject tokenJson = JSON.parseObject(tokenResponse);
            if (tokenJson == null || tokenJson.containsKey("error")) {
                log.error("[GOOGLE] token response parse null");
                throw new GoogleOAuthException(ErrorMsg.GOOGLE_AUTH_ERROR);
            }


            String idToken = tokenJson.getString("id_token");
            String accessToken = tokenJson.getString("access_token");

            if (idToken == null || idToken.trim().isEmpty()) {
                throw new GoogleOAuthException(ErrorMsg.GOOGLE_AUTH_ERROR);
            }

            // 3) 校验 id_token
            String tokenInfoResp = HttpUtil.get(googleTokenInfoUri + "?id_token=" + idToken);
            JSONObject tokenInfo = JSON.parseObject(tokenInfoResp);
            if (tokenInfo == null) {
                throw new GoogleOAuthException(ErrorMsg.GOOGLE_AUTH_ERROR);
            }

            if (!googleClientId.equals(tokenInfo.getString("aud"))) {
                throw new GoogleOAuthException(ErrorMsg.GOOGLE_AUTH_ERROR);
            }
            String iss = tokenInfo.getString("iss");
            if (iss != null && !("accounts.google.com".equals(iss) || "https://accounts.google.com".equals(iss))) {
                throw new GoogleOAuthException(ErrorMsg.GOOGLE_AUTH_ERROR);
            }

            String email = tokenInfo.getString("email");
            if (email == null || email.trim().isEmpty()) {
                throw new GoogleOAuthException(ErrorMsg.GOOGLE_AUTH_ERROR);
            }
            email = email.trim().toLowerCase(Locale.ROOT);
            if (!email.endsWith("@aucklanduni.ac.nz")) {
                throw new GoogleOAuthException(ErrorMsg.INVALID_EMAIL_DOMAIN);
            }

            String emailVerified = tokenInfo.getString("email_verified");
            if (emailVerified != null && !"true".equalsIgnoreCase(emailVerified)) {
                throw new GoogleOAuthException(ErrorMsg.GOOGLE_AUTH_ERROR);
            }

            // 4) 基本资料：先从 tokenInfo 拿，不足再 /userinfo 补齐
            String name = tokenInfo.getString("name");
            String picture = tokenInfo.getString("picture");

            if (blank(name) || blank(picture)) {
                if (!blank(accessToken)) {
                    String userInfoResp = HttpUtil.get(googleUserInfoUri + "?access_token=" + accessToken);
                    JSONObject userInfo = JSON.parseObject(userInfoResp);
                    if (userInfo != null) {
                        if (blank(name)) {
                            name = userInfo.getString("name");
                        }
                        if (blank(picture)) {
                            picture = userInfo.getString("picture");
                        }
                    }
                }
            }

            if (blank(name)) {
                int index = email.indexOf('@');
                name = index > 0 ? email.substring(0, index) : email;
            }
            String upi = extractUpi(email);

            // 5) 创建或更新用户
            User user = userService.findByEmail(email);
            if (user == null) {
                // 新用户：允许用 Google 头像；若无则用占位图
                if (blank(picture)) {
                    picture = "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png";
                }
                user = new User();
                user.setEmailNumber(email);
                user.setAccountNumber(upi);
                user.setUPI(upi);
                user.setNickname(name);
                user.setAvatar(picture);
                user.setUserPassword(null);
                user.setSignInTime(new Timestamp(System.currentTimeMillis()));
                if (!userService.userSignIn(user)) {
                    throw new GoogleOAuthException(ErrorMsg.SYSTEM_ERROR);
                }
                user = userService.findByEmail(email);
            } else {
                if (user.getUserStatus() != null && user.getUserStatus().equals((byte) 1)) {
                    throw new GoogleOAuthException(ErrorMsg.ACCOUNT_Ban);
                }

                User patch = new User();
                patch.setId(user.getId());
                boolean needUpdate = false;

                if (!upi.equalsIgnoreCase(user.getAccountNumber())) { patch.setAccountNumber(upi); needUpdate = true; }
                if (!upi.equalsIgnoreCase(user.getUPI()))           { patch.setUPI(upi);           needUpdate = true; }
                if (user.getEmailNumber() == null || !user.getEmailNumber().equalsIgnoreCase(email)) {
                    patch.setEmailNumber(email); needUpdate = true;
                }

                // 昵称：仅当库里为空时才覆盖
                if (blank(user.getNickname()) && !blank(name)) {
                    patch.setNickname(name);
                    needUpdate = true;
                }

                // 头像：仅当当前头像仍是默认/Google 或为空时才刷新
                if (shouldRefreshAvatar(user.getAvatar(), picture)) {
                    patch.setAvatar(picture);
                    needUpdate = true;
                }

                if (needUpdate) {
                    userService.updateUserSelective(patch);
                    user = userService.getUser(user.getId());
                }
            }

            if (user == null) {
                throw new GoogleOAuthException(ErrorMsg.SYSTEM_ERROR);
            }
            if (user.getUserStatus() != null && user.getUserStatus().equals((byte) 1)) {
                throw new GoogleOAuthException(ErrorMsg.ACCOUNT_Ban);
            }

            return user;
        } catch (GoogleOAuthException e) {
            log.error("[GOOGLE] business error={}", e.getErrorMsg().getMsg());
            throw e;
        } catch (Exception e) {
            log.error("[GOOGLE] unexpected exception", e);
            throw new GoogleOAuthException(ErrorMsg.GOOGLE_AUTH_ERROR, e);
        }
    }


    public static class GoogleOAuthException extends RuntimeException {
        private final ErrorMsg errorMsg;

        public GoogleOAuthException(ErrorMsg errorMsg) {
            super(errorMsg.getMsg());
            this.errorMsg = errorMsg;
        }

        public GoogleOAuthException(ErrorMsg errorMsg, Throwable cause) {
            super(errorMsg.getMsg(), cause);
            this.errorMsg = errorMsg;
        }

        public ErrorMsg getErrorMsg() {
            return errorMsg;
        }
    }
    private boolean shouldRefreshAvatar(String current, String newPic) {
        if (blank(newPic)) return false;
        if (blank(current)) return true;
        if (looksLikeGoogleAvatar(current)) return true;
        if (looksLikePlaceholder(current)) return true;
        return false; // 认为用户已有自定义头像，不覆盖
    }

    private boolean looksLikeGoogleAvatar(String url) {
        return url != null && (url.contains("googleusercontent.com") || url.contains("lh3.googleusercontent.com"));
    }

    private boolean looksLikePlaceholder(String url) {
        return url != null && (
                url.contains("cube.elemecdn.com")      // 你现在的占位图
                        || url.endsWith("/avatar.png")         // 例：项目内的默认头像路径
                        || url.contains("/default-avatar")     // 例：你项目的默认目录
        );
    }
    private static boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }
    private static String extractUpi(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at).toLowerCase(Locale.ROOT) : email.toLowerCase(Locale.ROOT);
    }


}