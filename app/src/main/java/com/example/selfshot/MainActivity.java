package com.example.selfshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class MainActivity extends Activity {

	FrameLayout m_FrameLayout;
	CameraSurface m_CameraSurface;
	ImageButton BrightPlusButton, BrightMinusButton, SutterButton;
	ImageButton ImgZoomPlusBtn, ImgZoomMinusBtn, Bluetooth_Btn;
	ToggleButton Front_Rear_Btn;
	int curYear, curMonth, curDay, curHour, curMinute, curSecond;
	Calendar c;
	Date curMillis;
	private static final String TAG = "BluetoothChat"; 
    private static final boolean DEBUG = true;

	// BluetoothChatService Handler ?? ???? ??? ??
    public static final int MESSAGE_STATE_CHANGE = 1; 
    public static final int MESSAGE_READ = 2; 
    public static final int MESSAGE_WRITE = 3; 
    public static final int MESSAGE_DEVICE_NAME = 4; 
    public static final int MESSAGE_TOAST = 5;

	// BluetoothChatService Handler ?? ?? ? ??
    public static final String DEVICE_NAME = "device_name"; 
    public static final String TOAST = "toast"; 
  
    // Intent request codes 
    private static final int REQUEST_CONNECT_DEVICE = 1; 
    private static final int REQUEST_ENABLE_BT = 2;
	protected static final int GET_CAMERA = 0;

	// ??? ??? ?? ??
    private String mConnectedDeviceName = null;
	// ???? ??? ?? ( ???? ?? ??? ???? ???? ???? ?? )
    private BluetoothAdapter mBluetoothAdapter = null;
    

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //String dirPath = getFilesDir().getAbsolutePath();
        String name = "Selfshot";
		String path = Environment.getExternalStorageDirectory().getPath();
		File file = new File(path + "/DCIM/" + name );
        
        if(!file.exists()){
        	file.mkdir();
        }

		// ??? ???? ???? ?? ????? ??? ??
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// ?? ??? ?????? ??? ???? ???? ??? ??
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        
        // If the adapter is null, then Bluetooth is not supported 
        if (mBluetoothAdapter == null) { 
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show(); 
            finish(); 
            return; 
        }

		// ??? ???? ???? ?????.
        m_FrameLayout = (FrameLayout) findViewById(R.id.container);
        m_CameraSurface = new CameraSurface(this);
        m_FrameLayout.addView(m_CameraSurface);

		// ??? ???? ????? ?? ????? ??? ?? ???? ??
        Singleton.getInstance().m_Camera = Camera.open();
        
		Singleton.getInstance().m_DisplaySize = Singleton.getInstance().m_Camera.new Size(0, 0);
		
		int nScreenWidth = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		int nScreenHeight = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getHeight();
		
		Singleton.getInstance().m_DisplaySize.width = nScreenWidth;
		Singleton.getInstance().m_DisplaySize.height = nScreenHeight;
		
		m_CameraSurface.cameraChange();


		// ?? ??, ??? ? ??,????, ?? ?? ??, ?? ????? ???? ?? ??
        
        SutterButton = (ImageButton) findViewById(R.id.camerashutterbtn);
        
        
        ImgZoomPlusBtn = (ImageButton) findViewById(R.id.img_zoomplus_btn);
        ImgZoomPlusBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				m_CameraSurface.ZoomPlus();
				
			}
        	
        });
        
        
        
        ImgZoomMinusBtn = (ImageButton) findViewById(R.id.img_zoomminus_btn);
        ImgZoomMinusBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				m_CameraSurface.ZoomMinus();
			}
        	
        });
        
        BrightPlusButton = (ImageButton) findViewById(R.id.brightplus);
        BrightPlusButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				m_CameraSurface.BrightPlus();
			}
		});
        
        BrightMinusButton = (ImageButton) findViewById(R.id.brightminus);
        BrightMinusButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				m_CameraSurface.BrightMinus();
			}
		});
        
        Front_Rear_Btn = (ToggleButton) findViewById(R.id.front_rear_btn);
        Front_Rear_Btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(Front_Rear_Btn.isChecked()){
					
					Front_Rear_Btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.rear_selector));
					
				}else{
					
					Front_Rear_Btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.front_selector));
				}
				
				m_CameraSurface.changeCamera();
				
				m_CameraSurface.cameraChange();
				
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)m_FrameLayout.getLayoutParams();
				params.width = Singleton.getInstance().m_DisplaySize.height * m_CameraSurface.m_OptimalPreviewSize.width / m_CameraSurface.m_OptimalPreviewSize.height;
				m_FrameLayout.setLayoutParams(params);

			}
			
		});
        
        
                
        SutterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				// ?? ??? ???? ?? ( ?? ??? ???? ??? ??? ?, ????? ?? )
				Update();
				
				Singleton.getInstance().m_Camera.autoFocus (new Camera.AutoFocusCallback() {

					public void onAutoFocus(boolean success, Camera camera) {

						if(success){
							ShutterCallback shutter = new ShutterCallback(){

								@Override
								public void onShutter() {
									// TODO Auto-generated method stub
									
								}
								
							};

							Singleton.getInstance().m_Camera.takePicture(shutter, null, new PictureCallback() {
								
								@Override
								public void onPictureTaken(byte[] arg0, Camera arg1) {
									// TODO Auto-generated method stub
									try {
//										int rotation = getWindowManager().getDefaultDisplay().getRotation();
//										int degrees = 0;
//										switch (rotation) {
//										case Surface.ROTATION_0: degrees = 0; break;
//										case Surface.ROTATION_90: degrees = 90; break;
//										case Surface.ROTATION_180: degrees = 180; break;
//										case Surface.ROTATION_270: degrees = 270; break;
//										}
										
										String name = "IMG_"+ curYear + curMonth + curDay + curHour + curMinute + curSecond + ".jpg";
										String path = Environment.getExternalStorageDirectory().getPath();
										File file = new File(path + "/DCIM/Selfshot/" + name );
										file.createNewFile();
										FileOutputStream stream = new FileOutputStream(path + "/DCIM/Selfshot/" + name);
										stream.write(arg0);
										stream.flush();
										stream.close();
										
//										Matrix matrix = new Matrix(); 
//										matrix.postRotate(90); 
										
										Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
										scanIntent.setData(Uri.fromFile(file));
										MainActivity.this.sendBroadcast(scanIntent);
										
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									// ?? ?? ?, ?? ??? ?? ??
									Singleton.getInstance().m_Camera.startPreview();
									
								}
							});

						}
						else {
							ShutterCallback shutter = new ShutterCallback(){

								@Override
								public void onShutter() {
									// TODO Auto-generated method stub
									
								}
								
							};

							Singleton.getInstance().m_Camera.takePicture(shutter, null, new PictureCallback() {
								
								@Override
								public void onPictureTaken(byte[] arg0, Camera arg1) {
									// TODO Auto-generated method stub
									try {
//										int rotation = getWindowManager().getDefaultDisplay().getRotation();
//										int degrees = 0;
//										switch (rotation) {
//										case Surface.ROTATION_0: degrees = 0; break;
//										case Surface.ROTATION_90: degrees = 90; break;
//										case Surface.ROTATION_180: degrees = 180; break;
//										case Surface.ROTATION_270: degrees = 270; break;
//										}
																														
										String name = "IMG_"+ curYear + curMonth + curDay + curHour + curMinute + curSecond + ".jpg";
										String path = Environment.getExternalStorageDirectory().getPath();
										File file = new File(path + "/DCIM/Selfshot/" + name );
										file.createNewFile();
										FileOutputStream stream = new FileOutputStream(path + "/DCIM/Selfshot/" + name);
										stream.write(arg0);
										stream.flush();
										stream.close();
										
//										Matrix matrix = new Matrix(); 
//										matrix.postRotate(90); 
										
										Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
										scanIntent.setData(Uri.fromFile(file));
										MainActivity.this.sendBroadcast(scanIntent);
										
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									// ?? ?? ?, ?? ??? ?? ??
									Singleton.getInstance().m_Camera.startPreview();
									
								}
							});							
						}

					}

				});
				
			}
        	
        });

		// ???? ??? ? ???? ?? ???? ???? ??
        Bluetooth_Btn = (ImageButton) findViewById(R.id.blueconnect);
        Bluetooth_Btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				StartBluetooth();
				
				AlertDialog dialog = blueDialogBox();
				dialog.show();
			}
        	
        });
        
    }


	// ???? ??? ?? ???? ??? ????? ??
    public void StartBluetooth() {
    	if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); 
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT); 
        // Otherwise, setup the chat session 
        } else { 
        	if (Singleton.getInstance().mChatService == null) 
            	Singleton.getInstance().mChatService = new BluetoothPhotoTransferService(this, nHandler); 
        }
    }
    
    private AlertDialog blueDialogBox(){
		//AlertDialog??? ?? editText ??? ???? ?? ??? ????? ???? ??? ?? ????? ??.

		//?? ???? ?? ??? LayoutInflater? ? ???? ?? ??? Context ??? ????..
		final Context ctx = getApplicationContext();

		//Context? ?? LayoutInflater? ??? ????.
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(LAYOUT_INFLATER_SERVICE);

		//TextView,editText? checkBox? ??? ????? ??? ?? ????.
		View layout = inflater.inflate(R.layout.blueselecter, null);

		//??? AlertDialog? ???.
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		//?? ?????. ??? ?? ?? ??? ??? ? ? ?? ?? ??, ?? ??? ??????.
		builder.setView(layout);
		
		builder.setTitle("기능을 선택해주세요.");
		
		ImageButton cam = (ImageButton) findViewById(R.id.se_ca);
		ImageButton remo = (ImageButton) findViewById(R.id.se_re);
		
		builder.setPositiveButton("camera", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				ensureDiscoverable();
				
				Singleton.getInstance().mChatService.start();
			}			
		});
				
		builder.setNegativeButton("remote", new DialogInterface.OnClickListener() {
		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub				
				Intent serverIntent = new Intent(ctx, DeviceListActivity.class); 
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}
		});
		
		AlertDialog dialog = builder.create();
		
		return dialog;
		
		
	}

	// ?? ??? ????.
    protected void Update()

     {  
    	
      c = Calendar.getInstance();

      curMillis = c.getTime();

      curYear = c.get(Calendar.YEAR);

      curMonth = c.get(Calendar.MONTH)+1;

      curDay = c.get(Calendar.DAY_OF_MONTH); 

      curHour = c.get(Calendar.HOUR_OF_DAY);
 
      curMinute = c.get(Calendar.MINUTE);  
 
      curSecond = c.get(Calendar.SECOND);
     
    }
  
    
    @Override 
    public void onStart() { 
        super.onStart(); 
  
        if(DEBUG) Log.e(TAG, "++ ON START ++"); 
        
    }


	// ???? ?? ????? ?
    @Override 
    public synchronized void onResume() { 
        super.onResume(); 
  
        if(DEBUG) Log.e(TAG, "+ ON RESUME +"); 
  
        if (Singleton.getInstance().mChatService != null) { 
          
            if (Singleton.getInstance().mChatService.getState() == BluetoothPhotoTransferService.STATE_NONE) { 
             
            	Singleton.getInstance().mChatService.start(); 
              
            } 
        } 
    }

	// ???? ??? ?? ???? ??
    private void setupChat() {          
  
    	Singleton.getInstance().mChatService = new BluetoothPhotoTransferService(this, nHandler); 
    }
    
    @Override 
    public synchronized void onPause() { 
        super.onPause(); 
  
        if(DEBUG) Log.e(TAG, "- ON PAUSE -"); 
    } 
  
    @Override 
    public void onStop() { 
        super.onStop(); 
  
        if(DEBUG) Log.e(TAG, "-- ON STOP --"); 
    } 
  
    @Override 
    public void onDestroy() { 
        super.onDestroy();

		// ?? ???? ??
        moveTaskToBack(true);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());

		// ???? ??? ???.
        if (Singleton.getInstance().mChatService != null) Singleton.getInstance().mChatService = null; 
  
        if(DEBUG) Log.e(TAG, "--- ON DESTROY ---"); 
    }

	// ?? ???? ? ??? ???? ??? ? ? ??? ???? ??
    private void ensureDiscoverable() { 
        if(DEBUG) Log.d(TAG, "ensure discoverable"); 
  
        if (mBluetoothAdapter.getScanMode() != 
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) { 
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE); 
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300); 
            startActivity(discoverableIntent); 
        } 
    }
    
    private void bt_preview() {
		Intent intent = new Intent(MainActivity.this, BtActivity.class);
		startActivityForResult(intent, GET_CAMERA);
		//startActivity(intent);
		
	}

	// BluetoothService??? ?? ???? ???? ??? ??
    private final Handler nHandler = new Handler() { 
        @Override
        public void handleMessage(Message msg) { 
            switch (msg.what) { 
                case MESSAGE_STATE_CHANGE: 
                    if(DEBUG) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1); 
  
                    switch (msg.arg1) { 
                    	case BluetoothPhotoTransferService.STATE_CONNECTED_REMOTE:
	                    	bt_preview();
	                        break;
	                    case BluetoothPhotoTransferService.STATE_CONNECTED_CAMERA:
	                    	
	                    	break; 
                        case BluetoothPhotoTransferService.STATE_CONNECTING: 
                        
                            break; 
                        case BluetoothPhotoTransferService.STATE_LISTEN: 
                        case BluetoothPhotoTransferService.STATE_NONE: 
                
                            break; 
                    }   
                    break; 
                case MESSAGE_WRITE: 
                    break; 
                case MESSAGE_READ:

					// ???? ??? ?? ???? ??? ???? ??
                	int receive = msg.arg2;
                	
                	if( receive == 1 ){
                		SutterButton.performClick();
                	}
                	else if( receive == 2 ){
                		ImgZoomPlusBtn.performClick();
                	}
                	else if( receive == 3 ){
                		ImgZoomMinusBtn.performClick();
                	}
                	else if( receive == 4 ){
                		BrightPlusButton.performClick();
                	}
                	else if( receive == 5 ){
                		BrightMinusButton.performClick();
                	}
                	else if( receive == 6){
                		m_CameraSurface.SetStateImageCall();
                	}
                	else if( receive == 7){
                		Singleton.getInstance().m_BtActivity.SetView((Byte[])msg.obj);
                	}
                	else if( receive == 8){
                		Singleton.getInstance().mChatService.stop();           
                	}
  
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


	// ?????? ?? ???? ???? ??
    @SuppressLint("NewApi") public void onActivityResult(int requestCode, int resultCode, Intent data) { 
        if(DEBUG) Log.d(TAG, "onActivityResult " + resultCode); 
  
        switch (requestCode) {
	        case GET_CAMERA:
	        	
	        	m_CameraSurface.changeCamera();
	        	
	        	m_CameraSurface.changeCamera();
				
				m_CameraSurface.cameraChange();

	        	break;
        
            case REQUEST_CONNECT_DEVICE: 
                // When DeviceListActivity returns with a device to connect 
                if (resultCode == Activity.RESULT_OK) { 
                    // Get the device MAC address 
                    String address = data.getExtras() 
                                         .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS); 
                    // Get the BLuetoothDevice object 
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address); 
                    // Attempt to connect to the device 
                    Singleton.getInstance().mChatService.connect(device);
                } 
  
                break; 
            case REQUEST_ENABLE_BT: 
                // When the request to enable Bluetooth returns 
                if (resultCode == Activity.RESULT_OK) { 
                    // Bluetooth is now enabled, so set up a chat session 
                    setupChat(); 
                } else { 
                    // User did not enable Bluetooth or an error occured 
                    Log.d(TAG, "BT not enabled"); 
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show(); 
                    finish(); 
  
                } 
                break; 
        } 
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { 
        switch (item.getItemId()) { 
            case R.id.scan: 
                // Launch the DeviceListActivity to see devices and do scan 
                Intent serverIntent = new Intent(this, DeviceListActivity.class); 
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); 
                return true; 
            case R.id.discoverable: 
                // Ensure this device is discoverable by others 
                ensureDiscoverable(); 
                return true; 
        } 
  
        return false; 
    }


}
