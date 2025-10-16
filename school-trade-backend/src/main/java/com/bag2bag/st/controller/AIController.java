package com.bag2bag.st.controller;

import com.bag2bag.st.dto.EnhanceRequest;
import com.bag2bag.st.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/enhance")
    public ResponseEntity<ApiResponse> enhanceText(@RequestBody EnhanceRequest request) {
        try {
            System.out.println("====== AI Enhancement Request ======");
            System.out.println("Text: " + request.getText());
            System.out.println("Field Type: " + request.getFieldType());

            // 验证输入
            if (request.getText() == null || request.getText().trim().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("Text cannot be empty"));
            }

            if (request.getFieldType() == null) {
                request.setFieldType("details");
            }

            // 验证 API Key
            if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
                System.err.println("ERROR: Gemini API Key is not configured!");
                return ResponseEntity.ok(ApiResponse.error("API Key not configured"));
            }

            // 构建prompt
            String prompt = buildPrompt(request.getText(), request.getFieldType());
            System.out.println("Prompt: " + prompt);

            // 调用Gemini API
            String enhancedText = callGeminiAPI(prompt);
            System.out.println("Enhanced Text: " + enhancedText);

            return ResponseEntity.ok(ApiResponse.success(enhancedText));

        } catch (Exception e) {
            System.err.println("====== AI Enhancement Error ======");
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
            return ResponseEntity.ok(ApiResponse.error("AI Enhancement failed: " + errorMsg));
        }
    }

    private String callGeminiAPI(String prompt) throws Exception {
        // 使用 gemini-2.5-flash 模型
        String geminiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

        System.out.println("Calling Gemini API: " + geminiUrl.replace(geminiApiKey, "***"));

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", Collections.singletonList(part));

        requestBody.put("contents", Collections.singletonList(content));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 2048);  // 改成 2048，给足够空间
        requestBody.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(geminiUrl, entity, Map.class);

            System.out.println("Gemini API Status: " + response.getStatusCode());

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new Exception("Gemini API returned status: " + response.getStatusCode());
            }

            Map<String, Object> responseBody = response.getBody();
            System.out.println("Gemini API Response: " + responseBody);

            if (responseBody == null) {
                throw new Exception("Empty response from Gemini API");
            }

            // 检查是否有错误
            if (responseBody.containsKey("error")) {
                Map error = (Map) responseBody.get("error");
                String errorMsg = error.get("message").toString();
                throw new Exception("Gemini API Error: " + errorMsg);
            }

            List<Map> candidates = (List<Map>) responseBody.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new Exception("No candidates in response");
            }

            Map firstCandidate = candidates.get(0);

            // 检查 finishReason
            String finishReason = (String) firstCandidate.get("finishReason");
            System.out.println("Finish Reason: " + finishReason);

            Map contentMap = (Map) firstCandidate.get("content");
            if (contentMap == null) {
                throw new Exception("No content in response. Finish reason: " + finishReason);
            }

            List<Map> parts = (List<Map>) contentMap.get("parts");

            // 添加空值检查
            if (parts == null || parts.isEmpty()) {
                throw new Exception("No parts in response. This might be due to " + finishReason +
                        ". Try reducing the prompt length or increasing maxOutputTokens.");
            }

            Map firstPart = parts.get(0);
            if (firstPart == null || !firstPart.containsKey("text")) {
                throw new Exception("No text in response parts. Finish reason: " + finishReason);
            }

            String enhancedText = (String) firstPart.get("text");

            if (enhancedText == null || enhancedText.trim().isEmpty()) {
                throw new Exception("Empty text received from Gemini API");
            }

            return enhancedText.trim();

        } catch (Exception e) {
            System.err.println("Gemini API Call Failed: " + e.getMessage());
            throw e;
        }
    }

    private String buildPrompt(String text, String fieldType) {
        if ("details".equals(fieldType)) {
            return "Translate the following text to English if needed, then rewrite it in 2-3 short, clear paragraphs. " +
                    "Use simple, natural language. Be direct and honest. " +
                    "DO NOT include any introductory phrases like 'Here is' or 'Here's'. " +
                    "Just provide the enhanced description directly.\n\n" +
                    "Original text: \"" + text + "\"\n\n";

        } else if ("exchange".equals(fieldType)) {
            return "Translate the following text to English if needed, then format it clearly and concisely. " +
                    "Use simple language. " +
                    "DO NOT include any introductory phrases. " +
                    "Just provide the result directly.\n\n" +
                    "Original text: \"" + text + "\"\n\n";

        } else {
            return "Translate and enhance the following text to English. Just provide the result directly:\n\n" + text;
        }
    }
}
