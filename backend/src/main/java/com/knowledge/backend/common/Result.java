package com.knowledge.backend.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> m = new Result<>();
        m.setCode(200);
        m.setMessage("success");
        m.setData(data);
        return m;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> m = new Result<>();
        m.setCode(code);
        m.setMessage(msg);
        return m;
    }
}
