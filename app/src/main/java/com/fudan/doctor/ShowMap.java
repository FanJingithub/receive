package com.fudan.doctor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fudan.helper.BaseActivity;
import com.fudan.helper.DoctorAdapter;
import com.fudan.helper.HttpConnector;
import com.fudan.helper.HttpListener;
import com.fudan.helper.SaveObject;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.object.Location;
import com.tencent.lbssearch.object.param.SearchParam;
import com.tencent.lbssearch.object.result.SearchResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mapsdk.raster.model.BitmapDescriptor;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by FanJin on 2017/1/19.
 */

public class ShowMap extends BaseActivity implements TencentLocationListener,HttpListener {

    private static final String TAG = "ShowMap";
    private String [] sos_str={"","突然晕倒","严重外伤","产科急救","儿科急救"};
    private String [] sos_state={
            "求救进行中……",
            "志愿者提示救助已完成，\n            等待呼救者确认完成",
            "呼救者已确认救助完成！",
            "该呼救者已被举报！"};
    private MapView mapview;
    private Button informationTV;
    private Button getOver,commit_help;
    private ImageButton phoneSos;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String mynum;
    private int type;
    private String fornum,forname;
    private String info;
    private int sos;

    TencentLocationManager locationManager;
    TencentLocationRequest request;
    Marker marker;
    TencentMap tencentMap;
    int error;
    SearchParam mSearchParam;
    private Bitmap bitmapAED;
    private BitmapDescriptor mAEDMarker;
    private Bitmap bitmapDoctor;
    private BitmapDescriptor mDoctorMarker;
    private Bitmap bitmapHospital;
    private BitmapDescriptor mHospitalMarker;
    private double longitudeSos,latitudeSos;
    private Double tlongitudeSos,tlatitudeSos;
    private TencentSearch tencentSearch;
    private Location location0;
    private SearchParam.Nearby nearBy;

    private List<SaveObject> mAEDList;
    private List<SaveObject> mHospitalList;
    private List<SaveObject> mDoctorList;
    private SaveObject mAED=new SaveObject();
    private SaveObject mHospital=new SaveObject();
    private SaveObject mDoctor=new SaveObject();

    private double tLatitude,tLongitude;

    private RecyclerView recyclerview;
    private LinearLayoutManager layoutManager;
    private DoctorAdapter adapter;
    private boolean firstLoc;

    private Button btn1,btn2,btn3;
    private Button reportBtn;
    private TextView title;
    private TextView noHelp;

    private String messages;
    private int wrongSOS,finishSOS;
    private int sosState;

    /**
     * whether need empty the list if state==-1 ?
     */
    @Override
    public void onHttpFinish(int state, String responseData){
        if (state == -1){
            Toast.makeText(ShowMap.this,getResources().getString(R.string.network_exception),Toast.LENGTH_SHORT).show();
        }else {
            Log.e(TAG, "onHttpFinish: --------"+responseData );
            parseJSON(responseData);
            // mark sos
            if (firstLoc){
                tencentMap.animateTo(new LatLng(tLatitude,tLongitude));
                firstLoc = false;
            }
            markObject(tLatitude,tLongitude,"求救者",BitmapDescriptorFactory.defaultMarker(),true);
            informationTV.setText(info);
            //adapt the help list
            if ((mDoctorList !=null) && (mDoctorList.size() >0)) {

                //mark Doctor
                for(SaveObject mSaveObjct : mDoctorList){
                    markObject(mSaveObjct.latitude, mSaveObjct.longitude,mSaveObjct.title+" "+mSaveObjct.address,mDoctorMarker,false);
                }
                adapter = new DoctorAdapter(mDoctorList,ShowMap.this);
                recyclerview.setAdapter(adapter);
                recyclerview.setVisibility(View.VISIBLE);
                noHelp.setVisibility(View.INVISIBLE);
            } else {
                recyclerview.setVisibility(View.INVISIBLE);
                noHelp.setVisibility(View.VISIBLE);
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
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMain));
        }

        bitmapAED = BitmapFactory.decodeResource(getResources(),R.drawable.aed_marker);
        mAEDMarker= new BitmapDescriptor(bitmapAED);

        bitmapHospital = BitmapFactory.decodeResource(getResources(),R.drawable.hospital_marker);
        mHospitalMarker= new BitmapDescriptor(bitmapHospital);

        bitmapDoctor = BitmapFactory.decodeResource(getResources(),R.drawable.doctor_marker);
        mDoctorMarker= new BitmapDescriptor(bitmapDoctor);

