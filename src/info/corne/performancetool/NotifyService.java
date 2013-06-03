package info.corne.performancetool;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import info.corne.performancetool.statics.DefaultSettings;
import info.corne.performancetool.statics.PowerSettings;

/**
 * Created by corne on 6/3/13.
 */
public class NotifyService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        int action = intent.getIntExtra("aaa", 2);

        if(action == 0) {
            PowerSettings settings = new PowerSettings();
            String[] files = settings.getFileNames();
            String[] values = settings.getValues();

            SetHardwareInfoTask task = new SetHardwareInfoTask(files, values);
            task.execute();
            MainActivity.notifyPower(getApplicationContext());
            System.out.println("PowerSettings");
        } else if (action == 1) {
            DefaultSettings settings = new DefaultSettings();
            String[] files = settings.getFileNames();
            String[] values = settings.getValues();

            SetHardwareInfoTask task = new SetHardwareInfoTask(files, values);
            task.execute();
            MainActivity.notifySaver(getApplicationContext());
            System.out.println("DefaultSettings");
        } else if (action == 2) {
            System.out.println("I'm out of options here bro!");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
