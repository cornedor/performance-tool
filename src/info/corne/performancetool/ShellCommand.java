package info.corne.performancetool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShellCommand {
	public static String run(String cmd[])
	{
		String res = "";
		try
		{
			Process process = new ProcessBuilder(cmd).start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            try {
                process.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(ShellCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
            String line;
            while ((line = stderr.readLine()) != null) {
                res = res + "\n" + line;
            }
            while ((line = stdout.readLine()) != null) {
                res = res + "\n" + line;
                while ((line = stderr.readLine()) != null) {
                    res = res + "\n" + line;
                }
            }
		}
		catch(IOException e)
		{
			return "Error: " + e.getMessage();
		}
		return res;
	}
}
