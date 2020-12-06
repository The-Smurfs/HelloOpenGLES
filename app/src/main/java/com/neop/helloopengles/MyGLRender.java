package com.neop.helloopengles;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.neop.helloopengles.Graphics.MyMediaPlayer;
import com.neop.helloopengles.Graphics.Triangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRender implements GLSurfaceView.Renderer {
    Context mContext;
    Triangle triangle;
    MyMediaPlayer mediaplayer;
    public MyGLRender(Context context)
    {
        mContext = context;
    }

    public void finalize()
    {
        //triangle.Destroy();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //指定刷新颜色缓冲区时所用的颜色
        //需要注意的是glClearColor只起到Set的作用，并不Clear。
        //glClearColor更类似与初始化，如果不做，新的绘制就会绘制在以前的上面，类似于混合，而不是覆盖
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        //triangle = new Triangle(mContext);
        mediaplayer = new MyMediaPlayer(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //glViewport用于告诉OpenGL应把渲染之后的图形绘制在窗体的哪个部位、大小
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //glClear表示清除缓冲  传入GL_COLOR_BUFFER_BIT指要清除颜色缓冲
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        //triangle.Draw();
        mediaplayer.play();
    }
}
