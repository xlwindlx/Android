package com.example.selfshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


// 카메라의 실질적인 동작을 정의해놓은 함수들이 모아진 클래스

public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback{

	// 멤버 변수들
	SurfaceHolder m_SurfaceHolder;
	Parameters m_Parameters;
	boolean m_StateImageCall = false;
	private int m_nCurrentFacing;
	private Singleton m_Singleton;
	private int m_nZoomNumber, m_nExposureNumber = 0;	
	public Size m_OptimalPreviewSize, m_OptimalPictureSize;


	public CameraSurface(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		m_SurfaceHolder = getHolder();
		m_SurfaceHolder.addCallback(this);
		m_SurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		m_Singleton = Singleton.getInstance();		
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
				
	}

	// 카메라 줌 확대 부분
	public void ZoomPlus()
	{
		int maxZoom = m_Parameters.getMaxZoom();
		//int oneTenthZoom = maxZoom / 10;

		if (m_nZoomNumber < maxZoom) {
			//m_nZoomNumber += oneTenthZoom;		
			m_Parameters.setZoom(++m_nZoomNumber);
			m_Singleton.m_Camera.setParameters(m_Parameters);
		}
	}

	// 카메라 줌 확대 부분
	public void ZoomMinus(){
		//int maxZoom = m_Parameters.getMaxZoom();
		//int oneTenthZoom = maxZoom / 10;

		if (m_nZoomNumber > 0) {
			//m_nZoomNumber -= oneTenthZoom;
			m_Parameters.setZoom(--m_nZoomNumber);
			m_Singleton.m_Camera.setParameters(m_Parameters);			
		}
	}

	// 카메라 밝기 조절(상승)
	public void BrightPlus(){
		int maxBright = m_Parameters.getMaxExposureCompensation();

		if (m_nExposureNumber < maxBright) {		
			m_Parameters.setExposureCompensation(++m_nExposureNumber);
			m_Singleton.m_Camera.setParameters(m_Parameters);
		}		
	}


	public void BrightMinus(){
		int minusBright = m_Parameters.getMinExposureCompensation();

		if (m_nExposureNumber > minusBright) {
			m_Parameters.setExposureCompensation(--m_nExposureNumber);
			m_Singleton.m_Camera.setParameters(m_Parameters);			
		}
	}

	// 카메라 전환 후, 카메라를 다시 open()한다.
	private void openCamera(int facing) {
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		int cameraCount = Camera.getNumberOfCameras();
		for (int i = 0; i < cameraCount; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == facing) {
				try {
					m_Singleton.m_Camera = Camera.open(i);
				} catch (RuntimeException e) {
					Log.e(VIEW_LOG_TAG, "Camera failed to open: " + e.getLocalizedMessage());
				}

				m_nCurrentFacing = facing;
			}
		}
	}

	// 스마트폰마다 다른 크기의 해상도를 가지고 있으므로, 카메라 프리뷰화면의 최적화를 위한 부분
	private Size getOptimalPreviewSize(List<Size> listSize) {
		
		float ratio = (float) m_Singleton.m_DisplaySize.width / (float) m_Singleton.m_DisplaySize.height;
		Size currentSize = listSize.get(0);
		
		for (Size size : listSize) {
			if ((float) size.width / (float) size.height == ratio) {
				if (currentSize == null || currentSize.width * currentSize.height < (size.width * size.height))
					currentSize = size;
			}
		}
		return currentSize;
	}

	// 스마트폰마다 다른 크기의 해상도를 가지고 있으므로, 카메라 픽쳐화면의 최적화를 위한 부분
		private Size getOptimalPictureSize(List<Size> listSize) {
			
			float ratio = (float) m_Singleton.m_DisplaySize.width / (float) m_Singleton.m_DisplaySize.height;
			Size currentSize = listSize.get(0);
			
			for (Size size : listSize) {
				if ((float) size.width / (float) size.height == ratio) {
					if (currentSize == null || currentSize.width * currentSize.height < (size.width * size.height))
						currentSize = size;
				}
			}
			return currentSize;
		}


	// 카메라 전후면 전환을 세팅하는 부분
	public void cameraChange()
	{
		m_Singleton.m_Camera.stopPreview();
		m_Parameters = m_Singleton.m_Camera.getParameters();

		List<Size> listPreviewSize = m_Parameters.getSupportedPreviewSizes();
		m_OptimalPreviewSize = getOptimalPreviewSize(listPreviewSize);
		
		List<Size> listPictureSize = m_Parameters.getSupportedPictureSizes();
		m_OptimalPictureSize = getOptimalPictureSize(listPictureSize);
		
		m_Parameters.setPreviewSize(m_OptimalPreviewSize.width, m_OptimalPreviewSize.height);
		
		m_Parameters.setPictureSize(m_OptimalPictureSize.width, m_OptimalPictureSize.height);
		// setPictureSize 함수 만들기!!
		//m_Parameters.setPictureSize(3264, 2448);		
		
		m_Singleton.m_Camera.setParameters(m_Parameters);
		m_Singleton.m_Camera.setPreviewCallback(mPreviewCallback);
		m_Singleton.m_Camera.startPreview();
	}
	
	public void changeCamera() {
		if (m_Singleton.m_Camera != null) {
			m_Singleton.m_Camera.stopPreview();
			m_Singleton.m_Camera.setPreviewCallback(null);
			m_Singleton.m_Camera.release();
			m_Singleton.m_Camera = null;
		}

		if (m_nCurrentFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			
			openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
		} else {
			openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
		}

		try {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
				
			} else {
				Parameters parameters = m_Singleton.m_Camera.getParameters();
			
				m_Singleton.m_Camera.setParameters(parameters);
			}

			m_Singleton.m_Camera.setPreviewDisplay(m_SurfaceHolder);
		} catch (Exception e) {
			Log.e(VIEW_LOG_TAG, "Failed to set camera preview.", e);
			m_Singleton.m_Camera.release();
		}

		m_Singleton.m_Camera.startPreview();
		
	}
	

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		try {
			m_Singleton.m_Camera.setPreviewDisplay(m_SurfaceHolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub		
	}

	// 블루투스 연결 후, 리모트 화면으로 실시간 화면전송 시작을 알리는 부분
	public void SetStateImageCall() {
		m_StateImageCall = true;
	}


	// 리모트 화면으로 실시간 화면전송을 하는 부분
    PreviewCallback mPreviewCallback = new PreviewCallback()  {
    	
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			
			if(m_StateImageCall == true){
				
				Camera.Parameters param = camera.getParameters();
				
				int width = param.getPreviewSize().width;
				int height = param.getPreviewSize().height;

				// 실시간 카메라 프리뷰화면의 화질 손실을 최소화하며, 용량을 줄이기 위해 몇번의 파일 변환을 거침.
				// 카메라API 내의 yuv파일로부터 파일 변환이 시작된다.

				// yuv파일 -> Jpeg파일 -> Bitmap파일(화질의 약1/8이나 1/16로 축소) -> Jpeg파일 -> byte[]로 변환 -> .write(byte[])로 전송
				// 이런식의 변환을 했을 때, 원본의 화질보다 떨어지지만, 이미지 용량의 크기가 500000~600000byte -> 10000~20000byte로 대폭감소.
				// 감소된 이미지를 한번 전송하는 데에 약 0.3~0.5초.
				YuvImage yuv = new YuvImage(data, param.getPreviewFormat(), width, height, null);
				data = null;

				ByteArrayOutputStream output = new ByteArrayOutputStream();
				yuv.compressToJpeg(new Rect(0, 0, width, height), 100, output);

				BitmapFactory.Options option = new BitmapFactory.Options();

				// 1/8로 축소
				option.inSampleSize = 8;
				option.inPurgeable = true;
				option.inDither = false;
				option.inPreferredConfig = Bitmap.Config.RGB_565;

				Bitmap bitmapImage = BitmapFactory.decodeByteArray(output.toByteArray(), 0, output.toByteArray().length, option);

				output.reset();
				bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, output);		
				
				
				byte[] buffer = output.toByteArray();
				
				int byteLength = buffer.length + 1;
				
				byte first = (byte)  (byteLength/256/256);
				byte second = (byte) ((byteLength/256)%256);
				byte third = (byte) (byteLength%256);				
				byte[] command = {first,second,third,7};
				
				byte[] send = new byte[command.length + buffer.length];				
				
				System.arraycopy(command, 0, send, 0, command.length);
				
				System.arraycopy(buffer, 0, send, 4, buffer.length);	
								
				m_StateImageCall = false;	
				
				m_Singleton.mChatService.write(send);
																													
			}
			
		}	
    };


}
