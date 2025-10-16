package com.bag2bag.st.service.impl;

import com.bag2bag.st.entity.OrderAddress;
import com.bag2bag.st.mapper.OrderAddressMapper;
import com.bag2bag.st.service.OrderAddressService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OrderAddressServiceImpl implements OrderAddressService {

    @Resource
    private OrderAddressMapper orderAddressMapper;

    public boolean addOrderAddress(OrderAddress orderAddressModel) {
        return orderAddressMapper.insert(orderAddressModel) == 1;
    }

    public boolean updateOrderAddress(OrderAddress orderAddressModel) {
        orderAddressModel.setOrderId(null);
        return orderAddressMapper.updateByPrimaryKeySelective(orderAddressModel) == 1;
    }

    public OrderAddress getOrderAddress(Long orderId) {
        return orderAddressMapper.selectByOrderId(orderId);
    }
}
