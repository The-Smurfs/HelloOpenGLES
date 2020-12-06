package com.neop.helloopengles.Graphics;

import android.content.Context;
import android.opengl.GLES30;

import com.neop.helloopengles.Utils.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Triangle {

    private static final int BYTES_LEN_FLOAT = 4;
    private static final int BYTES_LEN_SHORT = 2;
    // Member variables
    private int mProgramObject;
    private FloatBuffer mVertices;
    private static String TAG = "Graphic_Triangle";

    private final float[] mVerticesData =
            {0.0f, 0.6f, 0.0f, -1f, -0.8f, 0.0f, 1f, -0.8f, 0.0f};
    private int VAO, VBO, EBO;

    public Triangle(Context mContext)
    {
        // 初始化形状中顶点坐标数据的字节缓冲区
        // allocateDirect: 获取到 DirectByteBuffer 实例,参数是坐标所占字节，每个float占4个字节
        // order: 设置ByteBuffer的字节序为当前硬件平台的字节序
        // asFloatBuffer: 通过ByteBuffer中获得一个浮点缓冲区
        mVertices = ByteBuffer.allocateDirect(mVerticesData.length * BYTES_LEN_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        // 存储顶点坐标信息到FloatBuffer
        // 设置缓冲区从第一个位置读取顶点坐标
        mVertices.put(mVerticesData).position(0);

        // 生成顶点Buffer


        mProgramObject = ShaderHelper.loadProgramFromAsset(mContext, "hellotriangle.vert", "hellotriangle.frag");
    }

    public void Draw()
    {
        GLES30.glUseProgram(mProgramObject);

        // Load the vertex data
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, mVertices);
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
    }

    public void Destroy()
    {

    }
}
