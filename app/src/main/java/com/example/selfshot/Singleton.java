package com.example.selfshot;

import android.hardware.Camera;
import android.hardware.Camera.Size;

// ???????? ????? ???? ????? ?????? ??? ??? ???
public final class Singleton {
	public Camera m_Camera = null;
	public BluetoothPhotoTransferService mChatService = null;
	public BtActivity m_BtActivity = null;
	public Size m_DisplaySize = null;
	
	private static Singleton instance = null;
	
	public static Singleton getInstance() 
	{ 
    	synchronized (Singleton.class) 
    	{
			if(instance == null)
			instance = new Singleton();
		}
    	
    	return instance; 
    }
		

}

