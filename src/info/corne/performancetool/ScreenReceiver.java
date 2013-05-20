package info.corne.performancetool;

import info.corne.performancetool.statics.Settings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean screenOn = false;
		if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
		{
			screenOn = false;
		}
		else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
		{
			screenOn = true;
		}
		Intent i = new Intent(context, UpdateService.class);
		i.putExtra(Settings.SCREEN_STATE, screenOn);
		context.startService(i);
	}
	
}
