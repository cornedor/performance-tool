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
}