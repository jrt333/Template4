package com.bag2bag.st.service;

import com.bag2bag.st.entity.Trade;

public interface TradeService {
    boolean addTrade(Trade trade);      // 建立交易
    Trade getTrade(Long id);            // 查詢交易
    boolean sellerRespond(Long id, boolean accept, int version); // 賣家接受/拒絕
    boolean confirmTrade(Long id, Long userId, String role, int version); // 雙方確認
}
