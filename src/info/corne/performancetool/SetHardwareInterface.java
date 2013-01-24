package info.corne.performancetool;

import android.os.AsyncTask;

public interface SetHardwareInterface {
	void notifyOfHardwareInfoSaved(final AsyncTask<String[], Void, Void> task);
}
