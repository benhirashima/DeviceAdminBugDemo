package com.benhirashima.deviceadminbugdemo;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class Receiver extends BroadcastReceiver
{
	public static final String TAG = "DeviceAdminBugDemo";
	
	// set this to false to reproduce the bug. set it to true to work around the bug by delaying 
	// device admin operations until a certain amount of time has passed since boot started.
	private static final boolean WORK_AROUND_BUG = true;
	
	// adjust this to give the system enough time to finish booting before you issue device admin commands.
	private static final int MIN_UPTIME = 180000;
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		Log.i(TAG, action + " received");
		
		long uptime = SystemClock.uptimeMillis();
		Log.d(TAG, "Uptime since boot in milliseconds: " + uptime);
		
		DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName adminReceiver = new ComponentName(context, AdminReceiver.class);

		if (!devicePolicyManager.isAdminActive(adminReceiver))
		{
			Log.d(TAG, "App is not a device admin");
			return;
		}
			
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
			if (WORK_AROUND_BUG) 
			{
				// wait until boot has completed. rough guess on time to wait.
				if (uptime > MIN_UPTIME) doSomeDeviceAdminOperations(devicePolicyManager, adminReceiver);
			}
			else
			{
				// this will trigger the bug and make it impossible to uninstall the app!
				doSomeDeviceAdminOperations(devicePolicyManager, adminReceiver);
			}
		}
	}

	private void doSomeDeviceAdminOperations(DevicePolicyManager devicePolicyManager, ComponentName adminReceiver)
	{
		long maxTimeToLock = devicePolicyManager.getMaximumTimeToLock(adminReceiver);
		maxTimeToLock = maxTimeToLock > 0 ? 0 : 1; // toggle between 0 and 1
		
		devicePolicyManager.setMaximumTimeToLock(adminReceiver, maxTimeToLock);
		
		Log.i(TAG, "Max time to lock set to " + maxTimeToLock);
	}
}
