package com.example.selfshot;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class BtActivity extends Activity {
	private LinearLayout m_remoteview = null;
	private Button shutterbtn;
	private Button ZoomPlus;
	private Button ZoomMinus;
	private Button Shineplusbtn;
	private Button Shineminusbtn;
	private Button receiveButton;
	private Button connectmotor;
	private Button connectedmotor;

	
	CameraSurface m_CameraSurface;
	
    public ImageView mInPhoto;
    
    //public TextView m_textLog;
    
 // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
 // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;
    
 // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    
    private String mConnectedDeviceName = null;
    
    private BluetoothChatService mChatService = null;

    String m_strLog = "";

	// 이 클래스는 블루투스 연결 후, 원격조작하는 리모트 부분이다..
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetoothpreview);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
//		Intent intent = getIntent();
		
		
		Singleton.getInstance().m_BtActivity = this;
		mInPhoto = (ImageView) findViewById(R.id.inPhoto);
		mInPhoto.setImageBitmap(null);
		
		mChatService = new BluetoothChatService(this, mHandler);
		
		// Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

		// 실시간으로 전송받을 상대 스마트폰의 카메라 프리뷰 화면을 받기위한 이미지뷰의 크기를 스마트폰 크기에 맞게 설정하는 부분
		LinearLayout.LayoutParams mInPhotoParams = (LinearLayout.LayoutParams) mInPhoto.getLayoutParams();
		mInPhotoParams.height = (this.getWindowManager().getDefaultDisplay().getWidth()) * 9 / 16;
		mInPhoto.setLayoutParams(mInPhotoParams);
		
		m_remoteview = (LinearLayout) findViewById(R.id.remoteview);

		// 블루투스 데이터 송수신 중에 송신의 프로트콜이 정의된 부분
		
		int byteLength = 1;
		
		byte first = (byte)  (byteLength/256/256);
		byte second = (byte) ((byteLength/256)%256);
		byte third = (byte) (byteLength%256);				
		byte[] data = {first,second,third,6};
		
		Singleton.getInstance().mChatService.write(data);
						
		
		shutterbtn = (Button) findViewById(R.id.remoteshutterbtn);
		shutterbtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int byteLength = 1;
				
				byte first = (byte)  (byteLength/256/256);
				byte second = (byte) ((byteLength/256)%256);
				byte third = (byte) (byteLength%256);				
				byte[] data = {first,second,third,1};
				Singleton.getInstance().mChatService.write(data);
			}
		});
		
		ZoomPlus = (Button) findViewById(R.id.remotezoomplusbtn);
		ZoomPlus.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int byteLength = 1;
				
				byte first = (byte)  (byteLength/256/256);
				byte second = (byte) ((byteLength/256)%256);
				byte third = (byte) (byteLength%256);				
				byte[] data = {first,second,third,2};
				Singleton.getInstance().mChatService.write(data);
			}
		});
		
		ZoomMinus = (Button) findViewById(R.id.remotezoomminusbtn);
		ZoomMinus.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub				
				int byteLength = 1;
				
				byte first = (byte)  (byteLength/256/256);
				byte second = (byte) ((byteLength/256)%256);
				byte third = (byte) (byteLength%256);				
				byte[] data = {first,second,third,3};
				Singleton.getInstance().mChatService.write(data);
			}
		});
		
		Shineplusbtn = (Button) findViewById(R.id.shineplusbtn);
		Shineplusbtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int byteLength = 1;
				
				byte first = (byte)  (byteLength/256/256);
				byte second = (byte) ((byteLength/256)%256);
				byte third = (byte) (byteLength%256);				
				byte[] data = {first,second,third,4};
				
				Singleton.getInstance().mChatService.write(data);
			}
		});
		
		Shineminusbtn = (Button) findViewById(R.id.shineminusbtn);
		Shineminusbtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int byteLength = 1;
				
				byte first = (byte)  (byteLength/256/256);
				byte second = (byte) ((byteLength/256)%256);
				byte third = (byte) (byteLength%256);				
				byte[] data = {first,second,third,5};
				
				Singleton.getInstance().mChatService.write(data);
			}
		});
		
//		receiveButton = (Button) findViewById(R.id.recvpicture);
//		receiveButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				int byteLength = 1;
//				
//				byte first = (byte)  (byteLength/256/256);
//				byte second = (byte) ((byteLength/256)%256);
//				byte third = (byte) (byteLength%256);				
//				byte[] data = {first,second,third,6};
//				
//				Singleton.getInstance().mChatService.write(data);
//			}
//		});
		
		connectmotor = (Button) findViewById(R.id.motorconnect);
		connectmotor.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent serverIntent = new Intent(BtActivity.this, DeviceListActivity2.class);
	            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);				
			}
		});
		
		connectedmotor = (Button) findViewById(R.id.motorconnected);
		connectedmotor.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String command = "z";
				byte[] send = command.getBytes();
	            mChatService.write(send);
				
				mChatService.stop();
				
				connectmotor.setVisibility(View.VISIBLE);
            	connectedmotor.setVisibility(View.INVISIBLE);

			}
		});
		

		
		connectedmotor.setVisibility(View.INVISIBLE);


		//m_textLog = (TextView) findViewById(R.id.textLog);
	}

	
