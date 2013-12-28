package com.benhirashima.deviceadminbugdemo;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity
{
	private static final int ACTIVITY_RESULT_DEVICE_ADMIN = 808;
	
	Button button;
	ComponentName adminReceiver;
	DevicePolicyManager devicePolicyManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		adminReceiver = new ComponentName(this, AdminReceiver.class);
		devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		
		button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				toggleDeviceAdmin();
			}
		});
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if (devicePolicyManager.isAdminActive(adminReceiver))
			button.setText("Deactivate Device Admin");
		else
			button.setText("Activate Device Admin");
	}
	
	private void toggleDeviceAdmin()
	{
		if (devicePolicyManager.isAdminActive(adminReceiver))
		{
			int count = 0;
			for (ComponentName compName : devicePolicyManager.getActiveAdmins())
			{
				if (compName.equals(adminReceiver))
				{
					devicePolicyManager.removeActiveAdmin(compName);
					count++;
				}
			}
			Toast.makeText(getApplicationContext(), count + " device admin components deactivated", Toast.LENGTH_LONG).show();
			
			button.setText("Activate Device Admin");
		}
		else
		{
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
			startActivityForResult(intent, ACTIVITY_RESULT_DEVICE_ADMIN);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode)
		{
			case ACTIVITY_RESULT_DEVICE_ADMIN:
				if (resultCode == Activity.RESULT_OK)
				{
					button.setText("Deactivate Device Admin");
					Toast.makeText(getApplicationContext(), "Device admin activated", Toast.LENGTH_LONG).show();
				}
		}
	}
}
