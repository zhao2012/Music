package com.example.administrator.day2_27;

/**
 * Created by Administrator on 2018/3/1 0001.
 */

public class MessageEvent {
    private String msg;
    private int count;

    public MessageEvent(String msg,int count) {
        super();
        this.msg = msg;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public String getMsg() {
        return msg;
    }
}
