package com.neop.helloopengles;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class MyGLSurfaceView  extends GLSurfaceView {
    Context mContext;
    MyGLRender mRender;
    public MyGLSurfaceView(Context context) {
        this(context, null);
        mContext = context;
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init()
    {
        // 设置EGLContext客户端使用OpenGL ES3.0版本
        setEGLContextClientVersion(3);
        // 设置渲染器到surfaceView上
        mRender = new MyGLRender(mContext);
        setRenderer(mRender);
    }

    public MyGLRender getRender()
    {
        return mRender;
    }
}
