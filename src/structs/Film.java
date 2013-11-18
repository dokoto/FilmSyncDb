package structs;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class Film
{
	private String titulo;
	private ArrayList<Map.Entry<String, String>> titulos_alternativos;
	private ArrayList<String> generos;
	private ArrayList<String> directores;
	private ArrayList<String> actores;
	private String release_date;
	private String sinopsis;
	private String foto_mini_url, foto_mini_name, foto_grande_url, foto_grande_name;
	private SubMovieFile file;


	public Film(final JsonNode detailed_info, final JsonNode credits_info, final JsonNode alt_title_info, 
			final String foto_mini_url, final String foto_mini_name, final String foto_grande_url, final String foto_grande_name,
			SubMovieFile file)
	{
		this.titulos_alternativos = new ArrayList<Map.Entry<String, String>>();
		this.generos = new ArrayList<String>();
		this.directores = new ArrayList<String>();
		this.actores = new ArrayList<String>();
		this.foto_grande_url = foto_grande_url;
		this.foto_grande_name = foto_grande_name;
		this.foto_mini_name = foto_mini_name;
		this.foto_mini_url = foto_mini_url;
		this.titulo = detailed_info.path("original_title").asText();
		this.sinopsis = detailed_info.path("overview").asText();
		this.release_date = detailed_info.path("release_date").asText();
		this.file = file;
		
		JsonNode jsonArray = detailed_info.path("genres");
		for(int i = 0; i < jsonArray.size(); i++)
			this.generos.add(jsonArray.get(i).path("name").asText());
				
		jsonArray = credits_info.path("crew");
		for(int i = 0; i < jsonArray.size(); i++)
		{
			if (jsonArray.get(i).path("department").asText().compareTo("Directing") == 0)
				this.directores.add(jsonArray.get(i).path("name").asText());
		}
		
		jsonArray = credits_info.path("crew");
		for(int i = 0; i < jsonArray.size(); i++)		
			this.actores.add(jsonArray.get(i).path("name").asText());
		
		jsonArray = alt_title_info.path("titles");
		for(int i = 0; i < jsonArray.size(); i++)		
			this.titulos_alternativos.add(new AbstractMap.SimpleEntry<String, String>(jsonArray.get(i).path("iso_3166_1").asText(), 
					jsonArray.get(i).path("title").asText()));
	}


	public Film() {}
	
	public String foto_mini_url() { return this.foto_mini_url; }
	public String foto_grande_url() { return this.foto_grande_url; }
	public String foto_mini_name() { return this.foto_mini_name; }
	public String foto_grande_name() { return this.foto_grande_name; }
	public String titulo() { return this.titulo; }
	public ArrayList<Map.Entry<String, String>> titulos_alternativos() { return this.titulos_alternativos; }
	public ArrayList<String> generos() { return this.generos; }
	public ArrayList<String> directores() { return this.directores; }
	public ArrayList<String> actores() { return this.actores; }
	public String sinopsis() { return this.sinopsis; }
	public String release_date() { return this.release_date; }
	public SubMovieFile file() { return this.file; }
}
