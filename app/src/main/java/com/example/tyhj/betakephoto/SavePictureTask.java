package com.example.tyhj.betakephoto;

import android.os.AsyncTask;
import android.os.Environment;

import com.tyhj.myfist_2016_6_29.MyTime;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2015/4/8.
 */
public class SavePictureTask extends AsyncTask<byte[], String, String> {

    @Override
    protected String doInBackground(byte[]... data) {
        initStorage();
        String fileName =   new MyTime().getMonth_()+new MyTime().getDays() +new MyTime().getHour()
                +new MyTime().getMinute()+new MyTime().getSecond() +".jpg";
        String fullPath = Environment.getExternalStorageDirectory()+"/AMyPhoto/" + fileName;
        writeToFile(data[0], fullPath);
        return null;
    }

    void writeToFile(byte[] data, String fullPath) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fullPath);
            out.write(data);
            out.close();
        } catch (Exception e) {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
    }

    void initStorage() {
        File file = new File(Environment.getExternalStorageDirectory()+"/AMyPhoto");
        if (!file.exists())
            file.mkdirs();
    }
}
