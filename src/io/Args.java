package io;

import java.util.HashMap;

public class Args
{
	/*
	 *  Itera el argumentos en parejas de clave valor
	 */
	static final String uses = "cmd -conf_path \"/path/to/config/file\"\n"+
			"ej: cmd -conf_path \"/Users/dokoto/Developments/JAVA/FilmSyncDb/config/config.properties\"";
	
	public static HashMap<String, String> parse(String[] args) throws Exception
	{
		if (args == null)
			throw new Exception("Sin argumentos. Use :" + uses);
		
		if (args.length == 0)
			throw new Exception("Sin argumentos. Use :" + uses);
		
		if (args.length % 2 != 0)
			throw new Exception("Los argumento simpre van en parejas clave:valor. Use :" + uses);
		
		HashMap<String, String> newArgs = new HashMap<String, String>();
		for(int i = 0; i < args.length;)
		{
			if (args[i].compareTo("-conf_path") != 0)
				throw new Exception("Argumento : " + args[i] + " desconocido. Use :" + uses);
			
			newArgs.put(args[i].substring(1), args[i+1]);
			i += 2;
		}
		
		return newArgs;
	}
}
