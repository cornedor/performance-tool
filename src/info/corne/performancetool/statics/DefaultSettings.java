package info.corne.performancetool.statics;

public class DefaultSettings {
	protected String ENABLE_OC = "0";
	protected String CPU_USER_CAP = "0";
	protected String SCALING_GOVERNOR = "smartmax";
	protected String IO_SCHEDULERS = "bfq";
	protected String MAX_CPUS = "4";
	protected String SUSPEND_FREQ = "475000";
	protected String AUDIO_MIN_FREQ = "102000";
	protected String CPUQUIET_GOVERNOR = "rq_stats";
	protected String ENABLE_LP_OC = "0";
	protected String CPU_HOTPLUGGING_ENABLED = "0";
	protected String GPU_DECOUPLE_ENABLED = "1";
	protected String SELECTED_CPQGOV_SETTING = "rq_stats";
	protected String ACTIVE_CPUS = "0 0 0";

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
}
