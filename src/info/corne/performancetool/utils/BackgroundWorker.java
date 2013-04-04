package info.corne.performancetool.utils;

import info.corne.performancetool.ShellCommand;
import info.corne.performancetool.statics.DefaultSettings;
import info.corne.performancetool.statics.FileNames;
import info.corne.performancetool.statics.Settings;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class BackgroundWorker extends AsyncTask<Context, Void, Void> 
{
	@Override
	protected Void doInBackground(Context... arg0) {
		Context context = arg0[0];
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		if(sharedPreferences.getBoolean(Settings.SET_ON_BOOT_SETTING, false))
		{
			String selectedFrequencyCap = sharedPreferences.getString(Settings.SELECTED_FREQ_SETTING, "0");
			String selectedGovernor = sharedPreferences.getString(Settings.SELECTED_GOV_SETTING, "Undefined");
			String selectedScheduler = sharedPreferences.getString(Settings.SELECTED_SCHEDULER_SETTING, "Undefined");
			int ocEnabled = sharedPreferences.getInt(Settings.OC_ENABLED, 0);
			String maxCpus = sharedPreferences.getString(Settings.MAX_CPUS, "4");
			String suspendFreq = sharedPreferences.getString(Settings.SUSPEND_FREQ, DefaultSettings.SUSPEND_FREQ);
			String audioFreq = sharedPreferences.getString(Settings.AUDIO_MIN_FREQ, DefaultSettings.AUDIO_MIN_FREQ);
			
			String[] frequencyCommand = {"su", "-c", "echo " + selectedFrequencyCap + " > " + FileNames.CPU_USER_CAP};
			String[] schedulerCommand = {"su", "-c", "echo " + selectedScheduler + " > " + FileNames.IO_SCHEDULERS };
			String[] governorCommand = {"su", "-c", "echo " + selectedGovernor + " > " + FileNames.SCALING_GOVERNOR};
			String[] ocCommand = {"su", "-c", "echo " + ocEnabled + " > " + FileNames.ENABLE_OC};
			String[] maxCpusCommand1 = {"su", "-c", "echo " + maxCpus + " > " + FileNames.MAX_CPUS_MPDEC};
			String[] maxCpusCommand2 = {"su", "-c", "echo " + maxCpus + " > " + FileNames.MAX_CPUS_QUIET};				
			String[] suspendFreqCommand = {"su", "-c", "echo " + suspendFreq + " > " + FileNames.SUSPEND_FREQ};
			String[] audioFreqCommand = {"su", "-c", "echo " + audioFreq + " > " + FileNames.AUDIO_MIN_FREQ};
			
			File f;
			
			ShellCommand.run(frequencyCommand);
			ShellCommand.run(schedulerCommand);
			ShellCommand.run(ocCommand);
			ShellCommand.run(governorCommand);
			f=new File(FileNames.SUSPEND_FREQ);
			if(f.exists())
				ShellCommand.run(suspendFreqCommand);
			
			f=new File(FileNames.AUDIO_MIN_FREQ);
			if(f.exists())
				ShellCommand.run(audioFreqCommand);
			
			f=new File(FileNames.MAX_CPUS_MPDEC);
			if(f.exists())
				ShellCommand.run(maxCpusCommand1);
				
			f=new File(FileNames.MAX_CPUS_QUIET);
			if(f.exists())
				ShellCommand.run(maxCpusCommand2);

		}
		return null;
	}
	
}