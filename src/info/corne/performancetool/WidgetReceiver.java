package info.corne.performancetool;

import info.corne.performancetool.statics.AudioSettings;
import info.corne.performancetool.statics.DefaultSettings;
import info.corne.performancetool.statics.FileNames;
import info.corne.performancetool.statics.PowerSettings;
import info.corne.performancetool.statics.Settings;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;


public class WidgetReceiver extends AppWidgetProvider
{
	private static final String ACTION_CLICK = "ACTION_CLICK";
	private static int current = 0;
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		if(intent.getAction().equals(ACTION_CLICK))
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			current =  sharedPreferences.getInt(Settings.CURRENT_WIDGET_PROFILE, 0);
			if(current == 0)
			{
				rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_audio);
				current = 1;
				String[] files = {
					FileNames.CPU_USER_CAP,
					FileNames.ENABLE_OC,
					FileNames.SCALING_GOVERNOR,
					FileNames.IO_SCHEDULERS,
					FileNames.MAX_CPUS_MPDEC,
					FileNames.MAX_CPUS_QUIET,				
					FileNames.SUSPEND_FREQ,
					FileNames.AUDIO_MIN_FREQ
				};
				String[] values = {
					AudioSettings.CPU_USER_CAP,
					AudioSettings.ENABLE_OC,
					AudioSettings.SCALING_GOVERNOR,
					AudioSettings.IO_SCHEDULERS,
					AudioSettings.MAX_CPUS,
					AudioSettings.MAX_CPUS,
					AudioSettings.SUSPEND_FREQ,
					AudioSettings.AUDIO_MIN_FREQ
				};
				SetHardwareInfoTask task = new SetHardwareInfoTask(files, values);
				task.execute();
			}
			else if (current == 1)
			{
				rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_power);
				current = 2;
				String[] files = {
					FileNames.CPU_USER_CAP,
					FileNames.ENABLE_OC,
					FileNames.SCALING_GOVERNOR,
					FileNames.IO_SCHEDULERS,
					FileNames.MAX_CPUS_MPDEC,
					FileNames.MAX_CPUS_QUIET,				
					FileNames.SUSPEND_FREQ,
					FileNames.AUDIO_MIN_FREQ
				};
				String[] values = {
					PowerSettings.CPU_USER_CAP,
					PowerSettings.ENABLE_OC,
					PowerSettings.SCALING_GOVERNOR,
					PowerSettings.IO_SCHEDULERS,
					PowerSettings.MAX_CPUS,
					PowerSettings.MAX_CPUS,
					PowerSettings.SUSPEND_FREQ,
					PowerSettings.AUDIO_MIN_FREQ
				};
				SetHardwareInfoTask task = new SetHardwareInfoTask(files, values);
				task.execute();
			}
			else if (current == 2)
			{
				rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_default);
				current = 0;
				String[] files = {
					FileNames.CPU_USER_CAP,
					FileNames.ENABLE_OC,
					FileNames.SCALING_GOVERNOR,
					FileNames.IO_SCHEDULERS,
					FileNames.MAX_CPUS_MPDEC,
					FileNames.MAX_CPUS_QUIET,
					FileNames.SUSPEND_FREQ,
					FileNames.AUDIO_MIN_FREQ
				};
				String[] values = {
					DefaultSettings.CPU_USER_CAP,
					DefaultSettings.ENABLE_OC,
					DefaultSettings.SCALING_GOVERNOR,
					DefaultSettings.IO_SCHEDULERS,
					DefaultSettings.MAX_CPUS,
					DefaultSettings.MAX_CPUS,
					DefaultSettings.SUSPEND_FREQ,
					DefaultSettings.AUDIO_MIN_FREQ
				};
				SetHardwareInfoTask task = new SetHardwareInfoTask(files, values);
				task.execute();
				Editor editor = sharedPreferences.edit();
				editor.putInt(Settings.CURRENT_WIDGET_PROFILE, current);
				editor.commit();
			}
			ComponentName cn = new ComponentName(context, WidgetReceiver.class);
			(AppWidgetManager.getInstance(context)).updateAppWidget(cn, rv);
		}
		super.onReceive(context, intent);
	}
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		for(int i = 0; i < appWidgetIds.length; ++i) {
			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			
			Intent clickIntent = new Intent(context, WidgetReceiver.class);
			
			clickIntent.setAction(ACTION_CLICK);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			// intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
			clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));
			PendingIntent clickPendingIntent = PendingIntent.getBroadcast(
					context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.widgetButton, clickPendingIntent);
			appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
			
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}
