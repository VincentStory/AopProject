package com.vincent.aop.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.vincent.aop.project.annotation.LoginCheck;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "aop_tag >>> ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }



    @LoginCheck
    public void myInfomation(View view) {
        Log.e(TAG, "开始跳转到 -> 我的资料 Activity");
    }

    @LoginCheck
    public void myMoney(View view) {
        Log.e(TAG, "开始跳转到 -> 我的余额 Activity");
    }

    @LoginCheck(title = "登录检查",isSaveRequestData = false)
    public void myScore(View view) {
        Log.e(TAG, "开始跳转到 -> 我的积分 Activity");
    }


}