package info.corne.performancetool;

import java.io.File;

import android.app.ProgressDialog;
import android.os.AsyncTask;
/**
 * Using this class commands to save the settings can be run.
 * 
 * Copyright (C) 2013  Corné Dorrestijn
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * @author Corné Dorrestijn
 *
 */
public class SetHardwareInfoTask extends AsyncTask<String[], Void, Void>
{
	String[] files;
	String[] values;
	ProgressDialog dialog;
	/**
	 * 
	 * @param files files to write settings to.
	 * @param values values that needs to be written to the files
	 */
	public SetHardwareInfoTask(String[] files, String[] values, ProgressDialog dialog)
	{
		this.files = files;
		this.values = values;
		this.dialog = dialog;
	}
	@Override
	protected Void doInBackground(String[]... params) {
		for(int i = 0; i < files.length; i++)
		{
			String[] command = {"su", "-c", "echo \"" + values[i] + "\" > " + files[i]};
			ShellCommand.run(command);
		}
		return null;
	}
	@Override
	protected void onPostExecute(Void res)
	{
		dialog.dismiss();
	}

}
