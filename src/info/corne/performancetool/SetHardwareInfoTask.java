package info.corne.performancetool;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class SetHardwareInfoTask extends AsyncTask<String[], Void, Void>{

	ProgressDialog dialog;
	boolean allCpus;
	public SetHardwareInfoTask(MainActivity main, boolean allCpus)
	{
		dialog = ProgressDialog.show(main, "Please wait...", "Settings are being saved.");
		this.allCpus = allCpus;
	}
	@Override
	protected Void doInBackground(String[]... params) {
		for(int i = 0; i < params.length; i++)
		{
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
						if(result.length() < 3) done = 4;
						else
						{
							String[] enableCpus = {"su", "-c", "echo 4 > /sys/kernel/debug/tegra_hotplug/min_cpus"};
							ShellCommand.run(enableCpus);
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
		dialog.dismiss();
	}

}
