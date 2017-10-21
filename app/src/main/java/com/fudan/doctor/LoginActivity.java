package com.fudan.doctor;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fudan.helper.BaseActivity;
import com.fudan.helper.HttpConnector;
import com.fudan.helper.HttpListener;
import com.fudan.helper.MyClickableSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by FanJin on 2017/1/18.
 */

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    EditText login_name,login_num,login_pwd;
    Button login_submit,login_sign;
    public static Button get_ratify;
    TextView login_title,login_textview_num,login_textview_pwd;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private TimeCount time;
    private TextView tv;
    private SpannableString str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >=21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMain));
        }


        str=new SpannableString(getResources().getString(R.string.login_2));
        MyClickableSpan clickableSpan = new MyClickableSpan(LoginActivity.this);
        ForegroundColorSpan colorSpanA = new ForegroundColorSpan(getResources().getColor(R.color.colorMain));
        str.setSpan(colorSpanA, 16, 20, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        str.setSpan(clickableSpan, 16, 20, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        /**
         * check the user state. Skip this activity if isOnline.
         */
        pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
        boolean isOnline=pref.getBoolean("isOnline",false);
        if (isOnline){
            loginFinish();
        }
        setContentView(R.layout.login);
        tv = (TextView) findViewById(R.id.login_tv2);
        tv.setText(str);

        login_name=(EditText) findViewById(R.id.login_name);
        login_num=(EditText) findViewById(R.id.login_num);
        login_pwd=(EditText) findViewById(R.id.login_pwd);
        login_submit=(Button) findViewById(R.id.login_submit);
        //login_sign=(Button) findViewById(R.id.login_sign);
        get_ratify=(Button) findViewById(R.id.get_ratify);
        time = new TimeCount(60000, 1000);

        login_title=(TextView) findViewById(R.id.login_title);

        login_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validationBoxes()){
                    HttpConnector.login(login_num.getText().toString(), login_name.getText().toString(), login_pwd.getText().toString(),
                            new HttpListener() {
                                @Override
                                public void onHttpFinish(int state, String responseData) {
                                    if (state==-1){
                                        Log.e(TAG, "onHttpFinish: -------out time" );
                                        loginError();
                                        Toast.makeText(LoginActivity.this,"无法连接到服务器，请检查网络状态",Toast.LENGTH_SHORT).show();
                                    }else {
                                        if (responseData.equals("200")){
                                            editor=pref.edit();
                                            editor.putBoolean("isOnline",true);
                                            editor.putString("name",login_name.getText().toString());
                                            editor.putString("num",login_num.getText().toString());
                                            editor.apply();
                                            loginFinish();
                                        }else {
                                            Log.e(TAG, "onHttpFinish: ---------wrong pwd");
                                            loginError();
                                        }
                                    }
                                }
                            });
                }
            }
        });
        get_ratify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login_num.setError(null);
                String snum= login_num.getText().toString();
                View focusView = null;
                /**
                 * cancel : true if there is invalid message.
                 * the value of 'cancel' can conduct View.requestFocus().
                 */
                boolean cancel = false;

                if (TextUtils.isEmpty(snum)){
                    login_num.setError("此项不能为空！");
                    focusView = login_num;
                    cancel = true;
                } else if (!isPhoneNum(snum)){
                    login_num.setError("不存在此手机号码！");
                    focusView = login_num;
                    cancel = true;
                }

                if (cancel) {
                    focusView.requestFocus();
                } else {
                    time.start();// 开始计时
                    HttpConnector.getRatify(snum, new HttpListener() {
                        @Override
                        public void onHttpFinish(int state, String responseData) {
                            if (state == -1){
                                Toast.makeText(LoginActivity.this,"无法连接到服务器，请检查网络状态",Toast.LENGTH_SHORT).show();
                            } else {
                                //
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * check every boxes
     */
    private boolean validationBoxes(){
        /**
         * reset errors
         */
        login_name.setError(null);
        login_num.setError(null);
        login_pwd.setError(null);

        /**
         * Store values at the time of the login attempt.
         */
        String sname= login_name.getText().toString();
        String snum= login_num.getText().toString();
        String spwd = login_pwd.getText().toString();

        boolean cancel = false;
        View focusView = null;

        /**
         * check every boxes
         */
        // Passwords
        if (TextUtils.isEmpty(spwd)){
            login_pwd.setError("此项不能为空！");
            focusView = login_pwd;
            cancel = true;
        } else if (!hasOKLength(spwd)){
            login_pwd.setError("请输入4位验证码！");
            focusView = login_pwd;
            cancel = true;
        } else if (!hasSpecialCharacter(spwd)){
            login_pwd.setError("密码只允许数字或字母！");
            focusView = login_pwd;
            cancel = true;
        }

        // Phone number
        if (TextUtils.isEmpty(snum)){
            login_num.setError("此项不能为空！");
            focusView = login_num;
            cancel = true;
        } else if (!isPhoneNum(snum)){
            login_num.setError("不存在此手机号码！");
            focusView = login_num;
            cancel = true;
        }

        // Name
        if (TextUtils.isEmpty(sname)){
            login_name.setError("此项不能为空！");
            focusView = login_name;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        }
        return true;
    }

    private void loginFinish(){
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginError(){
        /**TextView warningText=(TextView) findViewById(R.id.textView8);
        warningText.setText("登陆失败！");
        warningText.setVisibility(View.VISIBLE);*/
        new AlertDialog.Builder(LoginActivity.this)
                .setMessage("登录失败，请输入正确的验证码")
                .setPositiveButton("确定", null)
                .create()
                .show();
    }

    private boolean hasOKLength(String str){
        if (str.length()!=4){
            return false;
        }
        return true;
    }
    private boolean hasSpecialCharacter(String s){
        if (s == null || s.trim().isEmpty()) {
            return false;
        }
        int i;
        char c;
        for (i=0;i<4;i++){
            c=s.charAt(i);
            if (!( (c>='0')&&(c<='9')|| (c>='A')&&(c<='Z')|| (c>='a')&&(c<='z') ) ){
                return false;
            }
        }
        return true;
    }

    private boolean isPhoneNum(String s){
        // the length should be valid
        if (s == null || s.trim().isEmpty() || s.length()!=11)
            return false;

        // s should have no dot (.)
        Pattern p = Pattern.compile("[^0-9]");
        Matcher m = p.matcher(s);
        boolean b = m.find();
        if (b == true)
            return false; // Should not have other character than number, so false if there is
        else
            return true;
    }

}

class TimeCount extends CountDownTimer {

    public TimeCount(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }
    @Override
    public void onFinish() {
        // TimeCount finishes.
        LoginActivity.get_ratify.setText("获取验证码");
        LoginActivity.get_ratify.setClickable(true);
        LoginActivity.get_ratify.setBackgroundResource(R.drawable.ratify_press_normal);
    }

    @Override
    public void onTick(long millisUntilFinished) {// being counting now
        LoginActivity.get_ratify.setClickable(false);
        LoginActivity.get_ratify.setText(millisUntilFinished / 1000 + "秒后可以重新发送");
        LoginActivity.get_ratify.setBackgroundResource(R.drawable.ratify_press_fobid);
    }
}
