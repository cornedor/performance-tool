package info.corne.performancetool;

/**
 * Receiver for when the screen turns on or off
 * 
 * Copyright (C) 2013  Corné Dorrestijn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * @author Corné Dorrestijn
 *
 */

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
