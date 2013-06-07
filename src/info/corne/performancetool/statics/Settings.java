package info.corne.performancetool.statics;

public interface Settings {
	public static final String SELECTED_FREQ_SETTING = "info.corne.performancetool.selectedFrequencyCap";
	public static final String SELECTED_GOV_SETTING = "info.corne.performancetool.selectedGovernor";
	public static final String SET_ON_BOOT_SETTING = "info.corne.performancetool.setOnBootSetting";
	public static final String SELECTED_SCHEDULER_SETTING = "info.corne.performancetool.selectedScheduler";
	public static final String OC_ENABLED = "info.corne.performancetool.overclockEnabled";
	public static final String PROFILES = "info.corne.performancetool.profiles";
	public static final String MAX_CPUS = "info.corne.performancetool.maxCpus";
	public static final String SUSPEND_FREQ = "info.corne.performancetool.suspendFreq";
	public static final String AUDIO_MIN_FREQ = "info.corne.performancetool.audioMinFreq";
	public static final String SELECTED_CPQGOV_SETTING = "info.corne.performancetool.selectedCPQGovernor";
	public static final String CURRENT_WIDGET_PROFILE = "info.corne.performancetool.currentWidgetProfile";
	public static final String LP_OC_ENABLED = "info.corne.performancetool.lpOverclockEnabled";
	public static final String SCREEN_STATE = "info.corne.performancetool.screenState";
	public static final String WIFI_ALREADY_ON = "info.corne.performancetool.wifiAlreadyOn";
	public static final String AUTO_WIFI = "info.corne.performancetool.autoWifi";
	public static final String GPU_SCALING = "info.corne.performancetool.gpuScaling";
	public static final String CPU_HOTPLUGGING = "info.corne.performancetool.cpuHotplugging";
	public static final String ACTIVE_CPUS = "info.corne.performancetool.activeCpus";
    public static final String GPU_QUICK_OC = "info.corne.performancetool.gpuQuickOC";
    public static final String A2DP_MIN_FREQ = "info.corne.performancetool.a2dpinFreq";

    // all settings that are stored in a profile
    public static final String[] ALL_PROFILE = new String[] {
        SELECTED_FREQ_SETTING,
        SELECTED_GOV_SETTING,
        SELECTED_SCHEDULER_SETTING,
        OC_ENABLED,
        MAX_CPUS,
        SUSPEND_FREQ,
        AUDIO_MIN_FREQ,
        SELECTED_CPQGOV_SETTING,
        LP_OC_ENABLED,
        AUTO_WIFI,
        GPU_SCALING,
        CPU_HOTPLUGGING,
        ACTIVE_CPUS,
        GPU_QUICK_OC,
        A2DP_MIN_FREQ
    };
}
