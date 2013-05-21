package info.corne.performancetool.statics;

import android.content.res.Resources;
import info.corne.performancetool.R; 

public class DefaultSettings {
	protected String ENABLE_OC = "0";
	protected String CPU_USER_CAP = "0";
	protected String SCALING_GOVERNOR = "smartmax";
	protected String IO_SCHEDULERS = "bfq";
	protected String MAX_CPUS = "4";
	// actually these should be 0 == disabled
	// but this will just confuse people :)
	protected String SUSPEND_FREQ = "475000";
	protected String AUDIO_MIN_FREQ = "102000";
	protected String CPUQUIET_GOVERNOR = "rq_stats";
	protected String ENABLE_LP_OC = "0";
	protected String CPU_HOTPLUGGING_ENABLED = "0";
	protected String GPU_DECOUPLE_ENABLED = "1";
	protected String SELECTED_CPQGOV_SETTING = "rq_stats";
	protected String ACTIVE_CPUS = "0 0 0";
	protected boolean AUTO_WIFI = false;

    public DefaultSettings() {
    }
    
    public String[] getFileNames(){
        return new String[] {
                FileNames.CPU_USER_CAP,
                FileNames.ENABLE_OC,
                FileNames.SCALING_GOVERNOR,
                FileNames.IO_SCHEDULERS,
                FileNames.MAX_CPUS_MPDEC,
                FileNames.MAX_CPUS_QUIET,
                FileNames.SUSPEND_FREQ,
                FileNames.AUDIO_MIN_FREQ,
                FileNames.ENABLE_LP_OC,
                FileNames.GPU_DECOUPLE,
                FileNames.CPUQUIET_GOVERNOR,
                FileNames.MANUAL_HOTPLUG,
                FileNames.ACTIVE_CPUS
            };
    }
    
    public String[] getValues() {
        return new String[] {
                CPU_USER_CAP,
                ENABLE_OC,
                SCALING_GOVERNOR,
                IO_SCHEDULERS,
                MAX_CPUS,
                MAX_CPUS,
                SUSPEND_FREQ,
                AUDIO_MIN_FREQ,
                ENABLE_LP_OC,
                GPU_DECOUPLE_ENABLED,
                CPUQUIET_GOVERNOR,
                CPU_HOTPLUGGING_ENABLED,
                ACTIVE_CPUS
            };
    }
    
    private String getIntValueString(String intString) {
        int value = Integer.parseInt(intString);
        return (value == 1?"true":"false");
    }

    private String getFreqValueString(String freqString, Resources resources) {
        int value = Integer.parseInt(freqString);
        if (value == 0)
            return resources.getString(R.string.disabled_string);
        return Integer.toString(value/1000)+resources.getString(R.string.mhz);
    }
    
    public void dump(StringBuilder message, Resources resources){
        message.append(resources.getString(R.string.cpu_cap) + ":" + getFreqValueString(CPU_USER_CAP, resources) + "\n");
        if (CPU_HOTPLUGGING_ENABLED.equals("0")){
            message.append(resources.getString(R.string.cpu_hotplug_mode) + "\n");
            message.append(resources.getString(R.string.cpq_governor) + ":" + CPUQUIET_GOVERNOR + "\n");
            message.append(resources.getString(R.string.max_cpus) + ":" + MAX_CPUS + "\n");
        } else {
            message.append(resources.getString(R.string.cpu_manual_mode) + "\n");
            message.append(resources.getString(R.string.active_cpus) + ":" + ACTIVE_CPUS + "\n");
        }
        message.append(resources.getString(R.string.governor) + ":" + SCALING_GOVERNOR + "\n");
        message.append(resources.getString(R.string.allow_overclock) + ":" + getIntValueString(ENABLE_OC) + "\n");
        message.append(resources.getString(R.string.allow_lpoverclock) + ":" + getIntValueString(ENABLE_LP_OC) + "\n");
        message.append(resources.getString(R.string.io_scheduler) + ":" + IO_SCHEDULERS + "\n");
        message.append(resources.getString(R.string.suspend_cap) + ":" + getFreqValueString(SUSPEND_FREQ, resources) + "\n");
        message.append(resources.getString(R.string.audio_cap) + ":" + getFreqValueString(AUDIO_MIN_FREQ, resources) + "\n");
        message.append(resources.getString(R.string.auto_wifi) + ":" + Boolean.toString(AUTO_WIFI) + "\n");
        message.append(resources.getString(R.string.allow_gpu_decouple) + ":" + getIntValueString((GPU_DECOUPLE_ENABLED.equals("1")?"0":"1")) + "\n");
    }
}
