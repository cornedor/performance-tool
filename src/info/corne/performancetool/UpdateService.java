package info.corne.performancetool;

import info.corne.performancetool.statics.Settings;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class UpdateService extends Service{
	
	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		BroadcastReceiver receiver = new ScreenReceiver();
		registerReceiver(receiver, filter);
	}
	
	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		final UpdateService self = this;
        Log.d("corne", "Flags: " + flags);
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				if(!pm.getBoolean(Settings.AUTO_WIFI, false)) return;
				
				boolean screenOn = intent.getBooleanExtra(Settings.SCREEN_STATE, true);
				int wifiAlreadyOn = pm.getInt(Settings.WIFI_ALREADY_ON, -1);
				WifiManager wifiManager = (WifiManager) self.getSystemService(Context.WIFI_SERVICE);
				
				if(screenOn)
				{
					if(wifiAlreadyOn == 1) wifiManager.setWifiEnabled(true);
				}
				else
				{
					Editor editor = pm.edit();
					if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) wifiAlreadyOn = 1;
					else wifiAlreadyOn = 0;
					editor.putInt(Settings.WIFI_ALREADY_ON, wifiAlreadyOn);
					editor.commit();
					
					wifiManager.setWifiEnabled(false);
				}
				
			}
		}).start();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}
