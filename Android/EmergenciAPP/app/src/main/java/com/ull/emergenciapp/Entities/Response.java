package com.ull.emergenciapp.Entities;

import com.google.gson.annotations.SerializedName;

public class Response<T> {
    @SerializedName("result")
    private Boolean result;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
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
