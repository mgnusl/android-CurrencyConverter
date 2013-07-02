package com.example.currencyconverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "APP";

	TextView tekst1, tekst2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tekst1 = (TextView) findViewById(R.id.tekst1);
		tekst2 = (TextView) findViewById(R.id.tekst2);
		
		new DownloadJSON().execute();
	
	}

	private class DownloadJSON extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				String s = getJSON("http://query.yahooapis.com/v1/public/yql?q=SELECT%20*%20FROM%20eurofx.daily&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");
				JSONObject jObject;
				jObject = new JSONObject(s);
				
				Map<String, Double> currencyRates = new HashMap<String, Double>() ;
					
				JSONObject query = jObject.getJSONObject("query");
				JSONObject results = query.getJSONObject("results");
				JSONArray cube = results.getJSONArray("Cube");
				
				if(cube != null) {
					int arrayLength = cube.length();
					for(int i = 0; i < arrayLength; i++) {
						
						JSONObject currency = (JSONObject) cube.get(i);
						currencyRates.put(currency.getString("currency"), Double.parseDouble((currency.getString("rate"))));					
						
					}
				}
				
				for (Map.Entry entry : currencyRates.entrySet()) {
					Log.d(TAG, ("Key = " + entry.getKey() + ", Value = " + entry.getValue()));
		        }
				

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
		}

	}

	private String getJSON(String URL) throws ClientProtocolException, IOException {
		
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(URL);
		HttpResponse response = client.execute(httpGet);
		HttpEntity entity = response.getEntity();
		
		InputStream inStream = entity.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
		
		String c;
		while ((c = reader.readLine()) != null) {
			builder.append(c);
		}
		
		return builder.toString();
	}

}
