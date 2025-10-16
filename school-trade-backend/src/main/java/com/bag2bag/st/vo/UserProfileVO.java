package com.bag2bag.st.vo;

import com.bag2bag.st.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // 空值不輸出（可選）
public class UserProfileVO {
    private Long id;
    private String upi;
    private String nickname;
    private String avatar; // 头像 URL

    public static UserProfileVO from(User u) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(u.getId());
        vo.setUpi(u.getUPI());         // 依你的字段名調整 getUPI()/getUpi()
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());   // 依你的字段名調整 getAvatar()/getAvatarUrl()
        return vo;
    }
}