//	@Override
//	protected void onDestroy() {
//		// TODO Auto-generated method stub
//		//super.onDestroy();
//		int byteLength = 1;
//		
//		byte first = (byte)  (byteLength/256/256);
//		byte second = (byte) ((byteLength/256)%256);
//		byte third = (byte) (byteLength%256);				
//		byte[] data = {first,second,third,8};
//		
//		Singleton.getInstance().mChatService.write(data);
//		
//		Singleton.getInstance().mChatService.stop();
//		Singleton.getInstance().mChatService = null;
//		
//		Intent intent = new Intent();
//		intent.putExtra("INPUT_TEXT", "camera return");
//		setResult(RESULT_OK, intent);
//		
//	}
			
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Back 버튼이 눌렸을 때, 블루투스 연결 전, 카메라 시작으로 되돌리는 부분
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// TODO Auto-generated method stub
		
		int byteLength = 1;
		
		byte first = (byte)  (byteLength/256/256);
		byte second = (byte) ((byteLength/256)%256);
		byte third = (byte) (byteLength%256);				
		byte[] data = {first,second,third,8};
		
		Singleton.getInstance().mChatService.write(data);
		
		Singleton.getInstance().mChatService.stop();
		
//        Intent intent = new Intent();
//		intent.putExtra("INPUT_TEXT", "camera return");
//		setResult(RESULT_OK, intent);
		finish();
		
	}

	// 실시간 카메라 프리뷰를 Byte[]로 받아서 이미지로 변환 후, 이미지뷰에 지정하는 부분
	public void SetView(Byte[] recvBuffer) 
	{
		// Byte 형 데이터를 byte 형으로 바꾸기 위해 선언
		byte[] arrJpeg = new byte[recvBuffer.length];

		// Byte 형 데이터를 byte 형으로 변경
		// decodeByteArray 에서는 byte 만 가능하기 때문
		for (int i = 0; i < recvBuffer.length; i++)
		{
			arrJpeg[i] = recvBuffer[i].byteValue();
		}
		
    	Bitmap Image = BitmapFactory.decodeByteArray(arrJpeg, 0, arrJpeg.length);
    	
    	mInPhoto.setImageBitmap(Image); 
    	
    	int byteLength = 1;
		
		byte first = (byte)  (byteLength/256/256);
		byte second = (byte) ((byteLength/256)%256);
		byte third = (byte) (byteLength%256);				
		byte[] data = {first,second,third,6};
		
		Singleton.getInstance().mChatService.write(data);
    	//receiveButton.performClick();
    		  	   	
	}
	
//	// 파일 전송률을 실시간으로 확인하기 위함. ( 단위 byte )
//    public void logToast(String str) {
//    	Log.i("TESTTTT", str);
//    	
//    	m_strLog = str;
//    	
//    	runOnUiThread(new Runnable() {
//    	    public void run() {
//    	    	
//    	    	m_textLog.setText(m_strLog);
//    	    }
//    	});
//	}
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
        
    }
    
 // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
//                    mTitle.setText(R.string.title_connected_to);
//                    mTitle.append(mConnectedDeviceName);
//                    mConversationArrayAdapter.clear();
                	
                	connectmotor.setVisibility(View.INVISIBLE);
                	connectedmotor.setVisibility(View.VISIBLE);

                	
                    break;
                case BluetoothChatService.STATE_CONNECTING:
//                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    //mTitle.setText(R.string.title_not_connected);                	
                    break;
                }
                break;
            case MESSAGE_WRITE:
                //byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                //String writeMessage = new String(writeBuf);
                //mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
				Toast.makeText(getApplicationContext(), "사람이 있어요", Toast.LENGTH_LONG).show();
                //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);

				// 진동
				Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(1500);//1.5초간 진동

				int year=0, month=0, day=0, hour=0, minute=0;
				GregorianCalendar calendar = new GregorianCalendar();
				year = calendar.get(Calendar.YEAR);
				month = calendar.get(Calendar.MONTH);
				day = calendar.get(Calendar.DAY_OF_MONTH);
				hour = calendar.get(Calendar.HOUR_OF_DAY);
				minute = calendar.get(Calendar.MINUTE);

				MakeCache(mInPhoto, "selfshot"+year +month+day+hour+minute);



                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };



	static public void MakeCache(View v,String filename){

		String StoragePath =
				Environment.getExternalStorageDirectory().getAbsolutePath();
		String savePath = StoragePath + "/selfshot";
		File f = new File(savePath);
		if (!f.isDirectory())f.mkdirs();

		v.buildDrawingCache();
		Bitmap bitmap = v.getDrawingCache();
		FileOutputStream fos;

		try{
			fos = new FileOutputStream(savePath+"/"+filename+".jpg");
			bitmap.compress(Bitmap.CompressFormat.JPEG,80,fos);

		}catch (Exception e){
			e.printStackTrace();
		}
	}


    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity2.EXTRA_DEVICE_ADDRESS);
                
                String address2 = address;
                
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
      
                
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                //setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    

}
