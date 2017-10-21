package com.fudan.doctor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fudan.helper.BaseActivity;
import com.fudan.helper.HttpConnector;
import com.fudan.helper.HttpListener;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by FanJin on 2017/1/20.
 */

public class MyCredit extends BaseActivity {
    private static final String TAG = "MyCredit";
    private Button back;
    private String myNum;
    private TextView totalScore,totalSuccess,totalJoin,totalAction;
    private TextView certification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >=21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMain));
        }

        setContentView(R.layout.my_credit);
        back=(Button) findViewById(R.id.back_credit);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        totalScore = findViewById(R.id.score_tv) ;
        totalJoin = findViewById(R.id.join_tv) ;
        totalAction = findViewById(R.id.action_tv) ;
        totalSuccess = findViewById(R.id.success_tv) ;
        certification = findViewById(R.id.certification_tv) ;

        SharedPreferences pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
        myNum=pref.getString("num","0");
        HttpConnector.getScore(myNum, new HttpListener() {
            @Override
            public void onHttpFinish(int state, String responseData) {
                if (state == -1){
                    Toast.makeText(MyCredit.this,getResources().getString(R.string.network_exception),Toast.LENGTH_SHORT).show();
                }else {
                    if (! responseData.equals("401")){
                        parseJSON(responseData);
                    }

                }
            }
        });
    }

    /**
     * parse the data which come from the server
     */
    public void parseJSON(String jsonData){
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            Log.e(TAG, "parseJSON: "+jsonData);
            for (int i=0; i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                totalScore.setText(jsonObject.getInt("total_score")+" 分");
                totalJoin.setText(jsonObject.getInt("total_join")+" 次");
                totalAction.setText(jsonObject.getInt("total_action")+" 次");
                totalSuccess.setText(jsonObject.getInt("total_success")+" 次");
                certification.setText(jsonObject.getString("certification"));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
