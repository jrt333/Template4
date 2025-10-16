package com.bag2bag.st.service;

import com.bag2bag.st.entity.User;
import com.bag2bag.st.vo.PageVo;

import java.math.BigDecimal;

/**
 * 用户相关 业务服务层
 *
 * @author: ShanZhu
 * @date: 2024-01-05
 */
public interface UserService {

    /**
     * 获取某个用户的公开信息
     *
     * @param id 用户id
     * @return 用户的公开信息
     */
    User getUser(Long id);

    /**
     * 登录
     *
     * @param UPI 账号
     * @param userPassword  密码
     * @return 结果
     */
    User userLogin(String UPI, String userPassword);

    /**
     * 注册
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean userSignIn(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean updateUserInfo(User user);

    /**
     * 修改密码
     *
     * @param newPassword 新密码
     * @param oldPassword 旧密码
     * @param id 用户id
     * @return 结果
     */
    boolean updatePassword(String newPassword, String oldPassword, Long id);

    PageVo<User> getUserByStatus(int status, int page, int nums);

    User findByEmail(String email);
    User getPublicUserById(Long id);

    boolean updateUserSelective(User user);

    /**
     * 给用户累计评分（并入平均分 + 次数+1）
     * @param userId 被评分的用户ID（对方）
     * @param score  本次分数（建议0~5）
     * @return 受影响行数
     */
    int applyRating(Long userId, BigDecimal score);

}