        type=1;
        Intent intent=getIntent();
        forname=intent.getStringExtra("name");
        fornum=intent.getStringExtra("fornum");
        sos=intent.getIntExtra("sos",0);
        tlatitudeSos=intent.getDoubleExtra("latitudeSos",0);
        latitudeSos = tlatitudeSos.doubleValue();
        tlongitudeSos = intent.getDoubleExtra("longitudeSos",0);
        longitudeSos = tlongitudeSos.doubleValue();

        Log.e("sos","------"+sos);
        pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
        mynum=pref.getString("num","0");

        //fornum get from apply.  These three arguments would be changed when click help

        setContentView(R.layout.show_map);
        mapview=(MapView) findViewById(R.id.bmapView);
        mapview.onCreate(savedInstanceState);
        tencentMap = mapview.getMap();
        tencentMap.setZoom(15);
        firstLoc = true;

        Log.e(TAG, "onCreate: ----"+latitudeSos+" "+longitudeSos );
        location0 = new Location().lat((float)latitudeSos).lng((float) longitudeSos);
        nearBy = new SearchParam.Nearby()
                .point(location0)
                .r(5*1000);
        tencentSearch=new TencentSearch(this);

        recyclerview = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(layoutManager);

        noHelp = (TextView) findViewById(R.id.no_help);

        informationTV=(Button) findViewById(R.id.information_btn);
        informationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ShowMap.this)
                        .setMessage("求救者："+forname+"\n联系方式："+fornum+"\n求救原因："+sos_str[sos]
                                +"\n附加信息："+messages+"\n求救状态："+sos_state[sosState])
                        .setPositiveButton("返回",null )
                        .setNegativeButton("虚假求救信息，我要举报",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                reportWrong();
                            }
                        })
                        .create()
                        .show();
            }
        });
        //information.setText("周围暂时没有求救信息");
        //neighborView=(TextView) findViewById(R.id.neighbors);
