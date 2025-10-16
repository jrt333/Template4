package com.bag2bag.st.service.impl;

import com.bag2bag.st.entity.Trade;
import com.bag2bag.st.entity.IdleItem;
import com.bag2bag.st.mapper.TradeMapper;
import com.bag2bag.st.mapper.IdleItemMapper;
import com.bag2bag.st.service.TradeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TradeServiceImpl implements TradeService {
    @Resource private TradeMapper tradeMapper;
    @Resource private IdleItemMapper idleItemMapper;

    @Override
    public boolean addTrade(Trade trade) {
        // 1) 取物品並做基本校驗
        IdleItem item = idleItemMapper.selectByPrimaryKey(trade.getItemId());
        if (item == null || item.getIdleStatus() != 1) return false;          // 物品不可交易
        if (item.getUserId().equals(trade.getBuyerId())) return false;        // 不能與自己交易

        // 2) 自動補 sellerId（保險）
        if (trade.getSellerId() == null) {
            trade.setSellerId(item.getUserId());
        } else if (!trade.getSellerId().equals(item.getUserId())) {
            return false; // sellerId 不匹配物品擁有者
        }

        // 3) 互斥校驗：依據物品的交易型別（1=SELL, 2=TRADE）
        Integer tradeType = item.getIdleTrade(); // 請確認 IdleItem 有此欄位/Getter
        if (tradeType == null) return false;

        // 允許前端同時塞兩個值時的清理策略：僅保留符合該型別的那個欄位
        if (tradeType == 1) { // SELL
            Integer price = trade.getPriceCents();
            if (price == null || price <= 0) return false;
            trade.setTradeItem(null); // 關鍵：確保互斥
        } else if (tradeType == 2) { // TRADE（以物易物）
            String ti = trade.getTradeItem();
            if (ti == null || ti.trim().isEmpty()) return false;
            trade.setTradeItem(ti.trim());
            trade.setPriceCents(null); // 關鍵：確保互斥
        } else {
            return false; // 未知型別
        }

        // 4) 會面資訊的基本校驗（可按需放寬/加強）
        if (trade.getMeetTime() == null) return false;
        if (trade.getMeetPlace() == null || trade.getMeetPlace().trim().isEmpty()) return false;
        trade.setMeetPlace(trade.getMeetPlace().trim());

        // 5) 狀態欄位初始化
        trade.setStatus(0);     // PROPOSED
        trade.setBuyerOk(0);
        trade.setSellerOk(0);
        trade.setLocked(0);
        trade.setVersion(0);

        // 6) 寫入
        return tradeMapper.insert(trade) == 1;
    }

    @Override
    public Trade getTrade(Long id) {
        return tradeMapper.selectById(id);
    }

    @Override
    public boolean sellerRespond(Long id, boolean accept, int version) {
        return accept
                ? tradeMapper.acceptToPending(id, version) == 1
                : tradeMapper.decline(id, version) == 1;
    }

    @Override
    public boolean confirmTrade(Long id, Long userId, String role, int version) {
        Trade t = tradeMapper.selectById(id);
        if (t == null) return false;

        int n = 0;
        if ("buyer".equals(role) && t.getBuyerId().equals(userId)) {
            n = tradeMapper.markBuyerConfirmed(id, version);
        } else if ("seller".equals(role) && t.getSellerId().equals(userId)) {
            n = tradeMapper.markSellerConfirmed(id, version);
        }
        if (n == 0) return false;

        Trade updated = tradeMapper.selectById(id);
        if (updated.getStatus() == 3) { // COMPLETED
            idleItemMapper.updateIdleStatusToOffShelf(updated.getItemId());
        }
        return true;
    }

}
