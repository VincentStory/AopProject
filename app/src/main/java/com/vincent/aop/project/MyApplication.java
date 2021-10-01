package com.vincent.aop.project;

import android.app.Application;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
    }


    public static boolean isLogin() {
        return false;
    }
}
