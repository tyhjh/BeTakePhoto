package com.example.tyhj.betakephoto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(GetActivity.getActivity()==null) {
            Toast.makeText(context,"1",Toast.LENGTH_SHORT).show();
            Intent in = new Intent(context, Took.class);
            in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(in);
        }
    }
}
