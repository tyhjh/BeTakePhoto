package com.example.tyhj.betakephoto;

import android.app.Activity;

/**
 * Created by _Tyhj on 2016/7/4.
 */
public class GetActivity {
    public  static  Activity activity1=null;
    public static void addActivity(Activity activity){
        activity1=activity;
    }
    public static Activity getActivity(){
        return activity1;
    }
}
