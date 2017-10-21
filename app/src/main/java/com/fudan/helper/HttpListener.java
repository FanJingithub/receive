package com.fudan.helper;

/**
 * Created by FanJin on 2017/10/11.
 */

public interface HttpListener {
    void onHttpFinish(int state, String responseData);
}
