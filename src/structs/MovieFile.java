package structs;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;

public class MovieFile
{
	private String file_name;
	private String file_extension;
	private File file;
	private int id;
	private String title; 

	public enum e_tipo {
		UNIX, WIN
	};

	private e_tipo tipo;

	public MovieFile()
	{
	}

	public MovieFile(File file)
	{
		this.file = file;
		this.file_name = file.getName();
		this.file_extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('.'));
		this.file_name = this.file.getName().replaceFirst("[.][^.]+$", "");
		this.tipo =  ( File.pathSeparator.compareTo("/") == 0 ) ? e_tipo.UNIX : e_tipo.WIN;
	}
	
	public MovieFile(String full_path, String file_name, String file_extension, e_tipo tipo)
	{
		this.file = new File(full_path);
		this.file_name = file_name;
		this.file_extension = file_extension;
		this.tipo = tipo;
	}

	public MovieFile(String full_path)
	{
		this.file = new File(full_path);
		this.file_extension = full_path.substring(full_path.lastIndexOf('.'));
		this.file_name = this.file.getName().replaceFirst("[.][^.]+$", "");
		this.tipo =  ( File.pathSeparator.compareTo("/") == 0 ) ? e_tipo.UNIX : e_tipo.WIN;
	}
	
	public MovieFile(JsonNode node)
	{
		this.file = new File(node.path("path").asText());
		this.title = node.path("new_title").asText();
		this.id = node.path("new_id").asInt();
		this.file_name = this.file.getName().replaceFirst("[.][^.]+$", "");
		this.tipo =  ( File.pathSeparator.compareTo("/") == 0 ) ? e_tipo.UNIX : e_tipo.WIN;
	}
	
	public SubMovieFile getSubMovieFile()
	{
		return new SubMovieFile(this.file_name, this.file_extension, this.file.getAbsolutePath());
	}

	public int id()
	{
		return this.id;
	}
	
	public String title()
	{
		return this.title;
	}
	
	public File file()
	{ 
		return this.file;
	}

	public String file_name()
	{
		return this.file_name;
	}

	public String file_extension()
	{
		return this.file_extension;
	}

	public e_tipo tipo()
	{
		return this.tipo;
	}

}
