package com.kreativo.simplecurrencyconverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends SherlockActivity {

	private static final String TAG = "APP";

	private TextView resultTV, toCurrencyTV, fromCurrencyTV, lastUpdatedTV;
	private EditText amountET, resultET;
	private Spinner fromSpinner, toSpinner;
	private Button calculateButton;

	private String toCurrency, fromCurrency;
	private double currencyRate, amount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		fromSpinner = (Spinner) findViewById(R.id.fromCurrencySpinner);
		toSpinner = (Spinner) findViewById(R.id.toCurrencySpinner);
		calculateButton = (Button) findViewById(R.id.calculateButton);
		toCurrencyTV = (TextView) findViewById(R.id.toCurrencyTV);
		fromCurrencyTV = (TextView) findViewById(R.id.fromCurrencyTV);
		lastUpdatedTV = (TextView) findViewById(R.id.lastUpdateTV);
		amountET = (EditText) findViewById(R.id.amountET);
		resultET = (EditText) findViewById(R.id.resultET);

		setSpinnerAdapter();

		currencyRate = 0;
		amount = 0;

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
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		fromSpinner.setAdapter(dataAdapter);
		toSpinner.setAdapter(dataAdapter);

	}

	public void calculateCurrency() {

		toCurrency = (String) toSpinner.getSelectedItem();
		toCurrency = toCurrency.substring(0, Math.min(3, toCurrency.length()));

		fromCurrency = (String) fromSpinner.getSelectedItem();
		fromCurrency = fromCurrency.substring(0,
				Math.min(3, fromCurrency.length()));

		if (amountET.getText().toString().matches("")) {
			Crouton.makeText(this, "Amount cannot be empty..", Style.ALERT)
					.show();
			amount = 0;
			return;
		} else {
			amount = Double.parseDouble(amountET.getText().toString());
		}

		toCurrencyTV.setText(toCurrency);
		fromCurrencyTV.setText(fromCurrency);

		new DownloadJSON().execute();

	}

	private class DownloadJSON extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {

				String s = getJSON("http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22"
						+ fromCurrency
						+ toCurrency
						+ "%22)&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");
				JSONObject jObject;
				jObject = new JSONObject(s);

				JSONObject query = jObject.getJSONObject("query");
				JSONObject results = query.getJSONObject("results");
				currencyRate = Double.parseDouble(results.getJSONObject("rate")
						.getString("Rate"));

			} catch (JSONException e) {
				e.printStackTrace();
				Crouton.makeText(
						MainActivity.this,
						"An error occurred while fetching the latest currency rates",
						Style.ALERT).show();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				Crouton.makeText(
						MainActivity.this,
						"An error occurred while fetching the latest currency rates",
						Style.ALERT).show();
			} catch (IOException e) {
				e.printStackTrace();
				Crouton.makeText(
						MainActivity.this,
						"An error occurred while fetching the latest currency rates",
						Style.ALERT).show();
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
		resultET.setText(df.format(result));	
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		String currentDateandTime = sdf.format(new Date());
		lastUpdatedTV.setText("Last update: " + currentDateandTime);

	}

	private String getJSON(String URL) throws ClientProtocolException,
			IOException {

		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(URL);
		HttpResponse response = client.execute(httpGet);
		HttpEntity entity = response.getEntity();

		InputStream inStream = entity.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inStream));

		String c;
		while ((c = reader.readLine()) != null) {
			builder.append(c);
		}

		return builder.toString();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Crouton.cancelAllCroutons();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu sub = menu.addSubMenu("More");
		sub.add(0, 1, 0, "About");
		sub.getItem().setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 1) {
			AlertDialog ad = new AlertDialog.Builder(this).create();
			ad.setCancelable(false);
			ad.setTitle(R.string.about);
			ad.setMessage("Simple Currency Converter uses data from Yahoo! Finance. "
					+ "\n\nThe application offers conversion between 153 currencies.");
			
			ad.setButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			
			ad.show();
		}
		return true;
	}
}
