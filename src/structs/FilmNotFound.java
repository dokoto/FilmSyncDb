package structs;

import io.SubFilmNotFound;

import java.util.ArrayList;

public class FilmNotFound
{
	public String original_title;
	public ArrayList<SubFilmNotFound> alt_titles;
	public String new_id;
	public String new_title;
	public String path;
	
	public FilmNotFound() {}
	
	public FilmNotFound(String original_title, String path)
	{		
		this.original_title = original_title;
		this.path = path;
	}
	
	public FilmNotFound(String original_title, ArrayList<SubFilmNotFound> alt_titles, String path)
	{		
		this.original_title = original_title;
		this.alt_titles = alt_titles;
		this.path = path;
	}
	
	public FilmNotFound(String original_title, ArrayList<SubFilmNotFound> alt_titles, String new_id, String new_title, String path)
	{	
		this.original_title = original_title;
		this.alt_titles = alt_titles;
		this.new_id = new_id;
		this.new_title = new_title;
		this.path = path;
	}
	
}
