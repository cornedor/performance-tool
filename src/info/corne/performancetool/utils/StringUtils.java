package info.corne.performancetool.utils;

public class StringUtils {
	public static String join(String[] arr, String delimitter)
	{
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for(;i < arr.length - 1;)
			sb.append(arr[i++]+delimitter);
		return sb.toString()+arr[i];
	}
}
