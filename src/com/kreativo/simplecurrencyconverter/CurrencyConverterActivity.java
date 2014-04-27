package com.kreativo.simplecurrencyconverter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.cengalabs.flatui.FlatUI;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class CurrencyConverterActivity extends ActionBarActivity {

    private TextView toCurrencyTV, fromCurrencyTV, lastUpdatedTV;
    private EditText amountET, resultET;
    private Spinner fromSpinner, toSpinner;
    private Button calculateButton, arrowButton;

    private String toCurrency, fromCurrency;
    private double currencyRate, amount;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FlatUI.setDefaultTheme(FlatUI.BLOOD);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Actionbar style
        FlatUI.setActionBarTheme(this, FlatUI.DARK, false, true);
        getSupportActionBar().setBackgroundDrawable(FlatUI.getActionBarDrawable(FlatUI.DARK, false));
        getActionBar().setTitle(Html.fromHtml("<font color=\"#f2f2f2\">" + getResources().getString(R.string.app_name)
                + "</font>"));

        // ADS
        adView = new AdView(this);
        adView.setAdUnitId("a151d70cf75e5a8");
        adView.setAdSize(AdSize.BANNER);
        TableRow layout = (TableRow) findViewById(R.id.adTableRow);
        layout.addView(adView);
        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);

        fromSpinner = (Spinner) findViewById(R.id.fromCurrencySpinner);
        toSpinner = (Spinner) findViewById(R.id.toCurrencySpinner);
        calculateButton = (Button) findViewById(R.id.calculateButton);
        arrowButton = (Button) findViewById(R.id.arrowButton);
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
                // Check if internet connection is available
                if(!NetworkChecker.isNetworkAvailable(CurrencyConverterActivity.this)) {
                    Crouton.makeText(CurrencyConverterActivity.this, getResources().getString(R.string.no_inet),
                            Style.ALERT).show();
                    return;
                }
                calculateCurrency();
            }
        });

        arrowButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String prevFrom = (String) fromSpinner.getSelectedItem();
                String prevTo = (String) toSpinner.getSelectedItem();

                @SuppressWarnings("rawtypes")
                ArrayAdapter myAdap = (ArrayAdapter) fromSpinner.getAdapter();

                @SuppressWarnings("unchecked")
                int spinnerPositionFrom = myAdap.getPosition(prevFrom);
                @SuppressWarnings("unchecked")
                int spinnerPositionTo = myAdap.getPosition(prevTo);

                toSpinner.setSelection(spinnerPositionFrom);
                fromSpinner.setSelection(spinnerPositionTo);

            }

        });

    }

    private void setSpinnerAdapter() {

        // adding the adapter to both spinners
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources()
                .getStringArray(R.array.currencies)
        );
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(dataAdapter);
        toSpinner.setAdapter(dataAdapter);

    }

    private void calculateCurrency() {

        toCurrency = (String) toSpinner.getSelectedItem();
        toCurrency = toCurrency.substring(0, Math.min(3, toCurrency.length()));

        fromCurrency = (String) fromSpinner.getSelectedItem();
        fromCurrency = fromCurrency.substring(0, Math.min(3, fromCurrency.length()));

        if (amountET.getText().toString().matches("")) {
            Crouton.makeText(this, "Amount cannot be empty..", Style.ALERT).show();
            amount = 0;
            return;
        } else if ((amountET.getText().toString().length() == 1)
                && (Character.toString(amountET.getText().toString().charAt(0)).equals("."))) {
            Crouton.makeText(this, "Invalid amount", Style.ALERT).show();
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
                runOnUiThread(new Runnable() {
                    public void run() {
                        Crouton.makeText(
                                CurrencyConverterActivity.this,
                                "An error occurred while fetching the latest currency rates",
                                Style.ALERT).show();
                    }
                });
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Crouton.makeText(
                                CurrencyConverterActivity.this,
                                "An error occurred while fetching the latest currency rates",
                                Style.ALERT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Crouton.makeText(
                                CurrencyConverterActivity.this,
                                "An error occurred while fetching the latest currency rates",
                                Style.ALERT).show();
                    }
                });
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // currency rate is fetched correctly, continue calculating
            continueCalculate();
        }

    }

    private void continueCalculate() {

        double result = amount * currencyRate;
        DecimalFormat df = new DecimalFormat("#.##");
        resultET.setText(df.format(result));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String currentDateandTime = sdf.format(new Date());
        lastUpdatedTV.setText("Last update: " + currentDateandTime);

        if (toCurrency.equals(fromCurrency))
            resultET.setText(Double.toString(amount));

    }

    private String getJSON(String URL) throws IOException {

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
    public boolean onCreateOptionsMenu(Menu menu) {
        SubMenu sub = menu.addSubMenu(getResources().getString(R.string.about));
        sub.setIcon(R.drawable.ic_action_action_about);
        sub.add(0, 1, 0, "About");
        sub.getItem().setShowAsAction(
                MenuItem.SHOW_AS_ACTION_ALWAYS
                        | MenuItem.SHOW_AS_ACTION_WITH_TEXT
        );
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

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this); // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this); // Add this method.
    }

    @Override
    protected void onDestroy() {
        adView.destroy();
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.resume();
    }

    @Override
    public void onPause() {
        adView.pause();
        super.onPause();
    }
}
