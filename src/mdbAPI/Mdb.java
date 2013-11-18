package mdbAPI;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import structs.Film;
import structs.MovieFile;

import com.fasterxml.jackson.databind.JsonNode;

import conn.Conn;
import db.DDBB;
import io.ConfigBuilder.e_exec_mode_orig;
import io.ConfigBuilder.e_exec_mode;
import io.IO;
import io.IOLog;

// SEARCH EXAMPLE : http://api.themoviedb.org/3/search/movie?api_key=0e9796ef18de2909a5d01cb3fa89d379&query=Planet+51
// MOVIE INFO EXAMPLE : http://api.themoviedb.org/3/movie/16866?api_key=0e9796ef18de2909a5d01cb3fa89d379
// CREDITS INFO EXAMPLE : http://api.themoviedb.org/3/movie/16866/credits?api_key=0e9796ef18de2909a5d01cb3fa89d379
// ALTERNATIVE TITLE INFO EXAMPLE : http://api.themoviedb.org/3/movie/16866/alternative_titles?api_key=0e9796ef18de2909a5d01cb3fa89d379
// IMAGE EXAMPLE : http://d3gtl9l2a4fn1j.cloudfront.net/t/p/w1280/z7z5fg7Ml1hHg67Sbk6pJmmOOT1.jpg

public class Mdb
{
	public static enum e_modo {
	};

	private final String API_config = "3/configuration?api_key=%s";
	private final String API_url = "http://api.themoviedb.org/";
	private final String API_url_search = "3/search/movie?api_key=%s&query=%s";
	private final String API_url_getInfo = "3/movie/%d?api_key=%s";
	private final String API_url_getAlternativeTitle = "3/movie/%d/alternative_titles?api_key=%s";
	private final String API_url_getCredits = "3/movie/%d/credits?api_key=%s";
	private final String API_key = "0e9796ef18de2909a5d01cb3fa89d379";
	private String API_base_url;
	private final String API_base_small_size = "w300", API_base_big_size = "w1280";

	private ArrayList<MovieFile> MovieFiles = null;
	private OutputStream LogToReprocess = null;
	private IOLog NoFoundLog = null;
	private DDBB db = null;
	private e_exec_mode_orig exec_mode_orig;
	private e_exec_mode exec_mode;

	public Mdb(final String path, OutputStream LogToReprocess, DDBB db, e_exec_mode_orig exec_mode_orig, e_exec_mode e_exec_mode)
	{
		getConfiguration();
		MovieFiles = new ArrayList<MovieFile>();
		this.exec_mode_orig = exec_mode_orig;
		this.exec_mode = e_exec_mode;
		if (exec_mode_orig == e_exec_mode_orig.FROM_PATH)
		{
			if (exec_mode == io.ConfigBuilder.e_exec_mode.UPDATE_DB)
			{				
				MovieFiles = IO.findAllFilesInPath(MovieFiles, new MovieFile(path));
			}
			else
				MovieFiles = new ArrayList<MovieFile>();
		} else if (exec_mode_orig == e_exec_mode_orig.FROM_FILE)
		{
			if (exec_mode == io.ConfigBuilder.e_exec_mode.UPDATE_DB)
			{
				MovieFiles = IO.findAllFilesInFile(path);
			}
			if (exec_mode == io.ConfigBuilder.e_exec_mode.UPDATE_DB_NEW)
			{
				MovieFiles = IO.findAllFilesInFile(path);
			}			
			if (exec_mode == io.ConfigBuilder.e_exec_mode.UPDATE_DB_BREAK_PATHS)
			{
				MovieFiles = IO.findAllFilesInFile(path);
			}
			else if (exec_mode == io.ConfigBuilder.e_exec_mode.UPDATE_FROM_FAILS)
			{
				MovieFiles = IO.findAllFilesInFailFile(path);
			}
		} else
			MovieFiles = new ArrayList<MovieFile>();

		this.LogToReprocess = LogToReprocess;
		this.NoFoundLog = new IOLog(this.LogToReprocess);
		this.db = db;
		this.db.connect();

	}

	public boolean sync()
	{
		if (null == MovieFiles)
			return false;

		ArrayList<MovieFile> PeliculasATratar = ListFimlValidator(MovieFiles);
		int total = PeliculasATratar.size();
		int index = 0;
		for (MovieFile film_path : PeliculasATratar)
		{
			System.out.print("PROCESANDO [" + index++ + "/" + total + "] : " + film_path.file_name());
			db_sync(search(film_path));
		}

		NoFoundLog.SaveLog();
		return true;
	}

	private ArrayList<MovieFile> ListFimlValidator(ArrayList<MovieFile> MovieFiles)
	{
		ArrayList<MovieFile> newMovieFiles = new ArrayList<MovieFile>();
		for (MovieFile film : MovieFiles)
		{
			try
			{
				if (film.file().getName().length() == 0)
					throw new Exception();

				if (film.file().getName().matches("[^a-zA-Z0-9\\s]+") == true)
					throw new Exception();
				Matcher matcher = Pattern.compile("(\\.avi|\\.mpeg|\\.mkv|\\.divx|\\.mpg|\\.ogg|\\.mp4|\\.ogm|\\.m4v)", Pattern.CASE_INSENSITIVE).matcher(film.file().getName().toString());
				if (!matcher.find())
					throw new Exception();

				newMovieFiles.add(film);
			} catch (Exception e)
			{
				NoFoundLog.AddLogTitleNoValid(film);
			}
		}
		return newMovieFiles;
	}

