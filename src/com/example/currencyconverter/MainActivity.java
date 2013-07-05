package com.example.currencyconverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
	
	private TextView resultTV;
	private EditText amountET;
	private Spinner fromSpinner, toSpinner;
	private Button calculateButton;
	
	private String toCurrency, fromCurrency;
	private double currencyRate, amount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		fromSpinner = (Spinner)findViewById(R.id.fromCurrencySpinner);
		toSpinner = (Spinner)findViewById(R.id.toCurrencySpinner);
		calculateButton = (Button)findViewById(R.id.calculateButton);
		resultTV = (TextView)findViewById(R.id.resultTV);
		amountET = (EditText)findViewById(R.id.amountET);
		
		setSpinnerAdapter();
		
		currencyRate = 0;
				
		calculateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				calculateCurrency();
			}
		});		
	
	}
	
	private void setSpinnerAdapter() {

		// adding the adapter to both spinners
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.currencies));
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		fromSpinner.setAdapter(dataAdapter);
		toSpinner.setAdapter(dataAdapter);
		
	}
	
	public void calculateCurrency() {
		
		toCurrency = (String) toSpinner.getSelectedItem();
		toCurrency = toCurrency.substring(0, Math.min(3, toCurrency.length()));

		fromCurrency = (String) fromSpinner.getSelectedItem();
		fromCurrency = fromCurrency.substring(0, Math.min(3, fromCurrency.length()));

		amount = Double.parseDouble(amountET.getText().toString());

		new DownloadJSON().execute();
							
	}

	private class DownloadJSON extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				
				String s = getJSON("http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22"
						+ fromCurrency + toCurrency + "%22)&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");
				JSONObject jObject;
				jObject = new JSONObject(s);
				
				JSONObject query = jObject.getJSONObject("query");
				JSONObject results = query.getJSONObject("results");
				currencyRate = Double.parseDouble(results.getJSONObject("rate").getString("Rate"));
							

			} 
			catch (JSONException e) {
				e.printStackTrace();
			} 
			catch (ClientProtocolException e) {
				e.printStackTrace();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}

			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// currencyrate is fetched, continue calculating
			continueCalculate();
		}

	}
	
	public void continueCalculate() {
		
		double result = amount * currencyRate;
		DecimalFormat df = new DecimalFormat("#.##");
		resultTV.setText(df.format(result));
		
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
