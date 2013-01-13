package info.corne.performancetool;

import java.io.File;

import android.app.ProgressDialog;
import android.os.AsyncTask;
/**
 * Using this class commands to save the settings can be run.
 * @author Corné Dorrestijn
 *
 */
public class SetHardwareInfoTask extends AsyncTask<String[], Void, Void>{

	ProgressDialog dialog;
	MainActivity main;
	boolean allCpus;
	/**
	 * 
	 * @param main The activity for the progress dialog.
	 * @param allCpus Does the code needs to be run on all the cores
	 */
	public SetHardwareInfoTask(MainActivity main, boolean allCpus)
	{
		dialog = ProgressDialog.show(main, main.getResources().getString(R.string.please_wait), main.getResources().getString(R.string.being_saved));
		this.allCpus = allCpus;
		this.main = main;
	}
	public SetHardwareInfoTask(boolean allCpus)
	{
		this.allCpus = allCpus;
		this.main = null;
		this.dialog = null;
	}
	@Override
	protected Void doInBackground(String[]... params) {
		for(int i = 0; i < params.length; i++)
		{
			// Commando's with allCpus on false can just be run.
			// if it is than we need to check a few things and
			// run it multiple times.
			if(!allCpus)
			{
				String[] command = params[i];
				ShellCommand.run(command);
			}
			else
			{
				for(int j = 0; j < 4; j++)
				{
					String[] command = params[i];
					for(int u = 0; u < command.length; u++) command[u] = command[u].replace("[[CPU]]", "" + j);
					int done = 0;
					while(done < 3)
					{
						done++;
						
						String result = ShellCommand.run(command);
						// if the message is longer than 1 character than there
						// is probably an error thrown and should all the CPU
						// cores be turned on.
						if(result.length() <= 1) done = 4;
						else
						{
							// Check if the new CPU hotplug manager is used in
							// this kernel
							File file = new File("/sys/kernel/tegra_mpdecision/conf/min_cpus");
							if(file.exists())
							{
								String[] enableCpus = {"su", "-c", "echo 4 > /sys/kernel/tegra_mpdecision/conf/min_cpus"};
								ShellCommand.run(enableCpus);
							}
							else
							{
								String[] enableCpus = {"su", "-c", "echo 4 > /sys/kernel/debug/tegra_hotplug/min_cpus"};
								ShellCommand.run(enableCpus);
							}
						}
					}
				}
			}
		}
		return null;
	}
	@Override
	protected void onPostExecute(Void res)
	{
		if(dialog != null) dialog.dismiss();
	}

}
