package info.corne.performancetool;
/**
 * This class will handle all the widget stuff.
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
 * 
 * @author Corné Dorrestijn
 *
 */
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
            if(current == 2)
            {
                rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_audio);
                current = 1;
                AudioSettings settings = new AudioSettings();
                String[] files = settings.getFileNames();
                String[] values = settings.getValues();

                SetHardwareInfoTask task = new SetHardwareInfoTask(files, values);
                task.execute();
            }
            else if (current == 1)
            {
                rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_power);
                current = 2;
                PowerSettings settings = new PowerSettings();
                String[] files = settings.getFileNames();
                String[] values = settings.getValues();

                SetHardwareInfoTask task = new SetHardwareInfoTask(files, values);
                task.execute();
            }
            else if (current == 0)
            {
                rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_default);
                current = 0;
                DefaultSettings settings = new DefaultSettings();
                String[] files = settings.getFileNames();
                String[] values = settings.getValues();

                SetHardwareInfoTask task = new SetHardwareInfoTask(files, values);
                task.execute();
            } else {
            }
            
            Editor editor = sharedPreferences.edit();
            editor.putInt(Settings.CURRENT_WIDGET_PROFILE, current);
            editor.commit();
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
            clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(
                    context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widgetButton, clickPendingIntent);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
            
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
