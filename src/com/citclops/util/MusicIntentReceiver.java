package com.citclops.util;

import java.util.EventListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MusicIntentReceiver extends BroadcastReceiver{
	
	public enum HEADESET_STATE {HEADSET_PLUGGED, HEADSET_UNPLUGGED}
	public HEADESET_STATE HeadsetState = HEADESET_STATE.HEADSET_UNPLUGGED;
	private changeStateListener changeState;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
            case 0:
            	HeadsetState = HEADESET_STATE.HEADSET_UNPLUGGED;
                break;
            case 1:
            	HeadsetState = HEADESET_STATE.HEADSET_PLUGGED;
                break;
            default:
            	HeadsetState = HEADESET_STATE.HEADSET_UNPLUGGED;
            }
        	if (changeState != null) changeState.changeState(HeadsetState);
        }		
	}	
	/*
	 * Listener to inform state changed
	 */
	public interface changeStateListener extends EventListener{
		public void changeState (HEADESET_STATE newState);
	}
    /*
     * Sets Final Value Listener
     */
    public void setNewSateListener(changeStateListener eventListener) {
    	changeState = eventListener;
    }
}
