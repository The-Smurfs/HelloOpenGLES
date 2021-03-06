package com.neop.helloopengles.Graphics;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.neop.helloopengles.R;
import com.neop.helloopengles.Utils.CameraHelper;
import com.neop.helloopengles.Utils.ShaderHelper;
import com.neop.helloopengles.Utils.TextureHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class MyMediaPlayer implements CameraHelper.CameraListener {
    private final float[] mVerticesData =
            {
                    -0.8f, 0.8f, 0.0f,
                    -0.8f, -0.8f, 0.0f,
                    0.8f, -0.8f, 0.0f,
                    0.8f, 0.8f, 0.0f
            };

    private final float[] mColorsData =
            {
                    1.0f, 0.0f, 0.0f,0.5f,
                    0.0f, 1.0f, 0.0f,0.5f,
                    0.0f, 0.0f, 1.0f,0.5f,
                    1.0f, 1.0f, 0.0f,0.5f
            };

    private final float[] mTextureData =
            {
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f
            };

    private final short[] mIndicesData =
            {
                    0, 1, 2,
                    0, 2, 3
            };
    private static final String TAG = MyMediaPlayer.class.getSimpleName();
    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_SHORT = 2;
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 4;
    private static final int TEXTURE_COMPONENT_COUNT = 2;
    private static final int INDEX_COMPONENT_COUNT = 1;


    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private Context mContext;
    private int mProgramObject;
    private int uTextureContainer, containerTexture;
    private int uTextureFace, faceTexture;
    private FloatBuffer mVerticesBuffer;
    private FloatBuffer mColorsBuffer;
    private FloatBuffer mTextureBuffer;
    private ShortBuffer mIndicesBuffer;
    private int mVAO, mVBO, mCBO, mTBO, mEBO;

    private int[] mYUVTextureIds = new int[3];
    ByteBuffer mBufferY,mBufferU,mBufferV, mBufferUV;
    int pixelWidth, pixelHeight;
    boolean mBufferChnaged;

    public MyMediaPlayer(Context context)
    {
        mContext = context;
        // 顶点buffer
        mVerticesBuffer = ByteBuffer.allocateDirect(mVerticesData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVerticesBuffer.put(mVerticesData).position(0);

        // 颜色
        mColorsBuffer = ByteBuffer.allocateDirect(mColorsData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mColorsBuffer.put(mColorsData).position(0);

        // 纹理坐标
        mTextureBuffer = ByteBuffer.allocateDirect(mTextureData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureBuffer.put(mTextureData).position(0);

        // 索引buffer
        mIndicesBuffer = ByteBuffer.allocateDirect(mIndicesData.length * BYTES_PER_SHORT)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndicesBuffer.put(mIndicesData).position(0);

        // 着色器程序
        mProgramObject = ShaderHelper.loadProgramFromAsset(mContext, "mediaplayer.vert", "mediaplayer.frag");

        int[] array = new int[1];
        GLES30.glGenVertexArrays(1, array, 0);
        mVAO = array[0];
        array = new int[4];
        GLES30.glGenBuffers(4, array, 0);
        mVBO = array[0];// 顶点
        mCBO = array[1];// 颜色
        mTBO = array[2];// 纹理
        mEBO = array[3];// 索引

        loadBufferData();

        TextureHelper.initYUVTextures(mYUVTextureIds);

        containerTexture = TextureHelper.loadTexture(mContext, R.mipmap.william);
        setMatrix();
    }

    private void loadBufferData() {
        GLES30.glBindVertexArray(mVAO);

        mVerticesBuffer.position(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, BYTES_PER_FLOAT * mVerticesData.length, mVerticesBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(0, POSITION_COMPONENT_COUNT, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(0);

        mColorsBuffer.position(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mCBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, BYTES_PER_FLOAT * mColorsData.length, mColorsBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(1, COLOR_COMPONENT_COUNT, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(1);

        mTextureBuffer.position(0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mTBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, BYTES_PER_FLOAT * mTextureData.length, mTextureBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(2, TEXTURE_COMPONENT_COUNT, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(2);

        mIndicesBuffer.position(0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mEBO);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, BYTES_PER_SHORT * mIndicesData.length, mIndicesBuffer, GLES30.GL_STATIC_DRAW);
    }

    private void loadYUVData()
    {
        InputStream is = null;
        try {
            is = mContext.getAssets().open("YUV_Image_840x1074.NV21");
        } catch (IOException e) {
            e.printStackTrace();
        }
        int lenght = 0;
        try {
            lenght = is.available();
            byte[] buffer = new byte[lenght];
            is.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try
            {
                is.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void play()
    {
        GLES30.glUseProgram(mProgramObject);

        // Load the vertex data

        uTextureContainer = GLES30.glGetUniformLocation(mProgramObject, "uTextureContainer");
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, containerTexture);
        GLES30.glUniform1i(uTextureContainer, 0);


        int uMatrixLocation = GLES30.glGetUniformLocation(mProgramObject, "uMVPMatrix");
        GLES30.glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0);

        if (mBufferChnaged) {
            int gvImageTextureY = GLES30.glGetUniformBlockIndex(mProgramObject, "yTexture");
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mYUVTextureIds[0]);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, pixelWidth, pixelHeight, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, mBufferY);
            GLES30.glUniform1i(gvImageTextureY, 0);

            int gvImageTextureUV = GLES30.glGetUniformBlockIndex(mProgramObject, "uvTexture");
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mYUVTextureIds[1]);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE_ALPHA, pixelWidth / 2, pixelHeight / 2, 0, GLES30.GL_LUMINANCE_ALPHA, GLES30.GL_UNSIGNED_BYTE, mBufferUV);
            GLES30.glUniform1i(gvImageTextureUV, 1);

            /*int gvImageTextureV = GLES30.glGetUniformBlockIndex(mProgramObject, "vTexture");
            GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mYUVTextureIds[2]);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, pixelWidth / 2, pixelHeight / 2, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, mBufferU);
            GLES30.glUniform1i(gvImageTextureV, 2);*/
        }


        GLES30.glBindVertexArray(mVAO);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, mIndicesData.length, GLES30.GL_UNSIGNED_SHORT, 0);
    }

    @Override
    public void getYUVData(ByteBuffer ChannelY, ByteBuffer ChannelUV, int width, int height) {
        pixelWidth = width;
        pixelHeight = height;
        mBufferY = ChannelY;
        mBufferUV = ChannelUV;
        //mBufferV = ChannelV;
        mBufferChnaged = true;
    }

    @Override
    public void onPreviewFrame(final byte[] data, int width, int height)
    {
        pixelWidth = width;
        pixelHeight = height;
        mBufferY = ByteBuffer.allocate(width*height).order(ByteOrder.nativeOrder());
        mBufferY.put(data, 0, width*height);
        mBufferY.position(0);
        mBufferU = ByteBuffer.allocate(width*height/4).order(ByteOrder.nativeOrder());
        mBufferU.put(data, width*height, width*height/4);
        mBufferU.position(0);
        mBufferV = ByteBuffer.allocate(width*height/4).order(ByteOrder.nativeOrder());
        mBufferV.put(data, width*height*5/4, width*height/4);
        mBufferV.position(0);
        mBufferChnaged = true;
    }

    private void setMatrix() {
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.rotateM(mMVPMatrix, 0, -90, 0f, 0f, 1f);
    }

}
