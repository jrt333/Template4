package com.bag2bag.st.dto;

public class ApiResponse {
    private Integer status_code;
    private Object data;
    private String msg;

    public ApiResponse() {}

    public ApiResponse(Integer status_code, Object data, String msg) {
        this.status_code = status_code;
        this.data = data;
        this.msg = msg;
    }

    public static ApiResponse success(Object data) {
        return new ApiResponse(1, data, "Success");
    }

    public static ApiResponse error(String msg) {
        return new ApiResponse(0, null, msg);
    }

    public Integer getStatus_code() {
        return status_code;
    }

    public void setStatus_code(Integer status_code) {
        this.status_code = status_code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}