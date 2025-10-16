package com.bag2bag.st.service.impl;

import com.bag2bag.st.entity.User;
import com.bag2bag.st.mapper.UserMapper;
import com.bag2bag.st.service.UserService;
import com.bag2bag.st.vo.PageVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    public User getUser(Long id) {
        return userMapper.selectByPrimaryKey(id);
    }

    public User userLogin(String UPI, String userPassword) {
        return userMapper.userLogin(UPI, userPassword);
    }

    public boolean userSignIn(User user) {
        return userMapper.insert(user) == 1;
    }

    public boolean updateUserInfo(User user) {
        if (user == null || user.getId() == null) return false;

        // 仅允许修改公开资料字段，避免越权更新敏感列
        User patch = new User();
        patch.setId(user.getId());
        patch.setNickname(user.getNickname());
        patch.setAvatar(user.getAvatar());
        patch.setCountry(user.getCountry());
        patch.setMajor(user.getMajor());
        patch.setDegree(user.getDegree());

        return userMapper.updateByPrimaryKeySelective(patch) > 0;
    }

    public boolean updatePassword(String newPassword, String oldPassword, Long id) {
        return userMapper.updatePassword(newPassword, oldPassword, id) == 1;
    }

    public PageVo<User> getUserByStatus(int status, int page, int nums) {
        List<User> list;
        int count = 0;
        if (status == 0) {
            count = userMapper.countNormalUser();
            list = userMapper.getNormalUser((page - 1) * nums, nums);
        } else {
            count = userMapper.countBanUser();
            list = userMapper.getBanUser((page - 1) * nums, nums);
        }
        return new PageVo<>(list, count);
    }

    @Override
    public User getPublicUserById(Long id) {
        // 可直接複用 selectByPrimaryKey；或用 selectPublicById（見下）
        return userMapper.selectByPrimaryKey(id);
    }

    @Override
    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return userMapper.selectByEmail(email.trim());
    }

    @Override
    public boolean updateUserSelective(User user) {
        if (user == null || user.getId() == null) {
            return false;
        }
        return userMapper.updateByPrimaryKeySelective(user) > 0;
    }

    public int applyRating(Long userId, BigDecimal score) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (score == null) {
            throw new IllegalArgumentException("score is required");
        }

        // 简单兜底：限制 0~5 区间
        BigDecimal min = BigDecimal.ZERO;
        BigDecimal max = new BigDecimal("5");
        if (score.compareTo(min) < 0) score = min;
        if (score.compareTo(max) > 0) score = max;

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("score", score);

        return userMapper.applyRating(params);
    }

}
