package com.bag2bag.st.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户 实体对象
 *
 * @author: ShanZhu
 * @date: 2024-01-05
 */
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 账号（手机号）
     */
    private String accountNumber;

    /**
     * 登录密码
     */
    private String userPassword;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 注册时间
     */
    private Date signInTime;

    private Byte userStatus;

    private String emailNumber;

    private String UPI;

    /**
     * 科系/专业
     */
    private String major;

    /**
     * 国家
     */
    private String country;

    /**
     * 学位
     */
    private String degree;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 评分次數
     */
    private Integer ratingCount;
}