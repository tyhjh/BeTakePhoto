package com.example.tyhj.betakephoto;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SignUpCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

/**
 * Created by _Tyhj on 2016/7/3.
 */
public class Took extends Activity implements ServiceConnection {
    public MyService.Binder binder;
    FrameLayout _previewContainer;
    CameraPreview _cameraPreview;
    int _front_camera_index = -1;
    int _back_camera_index = -1;
    int _currentCameraIndex = -1;
    int _cameraPictureRotation;
    Camera _camera;
    boolean _isReadToGo = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AVOSCloud.initialize(this, "sWU1ERjPGATpOg0d6dSzxfhB-gzGzoHsz", "zqLWPb25PoVCNHBSTIS6mQlj");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _previewContainer = (FrameLayout) findViewById(R.id.flCameraContainer);
        initCamera();
        readyToGo();
        AVUser.logInInBackground("13678141943", "444444", new LogInCallback() {
                    public void done(AVUser user, AVException e) {
                        if (e == null) {
                            build();
                            System.out.println("踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩");
                        }
                    }
                });
    }

    public void TakenPicture() {
        if (_isReadToGo)
            _camera.takePicture(null, null, pictureCallback);
    }
    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
           new SavePictureTask().execute(data);
        }
    };

    void initCamera() {
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
                _back_camera_index = i;
            else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                _front_camera_index = i;
        }
        _currentCameraIndex = _back_camera_index;
    }
    private void readyToGo() {

        if (_isReadToGo)
            return;

        if (_camera == null) {
            try {
                _camera = Camera.open(_currentCameraIndex);
            } catch (Exception e) {
                Toast.makeText(Took.this, "摄像头被其他程序占用，请关闭之后，再启动本程序", Toast.LENGTH_SHORT).show();
                _isReadToGo = false;
                return;
            }
        }

        Camera.Parameters parameters = _camera.getParameters();

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        if (_currentCameraIndex == _back_camera_index) {
            _cameraPictureRotation = 90;
            //set preview to right orientation
            // _camera.setDisplayOrientation(90);
        } else {
            _cameraPictureRotation = 270;
        }
        parameters.setRotation(_cameraPictureRotation);
        _camera.setParameters(parameters);
        _cameraPreview = new CameraPreview(this, _camera);
        _previewContainer.addView(_cameraPreview);
        _isReadToGo = true;
    }
    private void relax() {
        if (_isReadToGo == false)
            return;
        if (_camera != null) {
            _camera.stopPreview();
            _camera.release();
            _camera = null;
        }
        _previewContainer.removeView(_cameraPreview);
        _isReadToGo = false;
        initCamera();
        readyToGo();
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    TakenPicture();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                handler.sendEmptyMessage(2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    break;
                case 2:
                    relax();
                    try {
                        savaPhoto();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
    //绑定服务
    public void build(){
        bindService(new Intent(this, MyService.class), Took.this, Context.BIND_AUTO_CREATE);
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder= (MyService.Binder) service;
        binder.getService().setCallback(new MyService.Callback() {
            @Override
            public void onDataChange(final int str) {
               handler.sendEmptyMessage(1);
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
    private void savaPhoto() throws FileNotFoundException {
        AVObject obj=new AVObject("CanGet");
        File getfile=isFind( new File(Environment.getExternalStorageDirectory()+"/AMyPhoto"));
        if(!getfile.exists()){
            getfile.mkdirs();
        }
        AVFile file = AVFile.withAbsoluteLocalPath("myphoto",getfile.getAbsolutePath());
        obj.put("Myphoto",file);
        obj.saveInBackground();
    }
    private File isFind(File file) {
        if(file.isDirectory()){
            File next[] =file.listFiles();
            for(int i=1;i<next.length;i++){
                if(next[i].isFile()){
                    if(next[0].lastModified()<next[i].lastModified()){
                        next[0]=next[i];
                    }
                }
            }
            return next[0];
        }else
            return null;
    }
}
