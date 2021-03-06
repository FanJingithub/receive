package com.fudan.doctor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.fudan.helper.ActivityCollector;
import com.fudan.helper.BaseActivity;
import com.fudan.helper.HttpConnector;
import com.fudan.helper.HttpListener;
import com.fudan.helper.MobileInfoUtils;
import com.fudan.helper.PermissionUtils;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * Created by FanJin on 2017/1/19.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener,TencentLocationListener,HttpListener {

    private static final String TAG = "MainActivity";

    private Button to_map ,show_tell ,to_service,aMeun;
    private TextView information;
    private TextView locationResult;
    private Intent intent,intent2;
    private DrawerLayout mDrawerLayout;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String mynum, neighboring;
    private int i;
    private String [] sos_str={"","突然晕倒","严重外伤","产科急救","儿科急救"};
    private String [] list_name=new String[100];
    private String [] list_num=new String[100];
    private int [] list_sos=new int[100];
    private double[] list_lat = new double[100];
    private double[] list_lng = new double[100];
    private ArrayList<CharSequence> list_help;
    ListView listHelp;
    private ImageView backgroundPic;
    private SharedPreferences resource;
    private SpannableString str_sp1,str_sp2;
    private double distance;
    StyleSpan styleSpan_A,styleSpan_B;

    TencentLocationManager locationManager;
    TencentLocationRequest request;
    int error;

    @Override
    public void onHttpFinish(int state, String responseData){
        list_help = new ArrayList<CharSequence> ();
        if (((! responseData.equals("200")) && (! responseData.equals("401"))) ){
            parseJSON(responseData.toString());
            listHelp.setVisibility(View.VISIBLE);
            locationResult.setVisibility(View.INVISIBLE);
        }
        else {
            listHelp.setVisibility(View.INVISIBLE);
            locationResult.setVisibility(View.VISIBLE);
            if (state == -1){
                Toast.makeText(MainActivity.this,"无法连接到服务器，请检查网络状态",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
        mynum=pref.getString("num","0");


        setContentView(R.layout.have_menu);

        /**
         * set background pic
         */
/**        backgroundPic = (ImageView) findViewById(R.id.bg_pic);
        resource = getSharedPreferences("resource",MODE_PRIVATE);
        String pic = resource.getString("pic",null);
        long nowdate = System.currentTimeMillis();
        long lastdata = resource.getLong("lastdate",0);
        Log.d(TAG, "onCreate: ----"+lastdata);
        Log.d(TAG, "onCreate: ----"+nowdate);
        if ((pic != null) && (nowdate-lastdata <12*60*60*1000)){
            //if ((pic != null) && (nowdate-lastdata <1000)){ // just for debugging
            Glide.with(this).load(pic).into(backgroundPic);
        } else {
            SharedPreferences.Editor editor = resource.edit();
            editor.putLong("lastdate",nowdate);
            editor.apply();
            loadPic();
        }*/


        mDrawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView=(NavigationView) findViewById(R.id.nav_view);

        SharedPreferences pref2 =getSharedPreferences("loginStatus",MODE_PRIVATE);
        String name = pref2.getString("name","");
        String phone = pref2.getString("num","");
        View headView = navView.getHeaderView(0);
        TextView user = headView.findViewById(R.id.user_center_name);
        user.setText(name+"\n"+phone);

        //navView.setCheckedItem(R.id.user_center_wallet);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.user_center_wallet:
                        Log.e("click","------------credit");
                        intent2=new Intent(MainActivity.this,MyCredit.class);
                        startActivity(intent2);
                        break;
                    case R.id.user_center_permission:
                        Intent intentMyPermission=new Intent(MainActivity.this,MyPermission.class);
                        startActivity(intentMyPermission);
                        break;
                    case R.id.user_center_help:
                        Log.e("click","------------help");
                        intent2=new Intent(MainActivity.this,MyHelp.class);
                        startActivity(intent2);
                        break;
                    case R.id.user_center_about:
                        Log.e("click","------------about");
                        intent2=new Intent(MainActivity.this,MyAbout.class);
                        startActivity(intent2);
                        break;
                    case R.id.user_center_logout:
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("退出登录")
                                .setMessage("确定要退出吗？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
                                        editor=pref.edit();
                                        editor.clear();
                                        editor.putBoolean("isOnline",false);
                                        editor.apply();
                                        ActivityCollector.finishAll();
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .setCancelable(false)
                                .create()
                                .show();
                        break;
                    default:
                }
                //mDrawerLayout.closeDrawers();
                return true;
            }
        });

        //information=(TextView) findViewById(R.id.apply_declaration);
        locationResult = (TextView) findViewById(R.id.location_result);

        //to_map=(Button) findViewById(R.id.to_map);
        show_tell=(Button)findViewById(R.id.a_help);
        //to_service=(Button) findViewById(R.id.to_service);
        aMeun=(Button) findViewById(R.id.a_menu);
        listHelp = (ListView)findViewById(R.id.list_help);

        intent=new Intent(MainActivity.this,ShowMap.class);

        //to_map.setOnClickListener(this);
        show_tell.setOnClickListener(this);
        aMeun.setOnClickListener(this);

        List<String> permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CALL_PHONE);
        }
        if (!permissionList.isEmpty()){
            String [] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        } else {
            requestLocation();

        }

        /**
         * self-start dialog would show if it is the first time to use this APP.
         */
        SharedPreferences historyFile = getSharedPreferences("historyFile",MODE_PRIVATE);
        boolean isFirst = historyFile.getBoolean("isFirst",true);
        if (isFirst){
            jumpStartInterface();
            SharedPreferences.Editor editor = historyFile.edit();
            editor.putBoolean("isFirst",false);
            editor.apply();
        }
        Intent service = new Intent(MainActivity.this,NotifyService.class);
        startService(service);


        /**
         * set the effect for drawLayout
         */
        ImageView blurImageView = headView.findViewById(R.id.iv_blur);
        ImageView avatarImageView =headView.findViewById(R.id.iv_avatar);
        Glide.with(this).load(R.drawable.dddd)
                .apply(bitmapTransform(new BlurTransformation(25)))
                .into(blurImageView);

        Glide.with(this).load(R.drawable.ic_person)
                .apply(bitmapTransform(new CropCircleTransformation()))
                .into(avatarImageView);

        styleSpan_A  = new StyleSpan(Typeface.BOLD);
        styleSpan_B  = new StyleSpan(Typeface.BOLD);

        /**
         * check new version everyday
         */
        resource = getSharedPreferences("resource",MODE_PRIVATE);
        final long nowdate = System.currentTimeMillis();
        final long lastdata = resource.getLong("lastdate",0);
        Log.d(TAG, "onCreate: ----"+lastdata);
        Log.d(TAG, "onCreate: ----"+nowdate);
        if (nowdate-lastdata >24*60*60*1000){
            //if ((pic != null) && (nowdate-lastdata <1000)){ // just for debugging
            HttpConnector.checkNew(1,new HttpListener() {
                @Override
                public void onHttpFinish(int state, String responseData) {
                    if (state==-1){
                        Toast.makeText(MainActivity.this,"无法连接到服务器，请检查网络状态",Toast.LENGTH_SHORT).show();
                    }else {
                        SharedPreferences.Editor editor = resource.edit();
                        editor.putLong("lastdate",nowdate);
                        editor.apply();
                        if (responseData.equals("new")){
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("发现新版本")
                                    .setMessage("请升级APP！")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            HttpConnector.downloadNew(new HttpListener() {
                                                @Override
                                                public void onHttpFinish(int state, String responseData) {
                                                    if (state == -1){
                                                        Toast.makeText(MainActivity.this,"无法连接到服务器，请检查网络状态",Toast.LENGTH_SHORT).show();
                                                    }else {
                                                        //
                                                    }
                                                }
                                            });
                                        }
                                    })
                                    .setNegativeButton("以后再说", null)
                                    .create()
                                    .show();
                        }
                    }
                }
            });
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.a_help:
                //to server
                Intent intent3=new Intent(MainActivity.this,MyHelp.class);
                startActivity(intent3);
                break;

            case R.id.a_menu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
    }

    /**
     * Jump Start Interface
     */
    private void jumpStartInterface() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.app_user_auto_start);
            builder.setPositiveButton("立即设置",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MobileInfoUtils.jumpStartInterface(MainActivity.this);
                        }
                    });
            builder.setNegativeButton("暂时不设置",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.setCancelable(false);
            builder.create().show();
        } catch (Exception e) {
        }
    }

