package info.corne.performancetool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
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
 * @author corne
 *
 */
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
