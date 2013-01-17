package info.corne.performancetool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.app.ProgressDialog;
import android.os.AsyncTask;
/**
 * This class will read files etc. to get all
 * all the info needed. This will be done in threads.
 * 
 * Copyright (C) 2013  Corné Dorrestijn
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * @author Corné Dorrestijn
 *
 */
public class GetHardwareInfoTask extends AsyncTask<String, Void, Void>{

	private MainActivity main;
	private String[] result;
	int fragment;
	public GetHardwareInfoTask(MainActivity main, int fragment)
	{
		this.main = main;
		this.fragment = fragment;
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
				content.append("Error\n");
			}
			content.deleteCharAt(content.length()-1);
			result[i] = content.toString();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res)
	{
		main.hardwareInfoLoaded(result, fragment);
	}

	

}
