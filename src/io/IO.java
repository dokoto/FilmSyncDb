package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import structs.MovieFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IO
{
	public static ArrayList<MovieFile> findAllFilesInPath(ArrayList<MovieFile> files, MovieFile item)
	{
		if (files == null)
			files = new ArrayList<MovieFile>();

		if (!item.file().isDirectory())
		{
			files.add(item);
			return files;
		}

		for (File file : item.file().listFiles())
			findAllFilesInPath(files, new MovieFile(file));

		return files;
	}

	public static ArrayList<MovieFile> findAllFilesInFailFile(final String path)
	{
		try
		{
			ArrayList<MovieFile> ret = new ArrayList<MovieFile>();
			ObjectMapper mapper = new ObjectMapper();
			File fpath = new File(path);
			if (fpath.length() == 0)
				throw new Exception("Fichero de carga desde films no encontrados esta vacio");
			JsonNode ArrayrootNode = mapper.readTree(new FileInputStream(fpath));
			for (int i = 0; i < ArrayrootNode.size(); i++)
			{
				if (ArrayrootNode.get(i).path("new_id") != null || ArrayrootNode.get(i).path("new_id") != null)
					ret.add(new MovieFile(ArrayrootNode.get(i)));
			}
			return ret;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static ArrayList<MovieFile> findAllFilesInFile(final String path)
	{
		ArrayList<MovieFile> file = new ArrayList<MovieFile>();
		BufferedReader br = null;
		try
		{
			// br = new BufferedReader(new FileReader(path));
			br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
			String line = br.readLine();
			while (line != null)
			{
				file.add(new MovieFile(line));
				line = br.readLine();
			}
			return file;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		} finally
		{
			try
			{
				br.close();
			} catch (IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}
}
