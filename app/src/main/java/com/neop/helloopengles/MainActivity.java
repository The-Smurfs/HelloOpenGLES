package com.neop.helloopengles;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.neop.helloopengles.Utils.CameraHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        MyGLSurfaceView surfaceView = new MyGLSurfaceView(this);
        setContentView(surfaceView);
        CameraHelper camera = new CameraHelper(this);
        surfaceView.getRender().setCamera(camera);
        //camera.addCameraListener(surfaceView.getRender().getMediaPlayer());
    }
}