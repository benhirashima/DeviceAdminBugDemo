package com.benhirashima.deviceadminbugdemo;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver
{
	public static final String TAG = "DeviceAdminBugDemo";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		Log.i(TAG, action + " received");
		
		DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);

		if (Intent.ACTION_BOOT_COMPLETED.equals(action))
		{
			
			Log.d(TAG, "Active device administrators:");
			for (ComponentName compName : devicePolicyManager.getActiveAdmins())
			{
				Log.d(TAG, compName.toString());
			}
		}
		else
		{
			ComponentName adminReceiver = new ComponentName(context, AdminReceiver.class);
			
			if (devicePolicyManager.isAdminActive(adminReceiver))
			{
				long maxTimeToLock = devicePolicyManager.getMaximumTimeToLock(adminReceiver);
				maxTimeToLock = maxTimeToLock > 0 ? 0 : 1; // toggle between 0 and 1
				devicePolicyManager.setMaximumTimeToLock(adminReceiver, maxTimeToLock);
				Log.i(TAG, "Max time to lock set to " + maxTimeToLock);
			}
		}
	}
}
