package info.corne.performancetool;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;
/**
 * This service will change settings on boot
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
public class BootService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		new BootWorker().execute();
		return flags;

	}
	
	class BootWorker extends AsyncTask<Void, Void, Void> 
	{

		@Override
		protected Void doInBackground(Void... arg0) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			if(sharedPreferences.getBoolean(MainActivity.SET_ON_BOOT_SETTING, false))
			{
				String selectedFrequencyCap = sharedPreferences.getString(MainActivity.SELECTED_FREQ_SETTING, "0");
				String selectedGovernor = sharedPreferences.getString(MainActivity.SELECTED_GOV_SETTING, "Undefined");
				String selectedScheduler = sharedPreferences.getString(MainActivity.SELECTED_SCHEDULER_SETTING, "Undefined");
				int ocEnabled = sharedPreferences.getInt(MainActivity.OC_ENABLED, 0);
				
				String[] frequencyCommand = {"su", "-c", "echo " + selectedFrequencyCap + " > /sys/module/cpu_tegra/parameters/cpu_user_cap"};
				String[] schedulerCommand = {"su", "-c", "echo " + selectedScheduler + " > /sys/block/mmcblk0/queue/scheduler" };
				String[] governorCommand = {"su", "-c", "echo " + selectedGovernor + " > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"};
				String[] ocCommand = {"su", "-c", "echo " + ocEnabled + " > /sys/module/cpu_tegra/parameters/enable_oc"};
				
				ShellCommand.run(frequencyCommand);
				ShellCommand.run(schedulerCommand);
				ShellCommand.run(ocCommand);
				ShellCommand.run(governorCommand);
			}
			return null;
		}
		
	}

}
