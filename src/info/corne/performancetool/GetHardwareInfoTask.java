package info.corne.performancetool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class GetHardwareInfoTask extends AsyncTask<String, Void, Void>{

	private MainActivity main;
	private ProgressDialog dialog;
	private String[] result;
	public GetHardwareInfoTask(MainActivity main)
	{
		this.main = main;
		dialog = ProgressDialog.show(main, "Please wait...", "Gathering some info.");
	}
	
	@Override
	protected Void doInBackground(String... params) {
		result = new String[params.length];
		for(int i = 0; i < params.length; i++)
		{
			File file = new File(params[i]);
			StringBuilder content = new StringBuilder();
			String line = "";
			try 
			{
				BufferedReader br = new BufferedReader(new FileReader(file));
				while((line = br.readLine()) != null)
				{
					content.append(line);
					content.append("\n");
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			content.deleteCharAt(content.length()-1);
			result[i] = content.toString();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res)
	{
		main.hardwareInfoLoaded(this.result);
		dialog.dismiss();
	}

	

}
