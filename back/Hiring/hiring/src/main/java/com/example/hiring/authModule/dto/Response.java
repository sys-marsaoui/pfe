package com.example.hiring.authModule.dto;

public class Response {
    String code;

    String msg;

    Object object;
    public Response(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Response(String code, String msg, Object object) {
        this.code = code;
        this.msg = msg;
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
