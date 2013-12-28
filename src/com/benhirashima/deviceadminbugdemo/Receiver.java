package com.benhirashima.deviceadminbugdemo;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver
{
	public static final String TAG = "DeviceAdminBugDemo";
	
	// set this to false to reproduce the bug. set it to true to work around the bug by  
	// checking whether boot has completed before starting device admin operations.
	private static final boolean WORK_AROUND_BUG = true;
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		Log.i(TAG, action + " received");
		
		
		DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName adminReceiver = new ComponentName(context, AdminReceiver.class);

		if (!devicePolicyManager.isAdminActive(adminReceiver))
		{
			Log.d(TAG, "App is not a device admin");
			return;
		}
			
		if (Intent.ACTION_BOOT_COMPLETED.equals(action))
		{
			setBootCompleteAlarm(context);
			
			Log.d(TAG, "Active device administrators:");
			for (ComponentName compName : devicePolicyManager.getActiveAdmins())
			{
				Log.d(TAG, compName.toString());
			}
		}
		else // catches all other broadcasts
		{
			if (WORK_AROUND_BUG) 
			{
				if (isBootCompleteAlarmSet(context)) doSomeDeviceAdminOperations(devicePolicyManager, adminReceiver);
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
	
	// set a dummy alarm that gets cleared on boot.
	private void setBootCompleteAlarm(Context context)
	{
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1000); // will never fire, sorta
		
		final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME, cal.getTimeInMillis(), createDummyPendingIntent(context));
	}
	
	// a hacky way of telling whether boot has completed. the alarm gets cleared on boot.
	private boolean isBootCompleteAlarmSet(Context context)
	{
		return PendingIntent.getBroadcast(context, 0, createDummyIntent(context), PendingIntent.FLAG_NO_CREATE) != null;
	}
	
	private Intent createDummyIntent(Context context)
	{
		return new Intent(context, Receiver.class);
	}
	
	private PendingIntent createDummyPendingIntent(Context context)
	{
		return PendingIntent.getBroadcast(context, 0, createDummyIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
