package com.fudan.doctor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by FanJin on 2017/1/30.
 * MyReceiver can receive the broadcast of BOOT_COMPLETED
 * However some kinds of phone can't get the permission so we might should set the Auto_start after installing the APK
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context,NotifyService.class);
        context.startService(service);
        Log.e("TAG", "开机自动服务自动启动.....");
        //Intent intent = getPackageManager().getLaunchIntentForPackage(com.example.fanjin.client1);
        //context.startActivity(intent);
    }
}
