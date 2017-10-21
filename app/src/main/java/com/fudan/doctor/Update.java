package com.fudan.doctor;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fudan.helper.BaseActivity;
import com.fudan.helper.HttpConnector;
import com.fudan.helper.HttpListener;

/**
 * Created by FanJin on 2017/3/1.
 */

public class Update extends BaseActivity {
    private Button back;
    private Button downloadNew;
    private TextView checkResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >=21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMain));
        }

        setContentView(R.layout.updata);

        checkResult = (TextView) findViewById(R.id.check_result);
        downloadNew = (Button) findViewById(R.id.download_new);
        downloadNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HttpConnector.downloadNew(new HttpListener() {
                    @Override
                    public void onHttpFinish(int state, String responseData) {
                        if (state == -1){
                            Toast.makeText(Update.this,"无法连接到服务器，请检查网络状态",Toast.LENGTH_SHORT).show();
                        }else {
                            //
                        }
                    }
                });
            }
        });

        back=(Button) findViewById(R.id.back_updata);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        HttpConnector.checkNew(1,new HttpListener() {
            @Override
            public void onHttpFinish(int state, String responseData) {
                if (state==-1){
                    Toast.makeText(Update.this,"无法连接到服务器，请检查网络状态",Toast.LENGTH_SHORT).show();
                }else {
                    if (responseData.equals("new")){
                        checkResult.setText("发现新版本，是否下载更新？");
                        downloadNew.setVisibility(View.VISIBLE);
                    } else{
                        checkResult.setText("当前版本已经是最新版！");
                    }
                }
            }
        });
    }
}
