package info.corne.performancetool;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.io.File;

import info.corne.performancetool.statics.FileNames;
import info.corne.performancetool.statics.Settings;

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
	    private String parseActiveCpusView(String activeCpusString) {
	        if (activeCpusString.length()==0){
	            return null;
	        }
	    
	        String[] parts = activeCpusString.split(" ");
	        if (parts.length != 3){
	            return null;
	        }
        
            return Integer.parseInt(parts[0])+" " +
                Integer.parseInt(parts[1])+" " +
                Integer.parseInt(parts[2]);
        }
        
		@Override
		protected Void doInBackground(Void... arg0) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			if(sharedPreferences.getBoolean(Settings.SET_ON_BOOT_SETTING, false))
			{
				String selectedFrequencyCap = sharedPreferences.getString(Settings.SELECTED_FREQ_SETTING, "0");
				String selectedGovernor = sharedPreferences.getString(Settings.SELECTED_GOV_SETTING, "smartmax");
				String selectedScheduler = sharedPreferences.getString(Settings.SELECTED_SCHEDULER_SETTING, "bfq");
				String oc = sharedPreferences.getString(Settings.OC_ENABLED, "0");
				String maxCpus = sharedPreferences.getString(Settings.MAX_CPUS, "4");
				String suspendFreq = sharedPreferences.getString(Settings.SUSPEND_FREQ, "475000");
				String audioFreq = sharedPreferences.getString(Settings.AUDIO_MIN_FREQ, "102000");
				String selectedCPQGovernor = sharedPreferences.getString(Settings.SELECTED_CPQGOV_SETTING, "rq_stats");				
				String lpOc = sharedPreferences.getString(Settings.LP_OC_ENABLED, "0");
				String gpuScaling = sharedPreferences.getString(Settings.GPU_SCALING, "1");
				String cpuHotplugging = sharedPreferences.getString(Settings.CPU_HOTPLUGGING, "0");
				String activeCpus = parseActiveCpusView(sharedPreferences.getString(Settings.ACTIVE_CPUS, ""));
                String gpuQuickOC = sharedPreferences.getString(Settings.GPU_QUICK_OC, "0");
                String a2dpFreq = sharedPreferences.getString(Settings.A2DP_MIN_FREQ, "204000");

				String[] frequencyCommand = {"su", "-c", "echo " + selectedFrequencyCap + " > " + FileNames.CPU_USER_CAP};
				String[] schedulerCommand = {"su", "-c", "echo " + selectedScheduler + " > " + FileNames.IO_SCHEDULERS };
				String[] governorCommand = {"su", "-c", "echo " + selectedGovernor + " > " + FileNames.SCALING_GOVERNOR};
				String[] ocCommand = {"su", "-c", "echo " + oc + " > " + FileNames.ENABLE_OC};
				String[] maxCpusCommand1 = {"su", "-c", "echo " + maxCpus + " > " + FileNames.MAX_CPUS_MPDEC};
				String[] maxCpusCommand2 = {"su", "-c", "echo " + maxCpus + " > " + FileNames.MAX_CPUS_QUIET};				
				String[] suspendFreqCommand = {"su", "-c", "echo " + suspendFreq + " > " + FileNames.SUSPEND_FREQ};
				String[] audioFreqCommand = {"su", "-c", "echo " + audioFreq + " > " + FileNames.AUDIO_MIN_FREQ};
				String[] cpqSchedulerCommand = {"su", "-c", "echo " + selectedCPQGovernor + " > " + FileNames.CPUQUIET_GOVERNOR };				
				String[] lpOcCommand = {"su", "-c", "echo " + lpOc + " > " + FileNames.ENABLE_LP_OC};
				String[] gpuScalingCommand = {"su", "-c", "echo " + gpuScaling + " > " + FileNames.GPU_SCALING};
				String[] cpuHotpluggingCommand = {"su", "-c", "echo " + cpuHotplugging + " > " + FileNames.MANUAL_HOTPLUG};
				String[] activeCpusCommand = {"su", "-c", "echo " + activeCpus + " > " + FileNames.ACTIVE_CPUS};
                String[] gpuQuickOCCommand = {"su", "-c", "echo " + gpuQuickOC + " > " + FileNames.GPU_QUICK_OC};
                String[] a2dpFreqCommand = {"su", "-c", "echo " + a2dpFreq + " > " + FileNames.A2DP_MIN_FREQ};

                File f;
				
				ShellCommand.run(frequencyCommand);
				ShellCommand.run(schedulerCommand);
				ShellCommand.run(ocCommand);
				ShellCommand.run(governorCommand);

                // enable LP OC must be BEFORE suspend freq
				f=new File(FileNames.ENABLE_LP_OC);
				if(f.exists())
					ShellCommand.run(lpOcCommand);

				f=new File(FileNames.SUSPEND_FREQ);
				if(f.exists())
					ShellCommand.run(suspendFreqCommand);
				
				f=new File(FileNames.AUDIO_MIN_FREQ);
				if(f.exists())
					ShellCommand.run(audioFreqCommand);

                // cpq governor must be set BEFORE manual hotplug
				f=new File(FileNames.CPUQUIET_GOVERNOR);
				if(f.exists())
					ShellCommand.run(cpqSchedulerCommand);

				f=new File(FileNames.MAX_CPUS_MPDEC);
		        if(f.exists())
			        ShellCommand.run(maxCpusCommand1);
					
				f=new File(FileNames.MAX_CPUS_QUIET);
				if(f.exists())
					ShellCommand.run(maxCpusCommand2);

				f=new File(FileNames.MANUAL_HOTPLUG);
				if(f.exists())
				    ShellCommand.run(cpuHotpluggingCommand);
				
				f=new File(FileNames.ACTIVE_CPUS);
				if(f.exists())
					ShellCommand.run(activeCpusCommand);
                
				f=new File(FileNames.GPU_SCALING);
				if(f.exists())
					ShellCommand.run(gpuScalingCommand);

                f=new File(FileNames.GPU_QUICK_OC);
                if(f.exists())
                    ShellCommand.run(gpuQuickOCCommand);

                f=new File(FileNames.A2DP_MIN_FREQ);
                if(f.exists())
                    ShellCommand.run(a2dpFreqCommand);

            }
			return null;
		}
		
	}

}