/**
        getOver=(Button) findViewById(R.id.get_over);
        getOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
*/
        phoneSos = (ImageButton) findViewById(R.id.phone_sos);
        phoneSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+fornum));
                startActivity(intent);
            }
        });
        reportBtn = (Button) findViewById(R.id.report_btn);
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportWrong();
            }
        });

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        showFirstChoice();

        List<String> permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(ShowMap.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(ShowMap.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(ShowMap.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String [] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(ShowMap.this,permissions,1);
        } else {
            requestLocation();

        }
        //title= (TextView) findViewById(R.id.title_choice);
    }

    private void directCall(String fornum){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+fornum));
        startActivity(intent);
    }

    /**
     * get the location of neighboring AEDs
     */
    private void getAED(){
        int i;
        for (i=0;i<2;i++){
            mSearchParam = new SearchParam().keyword("AED")
                    .boundary(nearBy)
                    .page_size(20)
                    .page_index(i);
            tencentSearch.search(mSearchParam, new HttpResponseListener() {
                @Override
                public void onSuccess(int i, BaseObject baseObject) {
                    Log.d(TAG, "onSuccess: ------------"  );
                    SearchResultObject oj = (SearchResultObject)baseObject;
                    if(oj.data != null) {
                        for (SearchResultObject.SearchResultData data : oj.data) {
                            Log.d("demo", "title:" + data.address);
                            Log.d(TAG, "loacation--"+data.location.lat+" "+data.location.lng );
                            mAED = new SaveObject();   //should not be forgotten
                            mAED.address = data.address;
                            mAED.title = data.title;
                            mAED.latitude = data.location.lat;
                            mAED.longitude = data.location.lng;
                            mAEDList.add(mAED);
                        }
                    }
                }

                @Override
                public void onFailure(int i, String s, Throwable throwable) {
                    Log.e(TAG, "onFailure: -------------" );
                }
            });
        }
    }

    /**
     * get the location of the neighboring hospitals
     */
    private void getHospital(){
        int i;
        for (i=0;i<2;i++){
            mSearchParam = new SearchParam().keyword("急救")
                    .boundary(nearBy)
                    .page_size(20)
                    .page_index(i);
            tencentSearch.search(mSearchParam, new HttpResponseListener() {
                @Override
                public void onSuccess(int i, BaseObject baseObject) {
                    Log.e(TAG, "onSuccess: -------------" );
                    SearchResultObject oj = (SearchResultObject)baseObject;
                    if(oj.data != null) {
                        for (SearchResultObject.SearchResultData data : oj.data) {
                            Log.e("demo", "title:" + data.address);
                            Log.e(TAG, "loacation--"+data.location.lat+" "+data.location.lng );
                            mHospital = new SaveObject();
                            mHospital.address = data.address;
                            mHospital.title = data.title;
                            mHospital.latitude = data.location.lat;
                            mHospital.longitude = data.location.lng;
                            mHospitalList.add(mHospital);
                            //markObject(data.location.lat,data.location.lng,data.title);
                        }
                    }
                }

                @Override
                public void onFailure(int i, String s, Throwable throwable) {
                    Log.e(TAG, "onFailure: ------------" );
                }
            });
        }
    }

    /**
     * create a marker
     * @param flag: true if you want to show it's title
     */
    private void markObject(double mLatitude, double mLongitude, String title,BitmapDescriptor mBitmap,boolean flag){
        marker = tencentMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLatitude, mLongitude))
                .title(title)
                .anchor(0.5f, 0.5f)
                .icon(mBitmap)
                .draggable(true));
        if (flag){
            marker.showInfoWindow();
        }
    }

    /**
     * start to request the location data
     */
    private void requestLocation(){
        request = TencentLocationRequest.create()
                .setInterval(3*1000)
                .setAllowCache(true)
                .setRequestLevel(4);
        locationManager = TencentLocationManager.getInstance(this);
        error = locationManager.requestLocationUpdates(request, this);
        mAEDList = new ArrayList<>();
        getAED();
        mHospitalList = new ArrayList<>();
        getHospital();
    }

    /**
     * TencentLocationListener callback
     */
    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        if (TencentLocation.ERROR_OK == error) {
            // 定位成功

            Log.d(TAG, "onLocationChanged: ---------"+location.getLatitude()+"      "+location.getLongitude() );
            HttpConnector.downInformation(2,type,mynum,fornum,sos, location.getLatitude(), location.getLongitude(), ShowMap.this);//must use try and catch

            tencentMap.clearAllOverlays();

            // mark myself
            markObject(location.getLatitude(), location.getLongitude(),"我",mDoctorMarker,true);

            // mark AED
             for(SaveObject mSaveObjct : mAEDList){
             markObject(mSaveObjct.latitude, mSaveObjct.longitude,mSaveObjct.title,mAEDMarker,false);
             }

            // mark Hospital
            for(SaveObject mSaveObjct : mHospitalList){
                markObject(mSaveObjct.latitude, mSaveObjct.longitude,mSaveObjct.title,mHospitalMarker,false);
            }
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

    /**
     * parse the data which come from the server
     */
    public void parseJSON(String jsonData){
        int atype;
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            mDoctorList = new ArrayList<>();
            Log.d("parse","-------------------"+jsonData);
            Log.d("showMap--parse","-------------------"+jsonArray.length());
            sosState = 2;

            for (int i=0; i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                atype=jsonObject.getInt("type");
                if (atype==0){
                    info = "呼救者："+forname+" "+jsonObject.getString("num")+" |详情";
                    //reportBtn.setVisibility(View.VISIBLE);  // visualize the button
                    phoneSos.setVisibility(View.VISIBLE);  // visualize the button
                    tLatitude = jsonObject.getDouble("lati");
                    tLongitude = jsonObject.getDouble("longi");
                    messages = jsonObject.getString("message");
                    wrongSOS = jsonObject.getInt("wrong");
                    finishSOS= jsonObject.getInt("finish");
                    if (wrongSOS==1){
                        sosState = 3;
                        info = forname+" "+"已被举报 | 详情";
                    } else if (finishSOS==1){
                        sosState = 1;
                    } else {
                        sosState = 0;
                    }
                    Log.d(TAG, "parseJSON: ------00-------"+sos+"  "+tLatitude+" "+tLongitude );
                }
                else if((atype==2) || (atype==3)){
                    mDoctor = new SaveObject();
                    mDoctor.address =jsonObject.getString("num");;
                    mDoctor.title =jsonObject.getString("name");
                    //mDoctor.title = name_str[i];
                    mDoctor.latitude = jsonObject.getDouble("lati");
                    mDoctor.longitude = jsonObject.getDouble("longi");
                    mDoctorList.add(mDoctor);
                } else if (atype==-1){
                    info = forname+" "+"已撤销了求救 | 详情";
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        mapview.onDestroy();
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mapview.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mapview.onResume();
        super.onResume();
    }

    @Override
    protected void onStop() {
        mapview.onStop();
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
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
                            finish();
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
     * the first choice after clicking in the map
     */
    private void showFirstChoice(){
        btn3.setVisibility(View.VISIBLE);
        btn3.setText("参与救助");
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type=2;
                directCall(fornum);
                showPhoneStateChoice();
            }
        });

        btn1.setVisibility(View.VISIBLE);
        btn1.setText("返回");
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * whether get valid connection
     */
    private void showPhoneStateChoice(){
        //commit_help.setVisibility(View.INVISIBLE);

        new AlertDialog.Builder(ShowMap.this)
                .setMessage("是否已联系上求救者？")
                .setPositiveButton("联系成功", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        reportPhoneState(1);
                        showPhoneStateSuccessChoice();
                    }
                })
                .setNegativeButton("联系失败", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        reportPhoneState(0);
                        showPhoneStateFailedChoice();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void showPhoneStateSuccessChoice(){

        btn3.setVisibility(View.VISIBLE);
        btn3.setText("前往救助");
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               reportGoing(1);
            }
        });

        btn1.setVisibility(View.VISIBLE);
        btn1.setText("退出救助活动");
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportCancel(1);
            }
        });
    }

    private void showPhoneStateFailedChoice(){

        new AlertDialog.Builder(ShowMap.this)
                .setMessage("是否退出救助活动？")
                .setPositiveButton("仍然前往救助", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        reportGoing(0);
                    }
                })
                .setNegativeButton("退出救助活动", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        reportCancel(0);
                    }
                })
                .setCancelable(false)
                .create()
                .show();

        //title.setText("请做出下一步选择！");
    }

    private void showGoingChoice(final int phoneState){

        btn1.setVisibility(View.VISIBLE);
        btn1.setText("退出救助活动");
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportCancel(phoneState);
            }
        });

        btn3.setVisibility(View.VISIBLE);
        btn3.setText("救助完成");
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportFinish();
            }
        });
    }

    private void reportWrong(){

        new AlertDialog.Builder(ShowMap.this)
                .setMessage("举报此人后，此人将被拉入求救平台黑名单，求救信息将会停止传播。是否确定举报？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        type = 1;
                        HttpConnector.reportWrong(mynum, fornum, new HttpListener() {
                            @Override
                            public void onHttpFinish(int state, String responseData) {
                                if (state == -1){
                                    Toast.makeText(ShowMap.this,getResources().getString(R.string.network_exception),Toast.LENGTH_SHORT).show();
                                } else {
                                    finish();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    private void reportPhoneState(int myState){
        HttpConnector.reportPhoneState(mynum, fornum, myState, new HttpListener() {
            @Override
            public void onHttpFinish(int state, String responseData) {
                if (state == -1){
                    Toast.makeText(ShowMap.this,getResources().getString(R.string.network_exception),Toast.LENGTH_SHORT).show();
                } else {
                    //
                }
            }
        });
    }

    private void reportCancel(int phoneState){
        type = 1; // maybe need fix
        HttpConnector.reportMyStateChanged(mynum, fornum, 1, phoneState, new HttpListener() {
            @Override
            public void onHttpFinish(int state, String responseData) {
                if (state == -1){
                    Toast.makeText(ShowMap.this,getResources().getString(R.string.network_exception),Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
        });
    }

    private void reportGoing(final int phoneState){
        type = 3;
        HttpConnector.reportMyStateChanged(mynum, fornum, 3, phoneState, new HttpListener() {
            @Override
            public void onHttpFinish(int state, String responseData) {
                if (state == -1){
                    Toast.makeText(ShowMap.this,getResources().getString(R.string.network_exception),Toast.LENGTH_SHORT).show();
                } else {
                    showGoingChoice(phoneState);
                }
            }
        });
    }

    private void reportFinish(){

        new AlertDialog.Builder(ShowMap.this)
                .setMessage("确定施救结束后，其他志愿者会收到施救结束的提示。是否确定？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        type = 1;  // maybe need fix
                        HttpConnector.reportFinish(mynum, fornum, new HttpListener() {
                            @Override
                            public void onHttpFinish(int state, String responseData) {
                                if (state == -1){
                                    Toast.makeText(ShowMap.this,getResources().getString(R.string.network_exception),Toast.LENGTH_SHORT).show();
                                } else {
                                    finish();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("取消", null)
                .setCancelable(false)
                .create()
                .show();

    }

}
