package info.corne.performancetool.statics;

public interface FileNames {
	public static final String CPU_USER_CAP = "/sys/module/cpu_tegra/parameters/cpu_user_cap";
	public static final String ENABLE_OC = "/sys/module/cpu_tegra/parameters/enable_oc";
	public static final String SCALING_GOVERNOR = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
	public static final String SCALING_AVAILABLE_FREQUENCIES = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
	public static final String SCALING_AVAILABLE_GOVERNORS = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
	public static final String IO_SCHEDULERS = "/sys/block/mmcblk0/queue/scheduler";
	public static final String MAX_CPUS_MPDEC = "/sys/kernel/tegra_mpdecision/conf/max_cpus";
	public static final String MAX_CPUS_QUIET = "/sys/devices/system/cpu/cpuquiet/tegra_cpuquiet/max_cpus";	
	public static final String SUSPEND_FREQ = "/sys/module/cpu_tegra/parameters/suspend_cap_freq";
	public static final String AUDIO_MIN_FREQ = "/sys/module/snd_soc_tlv320aic3008/parameters/audio_min_freq";
	public static final String CPUQUIET_AVAILABLE_GOVERNORS = "/sys/devices/system/cpu/cpuquiet/available_governors";
	public static final String CPUQUIET_GOVERNOR = "/sys/devices/system/cpu/cpuquiet/current_governor";
	public static final String ENABLE_LP_OC = "/sys/module/cpu_tegra/parameters/enable_lp_oc";
	public static final String GPU_SCALING = "/sys/devices/gr3d/enable_3d_scaling";
	public static final String ACTIVE_CPUS = "/sys/devices/system/cpu/cpuquiet/tegra_cpuquiet/cpu_core_state";
	public static final String MANUAL_HOTPLUG= "/sys/devices/system/cpu/cpuquiet/tegra_cpuquiet/manual_hotplug";
    public static final String GPU_QUICK_OC= "/sys/kernel/tegra3_dvfs/gpu_quick_oc";
}