/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }
*/
    /**
     * start to request the location data
     */
    private void requestLocation(){
        //mLocationClient.start();
        request = TencentLocationRequest.create()
                .setInterval(3*1000)
                .setAllowCache(true)
                .setRequestLevel(4);
        locationManager = TencentLocationManager.getInstance(this);
        error = locationManager.requestLocationUpdates(request, this);
    }

    /**
     * TencentLocationListener callback
     */
    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        if (TencentLocation.ERROR_OK == error) {
            // 定位成功
            HttpConnector.downInformation(1,1,mynum,"0",0, location.getLatitude(), location.getLongitude(),MainActivity.this);
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
    protected void onResume() {
        requestLocation();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        requestLocation();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length >0){
                    for (int result :grantResults){
                        if (result !=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序！",
                                    Toast.LENGTH_SHORT).show();

                            /**
                             * permission dialog would show if it is the first time.
                             */
                            SharedPreferences historyFile = getSharedPreferences("historyFile",MODE_PRIVATE);
                            boolean isFirst = historyFile.getBoolean("isFirstPermission",true);
                            if (isFirst){
                                PermissionUtils.permissionWarning(this,getResources().getString(R.string.permission_message));
                                SharedPreferences.Editor editor = historyFile.edit();
                                editor.putBoolean("isFirstPermission",false);
                                editor.apply();
                            }

                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    /**
     * parse the data which come from the server
     */
    public void parseJSON(String jsonData){
        String num,name;
        int sos;
        double latitude,longitude;
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            Log.e(TAG, "parseJSON: "+jsonData);
            for (int i=0; i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                num=jsonObject.getString("num");
                name=jsonObject.getString("name");
                //name = name_str[i];
                sos=jsonObject.getInt("sos");
                latitude=jsonObject.getDouble("lati");
                longitude=jsonObject.getDouble("longi");
                //distance=jsonObject.getDouble("distance");
                Log.d(TAG, "parseJSON: -----"+name.length()+" "+name);
                str_sp1 = new SpannableString(name + " "+num+"         \n");
                str_sp2 = new SpannableString("求救原因  "+sos_str[sos]+"                | 详情 >");

                str_sp1.setSpan(styleSpan_A, 0,17, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                str_sp2.setSpan(styleSpan_B, 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                list_help.add(TextUtils.concat(str_sp1,str_sp2));
                list_name[i]=name;
                list_num[i]=num;
                list_sos[i]=sos;
                list_lat[i] = latitude;
                list_lng[i] =longitude;
                Log.e(TAG, "parseJSON: "+"-------------"+i+"-----------"+latitude+"-----------"+longitude);
                Log.e("parse","-------------"+i+"-----------over");
            }
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(MainActivity.this, R.layout.array_adapter, list_help);
            ListView listView = (ListView) findViewById(R.id.list_help);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    Intent intent=new Intent(MainActivity.this,ShowMap.class);
                    intent.putExtra("name",list_name[arg2]);
                    intent.putExtra("fornum",list_num[arg2]);
                    intent.putExtra("sos",list_sos[arg2]);
                    intent.putExtra("latitudeSos",list_lat[arg2]);
                    intent.putExtra("longitudeSos",list_lng[arg2]);
                    startActivity(intent);
                }
            });

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * download the pic from internet
     */
    private void loadPic(){
        HttpConnector.loadPic(new HttpListener() {
            @Override
            public void onHttpFinish(int state, String responseData) {
                resource = getSharedPreferences("resource",MODE_PRIVATE);
                if (state == -1){
                    Log.d(TAG, "onHttpFinish: ---failed" );
                    String pic = resource.getString("pic",null);
                    Glide.with(MainActivity.this).load(pic).into(backgroundPic);
                }else {
                    final String bingPic = responseData;
                    SharedPreferences.Editor editor = resource.edit();
                    editor.putString("pic", bingPic);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(MainActivity.this).load(bingPic).into(backgroundPic);
                        }
                    });
                }
            }
        });

    }

}
