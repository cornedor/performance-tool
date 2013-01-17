package info.corne.performancetool;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

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
				String selectedSuspendedCap = sharedPreferences.getString(MainActivity.SELECTED_SUSPENDED_FREQ_SETTINGS, "0");
				String selectedGovernor = sharedPreferences.getString(MainActivity.SELECTED_GOV_SETTING, "Undefined");
				String selectedScheduler = sharedPreferences.getString(MainActivity.SELECTED_SCHEDULER_SETTING, "Undefined");
				int ocEnabled = sharedPreferences.getInt(MainActivity.OC_ENABLED, 0);
				
				String[] frequencyCommand = {"su", "-c", "echo " + selectedFrequencyCap + " > /sys/module/cpu_tegra/parameters/cpu_user_cap"};
				String[] suspendedCommand = {"su", "-c", "echo " + selectedSuspendedCap + " > /sys/module/cpu_tegra/parameters/cpu_user_cap"};
				String[] schedulerCommand = {"su", "-c", "echo " + selectedScheduler + " > /sys/block/mmcblk0/queue/scheduler" };
				String[] governorCommand = {"su", "-c", "echo " + selectedGovernor + " > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"};
				String[] ocCommand = {"su", "-c", "echo " + ocEnabled + " > /sys/module/cpu_tegra/parameters/enable_oc"};
				
				ShellCommand.run(frequencyCommand);
				ShellCommand.run(suspendedCommand);
				ShellCommand.run(schedulerCommand);
				ShellCommand.run(ocCommand);
				ShellCommand.run(governorCommand);
			}
			return null;
		}
		
	}

}
