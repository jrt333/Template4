package com.bag2bag.st.controller;

import com.bag2bag.st.entity.Trade;
import com.bag2bag.st.enums.ErrorMsg;
import com.bag2bag.st.service.TradeService;
import com.bag2bag.st.vo.R;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
@RestController
@RequestMapping("trades")
public class TradeController {
    @Resource private TradeService tradeService;

    // 買家發起交易
    @PostMapping
    public R create(@CookieValue("shUserId") String shUserId, @RequestBody Trade trade) {
        // 前端只需帶 itemId、sellerId、meetTime(ISO字串)、meetPlace、priceCents
        trade.setBuyerId(Long.parseLong(shUserId));
        boolean ok = tradeService.addTrade(trade);
        return ok ? R.success(trade) : R.fail(ErrorMsg.CREATE_TRADE_FAIL);
    }

    // 賣家接受/拒絕
    @PostMapping("/{id}/respond")
    public R respond(@CookieValue("shUserId") String shUserId,
                     @PathVariable Long id,
                     @RequestParam boolean accept,
                     @RequestParam int version) {
        boolean ok = tradeService.sellerRespond(id, accept, version);
        return ok ? R.success(true) : R.fail(ErrorMsg.RESPOND_CONFLICT_OR_NO_PERMISSION);
    }

    // 買家/賣家點完成（雙方都點才算完成）
    @PostMapping("/{id}/confirm")
    public R confirm(@CookieValue("shUserId") String shUserId,
                     @PathVariable Long id,
                     @RequestParam String role,     // "buyer" 或 "seller"
                     @RequestParam int version) {
        boolean ok = tradeService.confirmTrade(id, Long.parseLong(shUserId), role, version);
        return ok ? R.success(true) : R.fail(ErrorMsg.CONFIRM_CONFLICT_OR_NO_PERMISSION);
    }

    //（可選）查一筆
    @GetMapping("/{id}")
    public R one(@PathVariable Long id) { return R.success(tradeService.getTrade(id)); }
}
