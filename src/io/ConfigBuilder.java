package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import mdbAPI.Mdb;
import db.DDBB;

public class ConfigBuilder
{
	public enum e_exec_mode_orig {FROM_FILE, FROM_PATH};
	public enum e_exec_mode {UPDATE_FROM_FAILS, UPDATE_DB, UPDATE_DB_NEW, UPDATE_DB_BREAK_PATHS};
	private e_exec_mode_orig exec_mode_orig;
	private e_exec_mode exec_mode;
	private Properties prop = null;
	//private ClassLoader loader = null;
	private InputStream stream = null;
	private Mdb mdb = null;
	private DDBB db = null;
	private String config_path = null;
	private OutputStream LogFilmNoFound = null;

	public ConfigBuilder(String config_path) throws IOException
	{
		prop = new Properties();
		//loader = Thread.currentThread().getContextClassLoader();
		//stream = loader.getResourceAsStream("config.properties");
		this.config_path = config_path;
		stream = new FileInputStream(new File(config_path));
		prop.load(stream);
	}

	public Mdb GetRefToMDB() throws Exception
	{
		return mdb;
	}

	public DDBB GetRefToMongoDB() throws Exception
	{
		return db;
	}

	public OutputStream GetRefToFilmNoFound() throws Exception
	{
		return LogFilmNoFound;
	}

	public Mdb CreateRefToMDB() throws Exception
	{
		LogFilmNoFound = CreateRefToFilmNoFound();
		db = CreateRefToMongoDB();
		
		if (Get("exec_mode").compareTo("UPDATE_FROM_FAILS") == 0 || Get("exec_mode").compareTo("UPDATE_DB") == 0 ||
				Get("exec_mode").compareTo("UPDATE_DB_NEW") == 0 || Get("exec_mode").compareTo("UPDATE_DB_BREAK_PATHS") == 0)
			return new Mdb(Get("exec_mode_orig_path"), LogFilmNoFound, db, exec_mode_orig, exec_mode);
		else
			return null;
	}

	public DDBB CreateRefToMongoDB() throws Exception
	{
		return new DDBB(Get("db_host"), Get("db_user"), Get("db_password"), Get("db_name"), Integer.valueOf(Get("db_port")), this.exec_mode_orig, this.exec_mode);
	}

	public OutputStream CreateRefToFilmNoFound() throws Exception
	{
		File log = new File(Get("log_films_nofound"));
		if (log.exists() == false)
			log.createNewFile();
		return new FileOutputStream(log);
	}

	public void Validate() throws Exception
	{
		// VALIDACION CAMPOS OBLIGATORIOS
		if (Get("db_host").isEmpty())
			throw new Exception("El campo db_host es obligatorio");
		if (Get("db_user").isEmpty())
			throw new Exception("El campo db_user es obligatorio");
		if (Get("db_password").isEmpty())
			throw new Exception("El campo db_password es obligatorio");
		if (Get("db_name").isEmpty())
			throw new Exception("El campo db_name es obligatorio");
		if (Get("db_port").isEmpty())
			throw new Exception("El campo db_port es obligatorio");
		if (Get("log_films_nofound").isEmpty())
			throw new Exception("El campo log_films_nofound es obligatorio");

		// VALIDACION exec_mode_orig_path
		if (Get("exec_mode_orig").compareTo("FROM_PATH") == 0)
		{			
			File f = new File(Get("exec_mode_orig_path"));
			if (!f.exists() || !f.isDirectory())
				throw new Exception("[exec_mode_orig:" + Get("exec_mode_orig") + "] El directorio : " + Get("exec_mode_orig_path") + ", o no existe o no es un directorio.");
			exec_mode_orig = e_exec_mode_orig.FROM_PATH;

		} else if (Get("exec_mode_orig").compareTo("FROM_FILE") == 0)
		{
			File f = new File(Get("exec_mode_orig_path"));
			if (f.exists() && f.isDirectory())
				throw new Exception("[exec_mode_orig:" + Get("exec_mode_orig") + "] El fichero : " + Get("exec_mode_orig_path") + ", parece se un directorio que ya existe.");			
			else if (!f.exists())
				throw new Exception("[exec_mode_orig:" + Get("exec_mode_orig") + "] El fichero : " + Get("exec_mode_orig_path") + ", parece no existir.");
			exec_mode_orig = e_exec_mode_orig.FROM_FILE; 
		} else
			throw new Exception("Valores porbles para 'exec_mode_orig':['FROM_PATH','FROM_FILE']");

		// VALIDACION exec_mode
		if (Get("exec_mode").compareTo("UPDATE_DB") != 0 && Get("exec_mode").compareTo("UPDATE_FROM_FAILS") != 0
				&& Get("exec_mode").compareTo("UPDATE_DB_NEW") != 0 && Get("exec_mode").compareTo("UPDATE_DB_BREAK_PATHS") != 0)
			throw new Exception("[exec_mode:" + Get("exec_mode") + "] Valores porbles para 'exec_mode':['UPDATE_DB','UPDATE_FROM_FAILS', 'UPDATE_DB_NEW', 'UPDATE_DB_BREAK_PATHS']");
		
		
		// VALIDACION DE COMBINACIONES DE PARAMETROS
		if (Get("exec_mode").compareTo("UPDATE_FROM_FAILS") != 0 && Get("exec_mode_orig").compareTo("FROM_FILE") != 0)
			throw new Exception("[exec_mode" + Get("exec_mode") + "] Cuando 'exec_mode'='UPDATE_FROM_FAILS' 'exec_mode_orig' solo puede ser 'FROM_FILE'");
		
		exec_mode = SetExecMode(Get("exec_mode"));	
		
		System.out.println("Modo de ejecucion fijado en los siguientes parametros:");
		System.out.println("exec_mode : " + exec_mode.toString());
		System.out.println("exec_mode_orig : " + exec_mode_orig.toString());
		System.out.println("log_films_nofound : " + Get("log_films_nofound"));
		System.out.println("exec_mode_orig_path : " + Get("exec_mode_orig_path"));
		System.out.println("config_path : " + config_path + "\n\n");
		
	}

	public e_exec_mode SetExecMode(String mode) throws Exception
	{
		if (Get("exec_mode").compareTo("UPDATE_DB") == 0)
			return e_exec_mode.UPDATE_DB;
		else if (Get("exec_mode").compareTo("UPDATE_FROM_FAILS") == 0)
			return e_exec_mode.UPDATE_FROM_FAILS;
		else if (Get("exec_mode").compareTo("UPDATE_DB_NEW") == 0)
			return e_exec_mode.UPDATE_DB_NEW;
		else if (Get("exec_mode").compareTo("UPDATE_DB_BREAK_PATHS") == 0)
			return e_exec_mode.UPDATE_DB_BREAK_PATHS;
		else
			return null;
	}
	
	public String Get(String key) throws Exception
	{
		return prop.getProperty(key);
	}
}
