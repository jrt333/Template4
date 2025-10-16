package com.bag2bag.st.controller;

import com.bag2bag.st.entity.IdleItem;
import com.bag2bag.st.enums.ErrorMsg;
import com.bag2bag.st.service.IdleItemService;
import com.bag2bag.st.vo.R;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

/**
 * 闲置商品 控制层
 *
 * @author: ShanZhu
 * @date: 2024-01-05
 */
@CrossOrigin
@RestController
@RequestMapping("idle")
@Slf4j
public class IdleItemController {

    @Resource
    private IdleItemService idleItemService;

    /**
     * 添加闲置商品
     *
     * @param shUserId 用户id
     * @param idleItem 闲置id
     * @return 结果
     */
    @PostMapping("add")
    public R addIdleItem(
            @CookieValue(value = "shUserId", defaultValue = "") String shUserId,
            @RequestBody IdleItem idleItem
    ) {
        log.info("shUserId={}, idleItem={}", shUserId, idleItem);
        log.info("fields: name={}, label={}, trade={}, new={}, pictureList(class)={}, pictureList={}",
                idleItem != null ? idleItem.getIdleName() : null,
                idleItem != null ? idleItem.getIdleLabel() : null,
                idleItem != null ? idleItem.getIdleTrade() : null,
                idleItem != null ? idleItem.getIdleNew() : null,
                (idleItem != null && idleItem.getPictureList() != null)
                        ? idleItem.getPictureList().getClass().getName() : null,
                idleItem != null ? idleItem.getPictureList() : null);
        if (shUserId.isEmpty()) {
            return R.fail(ErrorMsg.COOKIE_ERROR);
        }
        idleItem.setUserId(Long.valueOf(shUserId));
        idleItem.setIdleStatus((byte) 1);
        idleItem.setReleaseTime(new Date());
        if (idleItemService.addIdleItem(idleItem)) {
            return R.success(idleItem);
        }
        return R.fail(ErrorMsg.SYSTEM_ERROR);
    }

    /**
     * 获取闲置信息
     *
     * @param id 闲置id
     * @return 闲置信息
     */
    @GetMapping("info")
    public R getIdleItem(@RequestParam Long id) {
        return R.success(idleItemService.getIdleItem(id));
    }

    /**
     * 查询所有闲置信息
     *
     * @param shUserId 用户id
     * @return 闲置信息
     */
    @GetMapping("all")
    public R getAllIdleItem(
            @CookieValue(value = "shUserId", defaultValue = "") String shUserId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "trade",  required = false) Integer trade
    ) {
        Long targetUserId = (userId != null) ? userId
                : (!shUserId.isEmpty() ? Long.valueOf(shUserId) : null);
        if (targetUserId == null) {
            return R.fail(ErrorMsg.COOKIE_ERROR);
        }
        return R.success(idleItemService.getAllIdelItem(targetUserId));
    }

    /**
     * 查询限制
     *
     * @param findValue 值
     * @param page      分页大小
     * @param nums      页数
     * @return 闲置商品信息
     */
    @GetMapping("find")
    public R findIdleItem(
            @RequestParam(value = "findValue", required = false) String findValue,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "nums", required = false) Integer nums
    ) {
        if (null == findValue) {
            findValue = "";
        }
        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }
        return R.success(idleItemService.findIdleItem(findValue, p, n));
    }

    /**
     * 查询标签
     *
     * @return 结果
     */
    @GetMapping("lable")
    public R findIdleItemByLable(
            @RequestParam(value = "idleLabel", required = true) Integer idleLabel,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "nums", required = false) Integer nums
    ) {
        int p = 1;
        int n = 8;
        if (null != page) {
            p = page > 0 ? page : 1;
        }
        if (null != nums) {
            n = nums > 0 ? nums : 8;
        }
        return R.success(idleItemService.findIdleItemByLable(idleLabel, p, n));
    }

    /**
     * 更新信息
     *
     * @param shUserId 用户id
     * @param idleItem 限制id
     * @return 更新结果
     */
    @PostMapping("update")
    public R updateIdleItem(
            @CookieValue(value = "shUserId", defaultValue = "") String shUserId,
            @RequestBody IdleItem idleItem
    ) {
        if (shUserId.isEmpty()) {
            return R.fail(ErrorMsg.COOKIE_ERROR);
        }
        idleItem.setUserId(Long.valueOf(shUserId));
        if (idleItemService.updateIdleItem(idleItem)) {
            return R.success();
        }
        return R.fail(ErrorMsg.SYSTEM_ERROR);
    }

}
