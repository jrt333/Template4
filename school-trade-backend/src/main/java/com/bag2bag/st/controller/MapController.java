package com.bag2bag.st.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/map")
// 删除 @CrossOrigin 注解，使用全局配置
public class MapController {

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchLocation(@RequestParam String q) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (q == null || q.trim().isEmpty()) {
                response.put("error", "Missing query parameter");
                return ResponseEntity.badRequest().body(response);
            }

            String searchUrl = "https://maps.auckland.ac.nz/auckland/fa64ffa351cb4fe680fa2929/search?q="
                    + URLEncoder.encode(q, StandardCharsets.UTF_8.toString());

            response.put("success", true);
            response.put("name", q);
            response.put("detailsUrl", searchUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}