package com.bag2bag.st.service.impl;

import com.bag2bag.st.entity.Admin;
import com.bag2bag.st.mapper.AdminMapper;
import com.bag2bag.st.service.AdminService;
import com.bag2bag.st.vo.PageVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private AdminMapper adminMapper;

    public Admin login(String accountNumber, String adminPassword) {
        return adminMapper.login(accountNumber, adminPassword);
    }

    public PageVo<Admin> getAdminList(int page, int nums) {
        List<Admin> list = adminMapper.getList((page - 1) * nums, nums);
        int count = adminMapper.getCount();
        return new PageVo<>(list, count);
    }

    public boolean addAdmin(Admin admin) {
        return adminMapper.insert(admin) == 1;
    }
}
