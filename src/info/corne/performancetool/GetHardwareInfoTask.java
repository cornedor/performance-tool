package info.corne.performancetool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import android.os.AsyncTask;
/**
 * This class will read files etc. to get all
 * all the info needed. This will be done in threads.
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
public class GetHardwareInfoTask extends AsyncTask<String, Void, Void>{

	private MainActivity main;
	private String[] result;
    private HardwareInfoPostRunnable postHook;
	public GetHardwareInfoTask(MainActivity main)
	{
		this.main = main;
	}

    public GetHardwareInfoTask(HardwareInfoPostRunnable hook)
    {
        this.postHook = hook;
    }

	@Override
	protected Void doInBackground(String... params) {
		result = new String[params.length];
		// Loop trough all the params.
		for(int i = 0; i < params.length; i++)
		{
			// Open the file.
			File file = new File(params[i]);
			StringBuilder content = new StringBuilder();
			String line = "";
			if(file.exists())
			{
				try 
				{
					// Read it.
					BufferedReader br = new BufferedReader(new FileReader(file));
					while((line = br.readLine()) != null)
					{
						content.append(line);
						content.append("\n");
					}
					content.deleteCharAt(content.length()-1);
				} catch (IOException e) {
					e.printStackTrace();
					content.append("Error");
				}
			}
			else
			{
				content.append("Error");
			}
			// Remove the last new line character.
			// And write the result in result.
			result[i] = content.toString();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res)
	{
        if (postHook!=null){
            postHook.setResult(result);
            postHook.run();
        } else {
		    main.hardwareInfoLoaded(result);
        }
	}

	

}
