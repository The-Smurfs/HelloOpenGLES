package com.neop.helloopengles.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraHelper {
    Activity mActivity;
    private CameraManager mCameraManager;
    private Handler mHandler;
    private String mCameraId;
    private ImageReader mImageReader;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mSession;
    private Handler mainHandler;

    CameraListener mCameraListener;

    public CameraHelper(Activity activity)
    {
        mActivity = activity;
        // 申请动态权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mActivity.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        initCameraAndPreview();
    }

    public void initCameraAndPreview() {
        try {
            HandlerThread handlerThread = new HandlerThread("My First Camera2");
            handlerThread.start();
            mHandler = new Handler(handlerThread.getLooper());// 用来处理普通线程
            mainHandler = new Handler(mActivity.getMainLooper());//用来处理ui线程的handler，即ui线程

            // 定义ImageReader用来读取拍摄的图像，并设置相关监听
            mImageReader = ImageReader.newInstance(640, 480, ImageFormat.YUV_420_888,/*maxImages*/7);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mainHandler);//这里必须传入mainHandler，因为涉及到了Ui操作

            if (ActivityCompat.checkSelfPermission(mActivity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //return;//按理说这里应该有一个申请权限的过程，但为了使程序尽可能最简化，所以先不添加
            }

            mCameraId = "" + CameraCharacteristics.LENS_FACING_FRONT;// 设置前置/后置摄像头
            mCameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
            mCameraManager.openCamera(mCameraId, deviceStateCallback, mHandler);// openCamera打开相机
        } catch (CameraAccessException e) {
            Toast.makeText(mActivity, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            /*if (mCameraListener != null) {
                mCameraListener.onPreviewFrame(YUV_420_888_data(image), image.getWidth(), image.getHeight());
            }*/
            int width = image.getWidth();
            int height = image.getHeight();
            ByteBuffer buffer_y = image.getPlanes()[0].getBuffer();
            ByteBuffer buffer_u = image.getPlanes()[1].getBuffer();
            ByteBuffer buffer_v = image.getPlanes()[2].getBuffer();
            int pixelStride = image.getPlanes()[1].getPixelStride();
            int rowStride = image.getPlanes()[1].getRowStride();
            ByteBuffer buffer_uv = ByteBuffer.allocate(640*240);
            byte[] bytes = new byte[buffer_v.remaining()];
            buffer_v.get(bytes);//将image对象转化为byte，再转化为bitmap
            buffer_uv.put(bytes);
            buffer_uv.put(buffer_u.get(640*240-2));
            buffer_uv.position(0);
            if(mCameraListener != null)
                mCameraListener.getYUVData(buffer_y, buffer_uv, 640,480);
            image.close();
        }
    };

    private CameraDevice.StateCallback deviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {// 定义相机设备打开后的执行操作
            mCameraDevice = camera;
            try {
                takePreview();// 设备连接后设置预览
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Toast.makeText(mActivity, "打开摄像头失败", Toast.LENGTH_SHORT).show();
        }
    };

    // 预览，通过CameraDevice的createCaptureSession方法实现
    // 经过这步，就可以看到实时的拍摄画面预览
    public void takePreview() throws CameraAccessException {
        // 在安卓端和相机硬件端建立通道，进行信息的交换
        // 参数一表示图形数据会被输出到这两个地方
        // 参数二是会话建立后的回调
        mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()), mSessionPreviewStateCallback, mHandler);
    }

    // 预览回调
    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mSession = session;
            //配置完毕，调用CameraCaptureSession的setRepeatingRequest方法开始预览
            try {
                // 创建相机预览模式的captureRequest
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                // request设置参数形成的图像数据将会保存在指定的surfaceView对应的surface
                mPreviewBuilder.addTarget(mImageReader.getSurface());
                /**
                 * 设置你需要配置的参数
                 */
                //自动对焦
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //打开闪光灯
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                //无限次的重复获取图像
                mSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Toast.makeText(mActivity, "配置失败", Toast.LENGTH_SHORT).show();
        }
    };

    public interface CameraListener
    {
        public void getYUVData(final ByteBuffer ChannelY, final ByteBuffer ChannelUV, int width, int height);

        public void onPreviewFrame(final byte[] data, int width, int height);
    }

    public void addCameraListener(CameraListener listener)
    {
        mCameraListener = listener;
    }

    public static byte[] YUV_420_888_data(Image image) {
        final int imageWidth = image.getWidth();
        final int imageHeight = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[imageWidth * imageHeight *
                ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
        int offset = 0;

        for (int plane = 0; plane < planes.length; ++plane) {
            final ByteBuffer buffer = planes[plane].getBuffer();
            final int rowStride = planes[plane].getRowStride();
            // Experimentally, U and V planes have |pixelStride| = 2, which
            // essentially means they are packed.
            final int pixelStride = planes[plane].getPixelStride();
            final int planeWidth = (plane == 0) ? imageWidth : imageWidth / 2;
            final int planeHeight = (plane == 0) ? imageHeight : imageHeight / 2;
            if (pixelStride == 1 && rowStride == planeWidth) {
                // Copy whole plane from buffer into |data| at once.
                buffer.get(data, offset, planeWidth * planeHeight);
                offset += planeWidth * planeHeight;
            } else {
                // Copy pixels one by one respecting pixelStride and rowStride.
                byte[] rowData = new byte[rowStride];
                for (int row = 0; row < planeHeight - 1; ++row) {
                    buffer.get(rowData, 0, rowStride);
                    for (int col = 0; col < planeWidth; ++col) {
                        data[offset++] = rowData[col * pixelStride];
                    }
                }
                // Last row is special in some devices and may not contain the full
                // |rowStride| bytes of data.
                // See http://developer.android.com/reference/android/media/Image.Plane.html#getBuffer()
                buffer.get(rowData, 0, Math.min(rowStride, buffer.remaining()));
                for (int col = 0; col < planeWidth; ++col) {
                    data[offset++] = rowData[col * pixelStride];
                }
            }
        }

        return data;
    }
}
