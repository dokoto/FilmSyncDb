package structs;

public class SubMovieFile
{
	public String file_name;
	public String file_extension;
	public String file_full_path;
	
	public SubMovieFile() 
	{
		this("", "", "");
	}
	public SubMovieFile(String file_name, String file_extension, String file_full_path)
	{
		this.file_name = file_name;
		this.file_extension = file_extension;
		this.file_full_path = file_full_path;
	}

}
