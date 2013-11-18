package conn;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Conn
{
	private static HttpClient client = null;
	private static HttpGet httpGet = null;

	public static byte[] getImage(final String request)
	{
		try
		{
			client = HttpClientBuilder.create().build();
			httpGet = new HttpGet(request);
			String ext = request.substring(request.lastIndexOf('.'));
			httpGet.setHeader("Content-Type", "image/" + ext);
			httpGet.setHeader("Accept", "image/" + ext);
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200)
			{
				HttpEntity entity = response.getEntity();
				return EntityUtils.toByteArray(entity);	
				// Por si necesito bitmap
				// Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
			}
			else return null;
		} catch (Exception e)
		{			
			e.printStackTrace();
			return null;
		}
	}
	
	public static JsonNode getJSON(final String request)
	{
		try
		{
			client = HttpClientBuilder.create().build();
			httpGet = new HttpGet(request);
			httpGet.setHeader("Content-Type", "application/json");
			httpGet.setHeader("Accept", "application/json");
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200)
			{
				HttpEntity entity = response.getEntity();
				ObjectMapper mapper = new ObjectMapper();
				return mapper.readTree(entity.getContent());				
			}
			else return null;
		} catch (Exception e)
		{			
			e.printStackTrace();
			return null;
		}

	}
}
