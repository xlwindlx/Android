package com.example.selfshot; 
  
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


// 블루투스의 연결, 연결 후 송수신을 관리하는 부분
public class BluetoothPhotoTransferService { 
    private static final String TAG = "BluetoothChatService"; 
    private static final boolean DEBUG = true;   
    
    private static final String NAME = "BluetoothChat";   
     
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    // 멤버 변수들
    private final BluetoothAdapter mAdapter; 
    private final Handler nHandler;
    //private final Handler bHandler;
    private AcceptThread mAcceptThread; 
    private ConnectThread mConnectThread; 
    private ConnectedThread mConnectedThread; 
    private int mState; 
    private String mDeviceName;

    // 블루투스 상태를 나타내는 상수들
    public static final int STATE_NONE = 0;        
    public static final int STATE_LISTEN = 1;     
    public static final int STATE_CONNECTING = 2;  
    public static final int STATE_CONNECTED_CAMERA = 3;  
    public static final int STATE_CONNECTED_REMOTE = 4;
    
    private final int MODE_CAMERA = 1001;
    private final int MODE_REMOTE = 1002;
    
    private boolean m_bContinue = false;
    
    private List<Byte> m_listBuffer;
    
    private Context m_Context = null;
  
    @SuppressLint("NewApi")
	public BluetoothPhotoTransferService(Context context, Handler handler) { 
        mAdapter = BluetoothAdapter.getDefaultAdapter(); 
        mState = STATE_NONE; 
        nHandler = handler; 
        m_Context = context;
    }

    // 블루투스 상태를 변경해주는 부분
    private synchronized void setState(int state) { 
        if (DEBUG) Log.d(TAG, "setState() " + mState + " -> " + state); 
  
        mState = state; 
   
        nHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget(); 
    }

    // 현재 상태를 리턴해주는 부분
    public synchronized int getState() { 
        return mState; 
    }