	private String FilmTitleFormater(String film)
	{
		String ret = new String(film);

		// Elimina los PTR, PTR1
		Matcher matcher = Pattern.compile("([-\\s]+(PTR[0-9]|PRT[0-9]))").matcher(ret);
		if (matcher.find())
			ret = matcher.replaceAll("");

		// Elimina la extension
		ret = ret.replaceFirst("[.][^.]+$", "");

		return ret;
	}

	private void getConfiguration()
	{
		Formatter formatter = null;
		try
		{
			StringBuilder sb_request = new StringBuilder();
			formatter = new Formatter(sb_request, Locale.getDefault());
			formatter.format(API_url + API_config, API_key);
			JsonNode json_response = Conn.getJSON(sb_request.toString());
			assert (null != json_response);
			API_base_url = json_response.path("images").path("base_url").asText();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			formatter.close();
		}
	}

	private String getImageUrl(final String size, final String url)
	{
		return API_base_url + size + url;
	}

	private Film process_result(int id, MovieFile movie)
	{
		Formatter formatter = null;		
		try
		{
			StringBuilder detailed_info = new StringBuilder();
			formatter = new Formatter(detailed_info, Locale.getDefault());
			formatter.format(API_url + API_url_getInfo, id, API_key);
			formatter.close();

			JsonNode ArrayJsonDetailed = Conn.getJSON(detailed_info.toString());
			String image_url = ArrayJsonDetailed.path("backdrop_path").asText();
			System.out.print(" ===> TITULO ORIGINAL : " + ArrayJsonDetailed.path("original_title").asText());

			StringBuilder credits_info = new StringBuilder();
			formatter = new Formatter(credits_info, Locale.getDefault());
			formatter.format(API_url + API_url_getCredits, id, API_key);
			formatter.close();

			StringBuilder alt_title_info = new StringBuilder();
			formatter = new Formatter(alt_title_info, Locale.getDefault());
			formatter.format(API_url + API_url_getAlternativeTitle, id, API_key);
			formatter.close();

			return new Film(ArrayJsonDetailed, Conn.getJSON(credits_info.toString()), Conn.getJSON(alt_title_info.toString()), getImageUrl(API_base_small_size,
					image_url), API_base_small_size + image_url, getImageUrl(API_base_big_size, image_url), API_base_big_size + image_url,
					movie.getSubMovieFile());

		} catch (Exception e)
		{
			e.printStackTrace();
			return null;

		}
	}

	private Film process_result(final JsonNode json_response_id, MovieFile movie)
	{
		Formatter formatter = null;
		
		try
		{
			JsonNode ArrayJson = json_response_id.path("results");
			int id = ArrayJson.get(0).path("id").asInt();
			String image_url = ArrayJson.get(0).path("backdrop_path").asText();
			System.out.print(" ===> TITULO ORIGINAL : " + ArrayJson.get(0).path("original_title").asText());

			StringBuilder detailed_info = new StringBuilder();
			formatter = new Formatter(detailed_info, Locale.getDefault());
			formatter.format(API_url + API_url_getInfo, id, API_key);
			formatter.close();

			StringBuilder credits_info = new StringBuilder();
			formatter = new Formatter(credits_info, Locale.getDefault());
			formatter.format(API_url + API_url_getCredits, id, API_key);
			formatter.close();

			StringBuilder alt_title_info = new StringBuilder();
			formatter = new Formatter(alt_title_info, Locale.getDefault());
			formatter.format(API_url + API_url_getAlternativeTitle, id, API_key);
			formatter.close();

			return new Film(Conn.getJSON(detailed_info.toString()), Conn.getJSON(credits_info.toString()), Conn.getJSON(alt_title_info.toString()),
					getImageUrl(API_base_small_size, image_url), API_base_small_size + image_url, getImageUrl(API_base_big_size, image_url), API_base_big_size
							+ image_url, movie.getSubMovieFile());

		} catch (Exception e)
		{
			e.printStackTrace();
			return null;

		}
	}

	private Film search(final MovieFile movie_path)
	{
		String movie_title = null;
		if (exec_mode == io.ConfigBuilder.e_exec_mode.UPDATE_DB)
			movie_title = FilmTitleFormater(movie_path.file().getName());
		else if (exec_mode == io.ConfigBuilder.e_exec_mode.UPDATE_FROM_FAILS)
		{
			if (movie_path.id() == 0)
				movie_title = movie_path.title();
			else
				return process_result(movie_path.id(), movie_path);
		} else
			movie_title = FilmTitleFormater(movie_path.file().getName());
		
		// Chequear si tiene extension
		Formatter formatter = null;
		try
		{
			StringBuilder sb_request = new StringBuilder();
			formatter = new Formatter(sb_request, Locale.getDefault());
			formatter.format(API_url + API_url_search, API_key, movie_title.replace(" ", "+"));
			JsonNode json_response = Conn.getJSON(sb_request.toString());
			if (null == json_response)
				NoFoundLog.AddLogTitleNoFound(movie_path);
			else if (json_response.path("total_results").asInt() == 0)
				NoFoundLog.AddLogTitleNoFound(movie_path);
			else if (json_response.path("total_results").asInt() > 1)
				NoFoundLog.AddLogTitleNoFound(movie_path, json_response);
			else if (json_response.path("total_results").asInt() == 1)
				return process_result(json_response, movie_path);

			return null;

		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		} finally
		{
			formatter.close();
		}
	}

	private void db_sync(final Film film)
	{
		if (null == film)
			return;

		update_db(film);
		/*
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() { update_db(film); } }).start();
		 */
	}

	private void update_db(final Film film)
	{
		byte[] small_img = Conn.getImage(film.foto_mini_url());
		byte[] big_img = Conn.getImage(film.foto_mini_url());
		db.insertInFilms(film, small_img, big_img);
	}
}
