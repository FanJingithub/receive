package com.fudan.doctor;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fudan.helper.BaseActivity;
import com.fudan.helper.HttpConnector;
import com.fudan.helper.HttpListener;

/**
 * Created by FanJin on 2017/3/1.
 * Feedback sends the feedback message to the server.
 */

public class Feedback extends BaseActivity implements HttpListener {
    Button back;
    int choice;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String inf;
    private EditText feedback_inf;

    @Override
    public void onHttpFinish(int state, String responseData){
        if (state == -1){
            Toast.makeText(Feedback.this,"无法连接到服务器，请检查网络状态",Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >=21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMain));
        }

        setContentView(R.layout.feedback);
        back=(Button) findViewById(R.id.back_feedback);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        feedback_inf=(EditText) findViewById(R.id.feedback_inf);
        Log.e("feedback", "--------ok----"+inf+"-----------------");
        Button commit_feedback=(Button) findViewById(R.id.commit_feedback);
        commit_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inf = feedback_inf.getText().toString();
                //URLEncoder.encode(inf,"UTF-8");
                pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
                String snum=pref.getString("num","");
                HttpConnector.myFeedback(snum, inf, Feedback.this);
            }
        });
    }
}
