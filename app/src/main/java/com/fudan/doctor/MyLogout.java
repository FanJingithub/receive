package com.fudan.doctor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.fudan.helper.ActivityCollector;
import com.fudan.helper.BaseActivity;

/**
 * Created by FanJin on 2017/1/20.
 */

public class MyLogout extends BaseActivity {
    Button logout,back;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logout);
        logout=(Button) findViewById(R.id.logout);
        back=(Button) findViewById(R.id.back_logout);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
                editor=pref.edit();
                editor.clear();
                editor.putBoolean("isOnline",false);
                editor.apply();
                ActivityCollector.finishAll();
            }
        });
    }
}
