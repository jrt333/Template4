package com.bag2bag.st.controller;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bag2bag.st.entity.User;
import com.bag2bag.st.enums.ErrorMsg;
import com.bag2bag.st.service.UserService;
import com.bag2bag.st.service.EmailService;
import com.bag2bag.st.service.GoogleOAuthService;
import com.bag2bag.st.vo.R;
import com.bag2bag.st.vo.UserProfileVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户相关 控制层
 *
 * @author: ShanZhu
 * @date: 2024-01-05
 */
@CrossOrigin
@RestController
@RequestMapping("user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private EmailService emailService;

    @Resource
    private GoogleOAuthService googleOAuthService;

    @Value("${app.websocket.base-url:}")
    private String configuredWsBaseUrl;

    @Value("${app.websocket.prefix:webSocketServer}")
    private String wsPathPrefix;


    /**
     * 注册账号
     *
     * @param map 用户信息
     * @return 结果
     *
     */
    @PostMapping("sign-in")
    //public R signIn(@RequestBody User user) {
    public R signIn(@RequestBody Map<String, String> map) {
        User user = new User();
        user.setAccountNumber(map.get("accountNumber"));
        user.setUserPassword(map.get("userPassword"));
        user.setNickname(map.get("nickname"));

        String upi = map.get("upi");                 // 關鍵：拿到 UPI
        if (upi == null || upi.trim().isEmpty()) return R.fail(ErrorMsg.PARAM_ERROR);
        upi = upi.trim();
        user.setUPI(upi.trim());                     // 寫入到 User
        user.setEmailNumber(upi.trim() + "@aucklanduni.ac.nz");  // 你的需求：自動生成 Email


        String code = map.get("code");
        if (!emailService.verifyCode(user.getAccountNumber(), code)) {
            return R.fail(ErrorMsg.CODE_ERROR);
        }

        user.setSignInTime(new Timestamp(System.currentTimeMillis()));
        if (user.getAvatar() == null || "".equals(user.getAvatar())) {
            user.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        }
        if (userService.userSignIn(user)) {
            return R.success(user);
        }
        return R.fail(ErrorMsg.REGISTER_ERROR);
    }

    /**
     * 发送验证码
     *
     * @param upi 学生UPI
     * @return 结果
     */
    @GetMapping("code")
    public R sendCode(@RequestParam("upi") String upi) {
        try {
            emailService.sendCode(upi);
            return R.success();
        } catch (Exception e) {
            return R.fail(ErrorMsg.EMAIL_SEND_ERROR);
        }
    }

    /**
     * 登录
     *
     * @param UPI 账号
     * @param userPassword  密码
     * @return 登录结果
     */
    @PostMapping("google-login")
    public R googleLogin(@RequestBody Map<String, String> payload, HttpServletResponse response) {
        String code = payload.getOrDefault("code", "");
        if (code == null || code.trim().isEmpty()) {
            return R.fail(ErrorMsg.PARAM_ERROR);
        }


        try {
            String redirectUri = payload.getOrDefault("redirectUri", "");
            User user = googleOAuthService.authenticate(code, redirectUri);

            Cookie cookie = new Cookie("shUserId", String.valueOf(user.getId()));
            cookie.setPath("/");
            cookie.setHttpOnly(false);
            response.addCookie(cookie);

            return R.success(UserProfileVO.from(user));
        } catch (GoogleOAuthService.GoogleOAuthException e) {
            return R.fail(e.getErrorMsg());
        } catch (Exception e) {
            return R.fail(ErrorMsg.GOOGLE_AUTH_ERROR);
        }
    }


    /**
     * 登录
     *
     * @param UPI 账号
     * @param userPassword  密码
     * @return 登录结果
     */
    @RequestMapping("login")
    public R login(
            @RequestParam("UPI") @NotEmpty @NotNull String UPI,
            @RequestParam("userPassword") @NotEmpty @NotNull String userPassword,
            HttpServletResponse response
    ) {
        User user = userService.userLogin(UPI, userPassword);
        if (null == user) {
            return R.fail(ErrorMsg.EMAIL_LOGIN_ERROR);
        }
        if (user.getUserStatus() != null && user.getUserStatus().equals((byte) 1)) {
            return R.fail(ErrorMsg.ACCOUNT_Ban);
        }

        Cookie cookie = new Cookie("shUserId", String.valueOf(user.getId()));
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
        return R.success(UserProfileVO.from(user));
    }

    /**
     * 退出登录
     *
     * @param shUserId 用户id
     * @return 结果
     */
    @RequestMapping("logout")
    public R logout(
            @CookieValue(value = "shUserId", defaultValue = "") String shUserId, HttpServletResponse response
    ) {
        if (shUserId.isEmpty()) {
            return R.fail(ErrorMsg.COOKIE_ERROR);
        }
        Cookie cookie = new Cookie("shUserId", shUserId);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return R.success();
    }

    /**
     * 获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("info")
    public R getOneUser(
            @CookieValue(value = "shUserId", defaultValue = "") String id
    ) {
        if (id.isEmpty()) {
            return R.fail(ErrorMsg.COOKIE_ERROR);
        }
        return R.success(userService.getUser(Long.valueOf(id)));
    }

    @GetMapping("{id}")
    public R getPublicUserById(@PathVariable("id") Long id) {
        User user = userService.getUser(id);   // 已存在的 Service 方法
        if (user == null) {
            return R.fail(ErrorMsg.SYSTEM_ERROR); // 沒這個人時回 404/自定錯誤
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("country", user.getCountry());   // ← 確認實體有這些 getter
        data.put("major", user.getMajor());
        data.put("degree", user.getDegree());
        data.put("signInTime", user.getSignInTime());
        data.put("rating", user.getRating() != null ? user.getRating().doubleValue() : 0D);
        data.put("rating_count", user.getRatingCount() != null ? user.getRatingCount() : 0);

        return R.success(data);
    }

    /**
     * 修改用户公开信息
     *
     * @param id   用户id
     * @param user 用户信息
     * @return 修改结果
     */
    @PostMapping("/info")
    public R updateUserPublicInfo(@CookieValue(value = "shUserId", defaultValue = "") String id, @RequestBody User user) {
        if (id.isEmpty()) {
            return R.fail(ErrorMsg.COOKIE_ERROR);
        }
        user.setId(Long.valueOf(id));

        if (userService.updateUserInfo(user)) {
            return R.success();
        }
        return R.fail(ErrorMsg.SYSTEM_ERROR);
    }


    /**
     * 修改密码
     *
     * @param id          用户id
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return
     */
    @GetMapping("/password")
    public R updateUserPassword(
            @CookieValue(value = "shUserId", defaultValue = "") String id,
            @RequestParam("oldPassword") @NotEmpty @NotNull String oldPassword,
            @RequestParam("newPassword") @NotEmpty @NotNull String newPassword) {
        if (id.isEmpty()) {
            return R.fail(ErrorMsg.COOKIE_ERROR);
        }
        if (userService.updatePassword(newPassword, oldPassword, Long.valueOf(id))
        ) {
            return R.success();
        }
        return R.fail(ErrorMsg.PASSWORD_RESET_ERROR);
    }

    @PostMapping("/{id}/rating")
    public R<Void> rateUser(@PathVariable("id") Long userId,
                            @RequestParam("score") BigDecimal score) {
        userService.applyRating(userId, score);
        return R.success();
    }
    @GetMapping("webSocketServer")
    public R<Map<String, Object>> getUserWebSocketServer(
            @CookieValue(value = "shUserId", defaultValue = "") String id,
            HttpServletRequest request
    ) {
        if (id.isEmpty()) {
            return R.fail(ErrorMsg.COOKIE_ERROR);
        }

        String baseUrl = resolveWebSocketBaseUrl(request);
        String normalizedBase = stripTrailingSlash(baseUrl);
        String connectionUrl = joinUrl(normalizedBase, id);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", id);
        data.put("baseUrl", normalizedBase);
        data.put("prefix", normalizePrefix());
        data.put("url", connectionUrl);
        data.put("server", connectionUrl);
        data.put("wsUrl", connectionUrl);
        data.put("serverUrl", connectionUrl);
        data.put("webSocketUrl", connectionUrl);

        return R.success(data);
    }

    private String resolveWebSocketBaseUrl(HttpServletRequest request) {
        String configured = stripTrailingSlash(configuredWsBaseUrl);
        if (!configured.isEmpty()) {
            return configured;
        }

        if (request == null) {
            return "ws://localhost:3001/" + normalizePrefix();
        }

        String protoHeader = firstNonEmpty(
                request.getHeader("X-Forwarded-Proto"),
                request.getHeader("X-Forwarded-Protocol"),
                request.getScheme()
        );
        String protocol = normalizeProtocol(protoHeader, request.isSecure());

        String hostHeader = firstNonEmpty(
                request.getHeader("X-Forwarded-Host"),
                request.getHeader("Host")
        );

        String host;
        if (hostHeader != null) {
            host = hostHeader.split(",", 2)[0].trim();
        } else {
            host = request.getServerName();
            int port = request.getServerPort();
            boolean standardPort = ("ws".equals(protocol) && port == 80) || ("wss".equals(protocol) && port == 443);
            if (!standardPort && port > 0) {
                host = host + ":" + port;
            }
        }

        String prefix = normalizePrefix();
        return protocol + "://" + host + "/ws/" + prefix;
    }

    private String normalizeProtocol(String proto, boolean secureFallback) {
        if (proto == null || proto.trim().isEmpty()) {
            return secureFallback ? "wss" : "ws";
        }
        String lower = proto.trim().toLowerCase();
        if ("https".equals(lower)) {
            return "wss";
        }
        if ("http".equals(lower)) {
            return "ws";
        }
        if (lower.startsWith("ws")) {
            return lower.startsWith("wss") ? "wss" : "ws";
        }
        return secureFallback ? "wss" : "ws";
    }

    private String normalizePrefix() {
        String prefix = wsPathPrefix == null ? "" : wsPathPrefix.trim();
        if (prefix.isEmpty()) {
            return "webSocketServer";
        }
        return prefix.replaceAll("^/+|/+$", "");
    }

    private String stripTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String joinUrl(String base, String segment) {
        String normalizedBase = stripTrailingSlash(base);
        String normalizedSegment = segment == null ? "" : segment.replaceAll("^/+", "");
        if (normalizedBase.isEmpty()) {
            return normalizedSegment;
        }
        return normalizedBase + "/" + normalizedSegment;
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null) {
                String trimmed = v.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
        }
        return null;
    }

}
