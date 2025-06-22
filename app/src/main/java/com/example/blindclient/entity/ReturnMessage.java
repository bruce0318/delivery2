package com.example.blindclient.entity;

import com.alibaba.fastjson.annotation.JSONField;

public class ReturnMessage <T> {
    @JSONField(name = "code", ordinal = 1)
    private Integer code;

    @JSONField(name = "message", ordinal = 2)
    private String message;

    @JSONField(name = "data", ordinal = 3)
    private T data;

    public ReturnMessage (Integer code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ReturnMessage (){
        code = 0;
        message = "";
        data = null;
    }

    public boolean isSuccess(){
        return message.equals("success");
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
