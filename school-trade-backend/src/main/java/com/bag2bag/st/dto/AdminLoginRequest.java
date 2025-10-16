package com.bag2bag.st.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 管理员登录请求体
 */
public class AdminLoginRequest {

    @NotNull
    @NotEmpty
    private String accountNumber;

    @NotNull
    @NotEmpty
    private String adminPassword;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}