    // 블루투스서비스를 시작하는 부분
    public synchronized void start() { 
        if (DEBUG) Log.d(TAG, "start"); 
  
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;} 
  
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;} 
  
        if (mAcceptThread == null) { 
            mAcceptThread = new AcceptThread(); 
            mAcceptThread.start(); 
        } 
  
        setState(STATE_LISTEN); 
    }

    // 블루투스 연결하는 부분
    public synchronized void connect(BluetoothDevice device) { 
        if (DEBUG) Log.d(TAG, "connect to: " + device); 
  
        if (mState == STATE_CONNECTING) { 
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;} 
        } 
  
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;} 
  
        mConnectThread = new ConnectThread(device); 
        mConnectThread.start(); 
        setState(STATE_CONNECTING); 
    }

    // 블루투스 연결 완료하는 부분
    @SuppressLint("NewApi")
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, int mode) { 
        if (DEBUG) Log.d(TAG, "connected"); 
  
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;} 
  
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;} 
  
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;} 
   
        mConnectedThread = new ConnectedThread(socket); 
        mConnectedThread.start(); 
  
        // ����� ����� �̸��� �����ִ� �κ� 
        Message msg = nHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME); 
        Bundle bundle = new Bundle(); 
        bundle.putString(MainActivity.DEVICE_NAME, device.getName()); 
        msg.setData(bundle); 
        nHandler.sendMessage(msg); 
  
        if (mode == MODE_CAMERA)
        	setState(STATE_CONNECTED_CAMERA);
        else if(mode == MODE_REMOTE)
        	setState(STATE_CONNECTED_REMOTE);
  
        mDeviceName = device.getName(); 
    }

    // 블루투스 서비스를 중지할 때
    public synchronized void stop() { 
        if (DEBUG) Log.d(TAG, "stop"); 
  
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;} 
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;} 
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;} 
  
        setState(STATE_NONE); 
    }

    // 블루투스 연결 후 데이터 송신하는 부분
    public void write(byte[] out) { 
     
        ConnectedThread r; 
   
        synchronized (this) { 
            if (mState != STATE_CONNECTED_CAMERA && mState != STATE_CONNECTED_REMOTE) return;
            r = mConnectedThread; 
        } 
        
        r.write(out);
    }



    // 블루투스 연결을 실패하였을 때 처리하는 부분
    private void connectionFailed() { 
        setState(STATE_LISTEN); 
  
        Message msg = nHandler.obtainMessage(MainActivity.MESSAGE_TOAST); 
        Bundle bundle = new Bundle(); 
        bundle.putString(MainActivity.TOAST, "Unable to connect device"); 
        msg.setData(bundle); 
        nHandler.sendMessage(msg);
        
        Singleton.getInstance().mChatService.start();
    }

    // 블루투스 연결이 끊겼을 때 처리하는 부분
    private void connectionLost() { 
        setState(STATE_LISTEN); 
        
        // ��Ƽ��Ƽ�� ������� ���� ���� �޽����� ������.
        Message msg = nHandler.obtainMessage(MainActivity.MESSAGE_TOAST); 
        Bundle bundle = new Bundle(); 
        bundle.putString(MainActivity.TOAST, "Device connection was lost"); 
        msg.setData(bundle); 
        nHandler.sendMessage(msg); 
        
        Singleton.getInstance().mChatService.start();
    }

    // 블루투스 연결을 허용하는 쓰레드
    private class AcceptThread extends Thread { 
         
        private final BluetoothServerSocket mmServerSocket; 
  
        @SuppressLint("NewApi")
		public AcceptThread() { 
            BluetoothServerSocket tmp = null; 
  
            
            try { 
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID); 
            } catch (IOException e) { 
                Log.e(TAG, "listen() failed", e); 
            } 
            mmServerSocket = tmp; 
        } 
  
        public void run() { 
            if (DEBUG) Log.d(TAG, "BEGIN mAcceptThread" + this); 
  
            setName("AcceptThread"); 
            BluetoothSocket socket = null; 
              
            while (mState != STATE_CONNECTED_CAMERA && mState != STATE_CONNECTED_REMOTE) { 
                try { 
                    
                    socket = mmServerSocket.accept();
                    
                } catch (IOException e) { 
                    Log.e(TAG, "accept() failed", e); 
                    break; 
                } 
   
                if (socket != null) { 
                    synchronized (BluetoothPhotoTransferService.this) { 
                        switch (mState) { 
                            case STATE_LISTEN: 
                            case STATE_CONNECTING: 
                                 
                                connected(socket, socket.getRemoteDevice(), MODE_CAMERA); 
  
                                break; 
                            case STATE_NONE: 
                            case STATE_CONNECTED_CAMERA:
                            case STATE_CONNECTED_REMOTE:
                                 
                                try { 
                                    socket.close(); 
                                } catch (IOException e) { 
                                    Log.e(TAG, "Could not close unwanted socket", e); 
                                } 
  
                                break; 
                        } 
                    } 
                } 
            } 
  
            if (DEBUG) Log.i(TAG, "END mAcceptThread"); 
        } 
  
        public void cancel() { 
            if (DEBUG) Log.d(TAG, "cancel " + this); 
            try { 
                mmServerSocket.close(); 
            } catch (IOException e) { 
                Log.e(TAG, "close() of server failed", e); 
            } 
        } 
    }


    // 블루투스 연결을 하는 쓰레드
    private class ConnectThread extends Thread { 
        private final BluetoothSocket mmSocket; 
        private final BluetoothDevice mmDevice; 
  
        public ConnectThread(BluetoothDevice device) { 
            mmDevice = device; 
            BluetoothSocket tmp = null; 
  
            try { 
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID); 
            } catch (IOException e) { 
                Log.e(TAG, "create() failed", e); 
            } 
            mmSocket = tmp; 
        } 
  
        public void run() { 
            Log.i(TAG, "BEGIN mConnectThread"); 
  
            setName("ConnectThread"); 
   
            mAdapter.cancelDiscovery(); 
  
            try {                  
                mmSocket.connect(); 
            } catch (IOException e) { 
                connectionFailed(); 
                
                try { 
                    mmSocket.close();
                } catch (IOException e2) { 
                    Log.e(TAG, "unable to close() socket during connection failure", e2); 
                } 
             
                BluetoothPhotoTransferService.this.start(); 
                return; 
            } 
  
            synchronized (BluetoothPhotoTransferService.this) { 
                mConnectThread = null; 
            } 
   
            connected(mmSocket, mmDevice, MODE_REMOTE); 
        } 
  
        public void cancel() { 
            try { 
                mmSocket.close(); 
            } catch (IOException e) { 
                Log.e(TAG, "close() of connect socket failed", e); 
            } 
        } 
    }

    // 연결 후 데이터 송수신을 관리하는 쓰레드
    private class ConnectedThread extends Thread { 
        private final BluetoothSocket mmSocket; 
        private final InputStream mmInStream; 
        private final OutputStream mmOutStream; 
  
        @SuppressLint("NewApi")
		public ConnectedThread(BluetoothSocket socket) { 
            Log.d(TAG, "create ConnectedThread"); 
            mmSocket = socket; 
            InputStream tmpIn = null; 
            OutputStream tmpOut = null; 
  
            try { 
                tmpIn = socket.getInputStream(); 
                tmpOut = socket.getOutputStream(); 
            } catch (IOException e) { 
                Log.e(TAG, "temp sockets not created", e); 
            } 
  
            mmInStream = tmpIn; 
            mmOutStream = tmpOut; 
        } 
  
  
        public void run() { 
            Log.i(TAG, "BEGIN mConnectedThread"); 
  
            try { 
                
                byte[] buffer = new byte[1024]; 
                int bytes = 0;                
                int result = 0;
                m_listBuffer = new ArrayList<Byte>();
                byte[] message = null;

                // 데이터를 수신하는 부분 ( 조작명령어와 데이터 수신의 프로토콜이 있는 부분 )
                while (true) { 
                    try {                                    
                        
                        bytes = mmInStream.read(buffer);                       
                        
                        if(m_bContinue == false){
                        
	                        int bytefirst = buffer[0] & 0xFF ;
	        				int bytesecond = buffer[1] & 0xFF;
	        				int bytethird = buffer[2] & 0xFF;	        					        				
	        				
	        				result = bytefirst<<16;
	        				result += bytesecond<<8;   
	        				result += bytethird;
	        				
	        				message = new byte[bytes - 3];
                    		System.arraycopy(buffer, 3, message, 0, message.length);
                    		
                        	for(int i = 0; i < message.length; i++)
                        		m_listBuffer.add(message[i]);
                        	
                        }
                          
                        
	        			if(result == m_listBuffer.size()|| result == m_listBuffer.size() + bytes){
	        				
	        				message = new byte[bytes];
                    		System.arraycopy(buffer, 0, message, 0, message.length);
                    		
                        	for(int i = 0; i < message.length; i++)
                        		m_listBuffer.add(message[i]);
	        				
	        				int command = m_listBuffer.get(0);
	        				m_listBuffer.remove(0);
	        				Byte[] arrBuffer = m_listBuffer.toArray(new Byte[m_listBuffer.size()]);
	        				m_listBuffer.clear();
	        				
	        				buffer = new byte[1024];
	        				
	        				m_bContinue = false;
                    		nHandler.obtainMessage(MainActivity.MESSAGE_READ, -1, command, arrBuffer).sendToTarget();
                    		
                        }
                        else {                        	   	                        	
                        	                        	
                        	if( m_bContinue == true ) {
                        		message = new byte[bytes];
                        		System.arraycopy(buffer, 0, message, 0, message.length);
                        		
                            	for(int i = 0; i < message.length; i++)
                            		m_listBuffer.add(message[i]);
                        	}
                        	
                        	m_bContinue = true;                        	                        	
                        	
                        }
	        			
//	        			if (Singleton.getInstance().m_BtActivity != null)
//	        			{
//		        			String log = "[" + m_listBuffer.size() + "] / [" + result + "]";
//		        			Singleton.getInstance().m_BtActivity.logToast(log);
//	        			}
                    	
                    } catch (IOException e) { 
                        Log.e(TAG, "disconnected", e); 
                        connectionLost(); 
                        break; 
                    } 
                } 
   
            } catch (Exception e) { 
                e.printStackTrace(); 
            } 
        }


        // 데이터 송신하는 부분
        public void write(byte[] buffer) { 
            try { 
                mmOutStream.write(buffer);
  
            } catch (IOException e) { 
                Log.e(TAG, "Exception during write", e); 
            } 
        }
        
  
        public void cancel() { 
            try { 
                mmSocket.close(); 
            } catch (IOException e) { 
                Log.e(TAG, "close() of connect socket failed", e); 
            } 
        } 
    }
}