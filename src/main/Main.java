package main;

import java.util.HashMap;

import io.ConfigBuilder;
import mdbAPI.Mdb;

public class Main
{

	public static void main(String[] args)
	{
		ConfigBuilder cc = null;
		HashMap<String, String> arguments = null;
		try
		{
			arguments = io.Args.parse(args);
			cc = new ConfigBuilder(arguments.get("conf_path"));
			cc.Validate();
			Mdb filmSync = cc.CreateRefToMDB();
			filmSync.sync();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (cc.GetRefToFilmNoFound() != null)
					cc.GetRefToFilmNoFound().close();
				if (cc.GetRefToMongoDB() != null)
					cc.GetRefToMongoDB().close();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
