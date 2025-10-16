package com.bag2bag.st.mapper;

import com.bag2bag.st.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户 数据持久层
 *
 * @author: ShanZhu
 * @date: 2024-01-05
 */
@Mapper
public interface UserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(User record);

    int insertSelective(User record);

    User userLogin(@Param("UPI") String UPI,
                   @Param("userPassword") String userPassword);

    User selectByPrimaryKey(Long id);

    List<User> getUserList();

    List<User> findUserByList(List<Long> idList);

    List<User> getNormalUser(int begin, int nums);

    List<User> getBanUser(int begin, int nums);

    int countNormalUser();

    int countBanUser();

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int updatePassword(@Param("newPassword") String newPassword,
                       @Param("oldPassword") String oldPassword, @Param("id") Long id);
    User selectByEmail(@Param("email") String email);

    int applyRating(Map<String, Object> params);

}