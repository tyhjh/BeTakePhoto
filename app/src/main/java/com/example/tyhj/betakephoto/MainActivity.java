package com.example.tyhj.betakephoto;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.tyhj.myfist_2016_6_29.MyTime;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    private static final SparseArray ORIENTATIONS = new SparseArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private AotoFitTextview textureview;
    //摄像头Id
    private String mCameraId = "0";
    //摄像头成员变量
    private CameraDevice cameraDevice;
    //预览尺寸
    private Size previewsize;
    //预览照片的请求
    private CaptureRequest.Builder previewRequestBuilder;
    //
    private CaptureRequest previewRequest;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader imageReader;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //当TextureView可用的时候
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            MainActivity.this.cameraDevice = camera;
            //开始预览
            createCameraPreviewSession();
        }

        //摄像头链接断开时
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
            MainActivity.this.cameraDevice = null;
        }

        //摄像头打开错误时
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            MainActivity.this.cameraDevice = null;
            MainActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       /* textureview = (AotoFitTextview) findViewById(R.id.textrue);
        textureview.setSurfaceTextureListener(mSurfaceTextureListener);
        findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureStillPicture();
            }
        });*/

    }

    private void captureStillPicture() {
        try {
            if (cameraDevice == null) {
                return;
            }
            //创建作为拍照的CaptureRequest.Builder
            final CaptureRequest.Builder captureRequestBuild = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //将imageReader的surface作为CaptureRequest.Builder目标
            captureRequestBuild.addTarget(imageReader.getSurface());
            //设置自动对焦模式
            captureRequestBuild.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            //设置自动曝光模式
            captureRequestBuild.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            //获取设备方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            //根据设备方向计算照片方向
            captureRequestBuild.set(CaptureRequest.JPEG_ORIENTATION, (Integer) ORIENTATIONS.get(rotation));
            //停止连续取景
            cameraCaptureSession.stopRepeating();
            //捕获静态图片
            cameraCaptureSession.capture(captureRequestBuild.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    try {
                        //重设自动对焦
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        //打开连续取景模式
                        cameraCaptureSession.setRepeatingRequest(previewRequest, null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //打开摄像头
    private void openCamera(int width, int height) {
        setUpCameraOutPuts(width, height);
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //打开摄像头
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(mCameraId, stateCallback, null);

        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSession() {
        try{
            final SurfaceTexture texture=textureview.getSurfaceTexture();
            texture.setDefaultBufferSize(previewsize.getWidth(),previewsize.getHeight());
            //创建作为预览的Capture.Builder
            previewRequestBuilder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //将texture的surfaceView作为Capture.Builder的目标
            previewRequestBuilder.addTarget(new Surface(texture));
            Surface surface=new Surface(texture);
            cameraDevice.createCaptureSession(Arrays.asList(surface,imageReader.getSurface()),new CameraCaptureSession.StateCallback(){

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    if(null==cameraDevice){
                        return;
                    }
                    //当摄像头准备好
                    cameraCaptureSession=session;
                    try{
                        //设置自动对焦模式
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        //设置自动曝光模式
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        previewRequest=previewRequestBuilder.build();
                        cameraCaptureSession.setRepeatingRequest(previewRequest,null,null);
                    }catch (CameraAccessException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(MainActivity.this,"配置失败",Toast.LENGTH_SHORT).show();
                }
            },null);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void setUpCameraOutPuts(int width, int height) {
        CameraManager manager= (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics characteristics=manager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map=characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size largest= Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),new ComparaSizeByArea());
            imageReader=ImageReader.newInstance(largest.getWidth(),largest.getHeight(),ImageFormat.JPEG,2);
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener(){

                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image=reader.acquireNextImage();
                    ByteBuffer buffer= image.getPlanes()[0].getBuffer();
                    byte[] bytes=new byte[buffer.remaining()];
                    File file1=new File(Environment.getExternalStorageDirectory()+"/MyPhoto");
                    if(!file1.exists()){
                        file1.mkdirs();
                    }
                    File file=new File(Environment.getExternalStorageDirectory()+"/MyPhoto", new MyTime().getYear()+
                            new MyTime().getMonth_()+new MyTime().getDays()+new MyTime().getHour()+new MyTime().getMinute()+new MyTime().getSecond()
                            +".jpg");
                    buffer.get(bytes);
                    try(FileOutputStream outputs=new FileOutputStream(file)){
                        outputs.write(bytes);
                        Toast.makeText(MainActivity.this,"保存"+file,Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    finally {
                        image.close();
                    }
                }
            },null);
            //
            previewsize=chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),width,height,largest);
            int orientation=getResources().getConfiguration().orientation;
            if(orientation== Configuration.ORIENTATION_LANDSCAPE){
                textureview.setAspectRatio(previewsize.getWidth(),previewsize.getHeight());
            }else {
                textureview.setAspectRatio(previewsize.getHeight(),previewsize.getWidth());
            }
        }catch (CameraAccessException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            System.out.print("出现错误");
        }
    }
    private static Size chooseOptimalSize(Size[] choices,int width,int height,Size aspectRatio){
        List<Size> big=new ArrayList<>();
        int w=aspectRatio.getWidth();
        int h=aspectRatio.getHeight();
        for(Size option:choices){
            if(option.getHeight()==option.getWidth()*h/w&&option.getWidth()>=width&&option.getHeight()>=height){
                big.add(option);
            }
        }
        if(big.size()>0){
            return Collections.min(big,new ComparaSizeByArea());
        }else{
            return choices[0];
        }
    }
    static class ComparaSizeByArea implements Comparator<Size>{

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth()*lhs.getHeight()-(long)rhs.getWidth()*rhs.getHeight());
        }
    }
}
