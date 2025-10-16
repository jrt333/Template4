package com.bag2bag.st.mapper;

import com.bag2bag.st.entity.Trade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TradeMapper {
    int insert(Trade t);
    Trade selectById(@Param("id") Long id);

    // PROPOSED(0) -> PENDING_BOTH_CONFIRM(2) + locked=1
    int acceptToPending(@Param("id") Long id, @Param("expectVersion") Integer expectVersion);

    // PROPOSED(0) -> DECLINED(4)
    int decline(@Param("id") Long id, @Param("expectVersion") Integer expectVersion);

    // PENDING_BOTH_CONFIRM(2) 下確認；若雙方都 1 -> COMPLETED(3)
    int markBuyerConfirmed(@Param("id") Long id, @Param("expectVersion") Integer expectVersion);
    int markSellerConfirmed(@Param("id") Long id, @Param("expectVersion") Integer expectVersion);
}
