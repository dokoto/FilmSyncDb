package db;

import io.ConfigBuilder.e_exec_mode;
import io.ConfigBuilder.e_exec_mode_orig;

import java.util.ArrayList;

import structs.Film;
import structs.SubMovieFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

/* 
 {
 "titulo":"pelicula",
 "sinppsis":"sinopsis pelicula",
 "foto_mini":"BINARIO",
 "foto_maxi":"BINARIO",
 "titulos_alternativos": ["titulo1", "titulo2"],
 "generos": ["drama", "sci-fi"],
 "directores": ["director1", "director2"],
 "actores": ["actor1", "actor2"]
 }
 */

public class DDBB
{
	private String host;
	private String user;
	private String password;
	private String db_name;
	private int db_port;
	private DB db_handle;
	MongoClient mongoClient;
	private e_exec_mode_orig exec_mode_orig;
	private e_exec_mode exec_mode;

	public DDBB(String host, String user, String password, String db_name, int db_port, e_exec_mode_orig exec_mode_orig, e_exec_mode e_exec_mode)
	{
		this.host = host;
		this.user = user;
		this.password = password;
		this.db_name = db_name;
		this.db_port = db_port;
		db_handle = null;
		mongoClient = null;
		this.exec_mode_orig = exec_mode_orig;
		this.exec_mode = e_exec_mode;
	}

	public void close()
	{
		if (null != mongoClient)
			mongoClient.close();
	}

	public boolean connect()
	{
		try
		{
			mongoClient = new MongoClient(host, db_port);
			db_handle = mongoClient.getDB(db_name);
			boolean auth = db_handle.authenticate(user, password.toCharArray());
			if (!auth)
				return false;
			else
				return true;

		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public void insertInFilms(Film film, byte[] small_img, byte[] big_img)
	{
		DBCursor cursor = null;
		try
		{
			final String collection_name = "films";
			DBCollection coll = db_handle.getCollection(collection_name);
			db_handle.setWriteConcern(WriteConcern.SAFE);
			if (exec_mode == e_exec_mode.UPDATE_DB_NEW || exec_mode == e_exec_mode.UPDATE_FROM_FAILS)
			{
				BasicDBObject query = new BasicDBObject("titulo", film.titulo()).append("release_date", film.release_date());
				cursor = coll.find(query);
				if (cursor.count() > 0)
				{
					System.out.print(" ...[" + exec_mode.toString() + "], La pelicula ya existe en la Base de Datos : " + film.titulo() + " - "
							+ film.release_date());
					return;
				}
				cursor.close();
			}
			
			if (exec_mode == e_exec_mode.UPDATE_DB_BREAK_PATHS)
			{
				BasicDBObject query = new BasicDBObject("titulo", film.titulo()).append("release_date", film.release_date());
				cursor = coll.find(query);
				if (cursor.count() > 0)
				{
					BasicDBObject searchQuery = new BasicDBObject("titulo", film.titulo()).append("release_date", film.release_date());					
					BasicDBObject newDocument = new BasicDBObject().append("$set", new BasicDBObject().append(
							"fileAttributes",
							new BasicDBObject("file_name", film.file().file_name).append("file_extension", film.file().file_extension).append("file_full_path",
									film.file().file_full_path)));
					
					coll.update(searchQuery, newDocument);
					System.out.println(" ... updated OK");
				}
				cursor.close();
			} else
			{
				BasicDBObject document = new BasicDBObject();
				document.append("titulo", film.titulo());
				document.append("release_date", film.release_date());
				document.append("sinopsis", film.sinopsis());
				ArrayList<BasicDBObject> x = new ArrayList<BasicDBObject>();
				BasicDBObject item = null;
				for (int i = 0; i < film.titulos_alternativos().size(); i++)
				{
					item = new BasicDBObject();
					item.put("titulo_alt", film.titulos_alternativos().get(i).getValue());
					item.put("titulo_alt_lang", film.titulos_alternativos().get(i).getKey());
					x.add(item);
				}
				document.append("titulos_alternativos", x);
				document.append("generos", film.generos().toArray(new String[film.generos().size()]));
				document.append("directores", film.directores().toArray(new String[film.directores().size()]));
				document.append("actores", film.actores().toArray(new String[film.actores().size()]));
				document.append("fileAttributes", new BasicDBObject("file_name", film.file().file_name).append("file_extension", film.file().file_extension)
						.append("file_full_path", film.file().file_full_path));

				if (null != small_img && null != big_img)
				{
					GridFS gfsPhoto = new GridFS(db_handle, "photo");
					GridFSInputFile gfsFile = gfsPhoto.createFile(small_img);
					gfsFile.setFilename(film.foto_mini_name());
					gfsFile.save();

					gfsFile = gfsPhoto.createFile(big_img);
					gfsFile.setFilename(film.foto_grande_name());
					gfsFile.save();

					document.append("foto_mini", film.foto_mini_name());
					document.append("foto_maxi", film.foto_grande_name());
				} else
				{
					document.append("foto_mini", "NULL");
					document.append("foto_maxi", "NULL");
				}

				coll.insert(document);
				System.out.println(" ... updated OK");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (null != cursor)
				cursor.close();
		}

	}
}
