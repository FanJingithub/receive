package com.fudan.doctor;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.fudan.helper.HttpConnector;
import com.fudan.helper.HttpListener;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by FanJin on 2017/1/30.
 */

public class NotifyService extends Service implements TencentLocationListener,HttpListener {
    private static final String TAG = "NotifyService";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    String mynum;
    int flag=0;
    TencentLocationManager locationManager;
    TencentLocationRequest request;
    int error;

    private SharedPreferences locSet;
    private SharedPreferences.Editor setEditor;
    private int setSize;
    private Set loc_set,new_set;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
        mynum=pref.getString("num","0");

        request = TencentLocationRequest.create()
                .setInterval(10*1000)
                .setAllowCache(true)
                .setRequestLevel(4);
        locationManager = TencentLocationManager.getInstance(this);
        error = locationManager.requestLocationUpdates(request, this);


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onHttpFinish(int state, String responseData){
        if (state == -1){
            //
        } else{
            locSet = getSharedPreferences("locSet",MODE_PRIVATE);
            if (((! responseData.equals("200")) && (! responseData.equals("401"))) ){
                parseJSON(responseData.toString());
            } else if (responseData.equals("200")){
                setEditor = locSet.edit();
                setEditor.clear();
                setEditor.putInt("setSize",0);
                setEditor.apply();
            }
        }
    }

    /**
     * TencentLocationListener callback
     */
    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        if (TencentLocation.ERROR_OK == error) {
            // 定位成功
            HttpConnector.downInformation(0,1,mynum,"0",0, location.getLatitude(), location.getLongitude(),NotifyService.this);
        } else {
            // 定位失败
        }
    }

    /**
     * TencentLocationListener callback
     */
    @Override
    public void onStatusUpdate(String name, int status, String desc) {
        // do your work
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    /**
     * parse the data which come from the server
     */
    public void parseJSON(String jsonData){
        String num,name;
        int sos;
        double latitude,longitude;
        try {
            loc_set = new HashSet();
            setSize = locSet.getInt("setSize",0);
            for (int i=1;i<=setSize;i++){
                loc_set.add(locSet.getString("element"+i,""));
            }
            setEditor = locSet.edit();
            setEditor.clear();
            JSONArray jsonArray = new JSONArray(jsonData);
            new_set=new HashSet();
            Log.e(TAG, "parseJSON: "+jsonData);
            for (int i=0; i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                num=jsonObject.getString("num");
                new_set.add(num);
                setEditor.putString("element"+(i+1),num);
                Log.e("parse","-------------"+i+"-----------over");
            }
            setEditor.putInt("setSize",jsonArray.length());
            setEditor.apply();
            if (! loc_set.containsAll(new_set)){
                setNotification();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setNotification(){
        flag=1;
        Intent intent= new Intent(getApplicationContext(), LoginActivity.class);
        PendingIntent pi=PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("有求助信息")
                .setContentText("有求助信息")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setPriority(android.support.v7.app.NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[]{0,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000});

        manager.notify(1, builder.build());
    }
}
