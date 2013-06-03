package info.corne.performancetool.statics;

public class PowerSettings extends DefaultSettings {
	public PowerSettings(){
	    super();
        CPU_USER_CAP     = "1150000";
	    SCALING_GOVERNOR = "ondemand";
	}
}
