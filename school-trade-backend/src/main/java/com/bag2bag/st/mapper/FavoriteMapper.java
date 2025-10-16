package com.bag2bag.st.mapper;

import com.bag2bag.st.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏 数据库持久层
 *
 * @author: ShanZhu
 * @date: 2024-01-05
 */
@Mapper
public interface FavoriteMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Favorite record);

    int insertSelective(Favorite record);

    Favorite selectByPrimaryKey(Long id);

    List<Favorite> getMyFavorite(@Param("userId")Long userId);

    Integer checkFavorite(Long userId, Long idleId);

    int updateByPrimaryKeySelective(Favorite record);

    int updateByPrimaryKey(Favorite record);
}