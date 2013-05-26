package info.corne.performancetool.statics;

import android.content.SharedPreferences;

public class ProfileSettings extends DefaultSettings {

    public ProfileSettings(String selectedProfile, SharedPreferences sharedPreferences) {
        super();
        CPU_USER_CAP = sharedPreferences.getString(Settings.SELECTED_FREQ_SETTING + selectedProfile, "0");
        ENABLE_OC = sharedPreferences.getString(Settings.OC_ENABLED + selectedProfile, "0");
        SCALING_GOVERNOR = sharedPreferences.getString(Settings.SELECTED_GOV_SETTING + selectedProfile, "smartmax");
        IO_SCHEDULERS = sharedPreferences.getString(Settings.SELECTED_SCHEDULER_SETTING + selectedProfile, "bfq");
        MAX_CPUS = sharedPreferences.getString(Settings.MAX_CPUS+ selectedProfile, "4");
        SUSPEND_FREQ = sharedPreferences.getString(Settings.SUSPEND_FREQ+ selectedProfile, "475000");
        AUDIO_MIN_FREQ = sharedPreferences.getString(Settings.AUDIO_MIN_FREQ+ selectedProfile, "102000");
        ENABLE_LP_OC = sharedPreferences.getString(Settings.LP_OC_ENABLED + selectedProfile, "0");
        CPUQUIET_GOVERNOR = sharedPreferences.getString(Settings.SELECTED_CPQGOV_SETTING + selectedProfile, "rq_stats");
        CPU_HOTPLUGGING = sharedPreferences.getString(Settings.CPU_HOTPLUGGING + selectedProfile, "1");
        GPU_SCALING = sharedPreferences.getString(Settings.GPU_SCALING + selectedProfile, "1");
        ACTIVE_CPUS = sharedPreferences.getString(Settings.ACTIVE_CPUS + selectedProfile, "0 0 0");
	    AUTO_WIFI = sharedPreferences.getBoolean(Settings.AUTO_WIFI + selectedProfile, false);
    }
}
