package com.example.currencyconverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "APP";
	
	private Map<String, Double> currencyRates;

	private TextView resultTV;
	private EditText amountET;
	private Spinner fromSpinner, toSpinner;
	private Button calculateButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		fromSpinner = (Spinner)findViewById(R.id.fromCurrencySpinner);
		toSpinner = (Spinner)findViewById(R.id.toCurrencySpinner);
		calculateButton = (Button)findViewById(R.id.calculateButton);
		resultTV = (TextView)findViewById(R.id.resultTV);
		amountET = (EditText)findViewById(R.id.amountET);
		
		calculateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				calculateCurrency();

			}
		});

		new DownloadJSON().execute();
		
		
	
	}
	
	@SuppressWarnings("rawtypes")
	private void setSpinnerAdapter() {
		
		// iterates through the hashmap, adding the keys to an arraylist
		ArrayList<String> list = new ArrayList<String>();
		Iterator it = currencyRates.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        list.add((String)pairs.getKey());
	    }
		
	    // adding the adapter to both spinners
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		fromSpinner.setAdapter(dataAdapter);
		toSpinner.setAdapter(dataAdapter);
		
		
	}
	
	public void calculateCurrency() {
		
		String from = (String) fromSpinner.getSelectedItem();
		String to = (String) toSpinner.getSelectedItem();
		double amount = Double.parseDouble(amountET.getText().toString());

		// calculates from currency to currency via euro
		double result = (amount / currencyRates.get(from)) * currencyRates.get(to);
		
		resultTV.setText(Double.toString(result));
		
		
		
	}

	private class DownloadJSON extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				String s = getJSON("http://query.yahooapis.com/v1/public/yql?q=SELECT%20*%20FROM%20eurofx.daily&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");
				JSONObject jObject;
				jObject = new JSONObject(s);
				
				currencyRates = new HashMap<String, Double>() ;
					
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
					//Log.d(TAG, ("Key = " + entry.getKey() + ", Value = " + entry.getValue()));
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
			setSpinnerAdapter();
						
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
