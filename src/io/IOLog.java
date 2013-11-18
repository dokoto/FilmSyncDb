package io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import structs.FilmNotFound;
import structs.MovieFile;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class IOLog
{
	private ObjectMapper mapper;
	private OutputStream op;
	private ArrayList<FilmNotFound> FilmNotFounds;
	
	public IOLog(OutputStream op)
	{
		this.mapper = new ObjectMapper();
		this.op = op;
		FilmNotFounds = new ArrayList<FilmNotFound>();
	}
	
	
	public void AddLogTitleNoValid(MovieFile movie_path)
	{
		try
		{
			System.out.println(" *** TITULO NO VALIDO : " + movie_path.file().getName());
			FilmNotFounds.add(new FilmNotFound(movie_path.file().getName(), movie_path.file().getAbsolutePath()));
		} catch (Exception e)
		{
			e.printStackTrace();
		} 
	}	
	
	public void AddLogTitleNoFound(MovieFile movie_path)
	{
		try
		{
			System.out.println(" *** TITULO NO ENCONTRADO : " + movie_path.file().getName());
			FilmNotFounds.add(new FilmNotFound(movie_path.file().getName(), movie_path.file().getAbsolutePath()));
		} catch (Exception e)
		{
			e.printStackTrace();
		} 
	}
	
	public void AddLogTitleNoFound(MovieFile movie_path, JsonNode json_response)
	{
		try
		{
			System.out.println(" *** TITULO ENCONTRADO CON MULTIPLES RESULTADOS : " + movie_path.file().getName());
			ArrayList<SubFilmNotFound> alt_titles = new ArrayList<SubFilmNotFound>();
			JsonNode ArrayJson = json_response.path("results");
			for(int i = 0; i < ArrayJson.size(); i++)
				alt_titles.add(new SubFilmNotFound(ArrayJson.get(i).path("id").asText(),
						ArrayJson.get(i).path("original_title").asText(), 
						ArrayJson.get(i).path("release_date").asText()));
			FilmNotFounds.add(new FilmNotFound(movie_path.file().getName(), alt_titles, movie_path.file().getAbsolutePath()));
		
		} catch (Exception e)
		{
			e.printStackTrace();
		} 
	}
	
	public void SaveLog()
	{
		try
		{
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			mapper.writeValue(op, FilmNotFounds);
		} catch (JsonGenerationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
