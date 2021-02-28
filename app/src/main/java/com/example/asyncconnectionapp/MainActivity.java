package com.example.asyncconnectionapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//Chamath Wanigasooriya D/BSE/19/0004
public class MainActivity extends AppCompatActivity {

    private TextInputEditText urlText;
    private TextView textView;
    private Button button;
    private ProgressBar progressBar;
    // Madura Dictionary Address
    private String domain = "www.maduraonline.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        button = findViewById(R.id.button);
        urlText = findViewById(R.id.urlText);
        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressBar);
        button.setOnClickListener(v -> clickHandler());

    }

    // Button Click Handler for the translate button
    private void clickHandler() {
        String searchTerm = urlText.getText().toString();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebPageTask().execute(searchTerm);
        } else {
            showError();
        }
    }


    // Display Alert to User
    private void showError() {
        new AlertDialog.Builder(this)
                .setTitle("Connection Failure")
                .setMessage("Please Connect to the Internet")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }
        //doInBackground method with searched word as argument
        @Override
        protected String doInBackground(String... searchItem) {
            try {
                return downloadUrl(searchItem[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may br invalid" + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textView.setText(null);

            // Extract result from table as JSON using Jsoup
            try {
                Document doc = Jsoup.parse(s);
                Element table = doc.select("table").get(0);
                Elements rows = table.select("tr");
                for (int i = 0; i < rows.size(); i++) { // If first row is the col names skip it.
                    Element row = rows.get(i);
                    Elements cols = row.select("td");

                    for (int j = 0; j < cols.size(); j++) {
                        textView.append(cols.get(j).text());
                        textView.append("\n");
                    }
                }
                progressBar.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                textView.setText(e.getMessage());
                progressBar.setVisibility(View.INVISIBLE);
            }
        }

        // HttpURLConnection to request and get response
        private String downloadUrl(String Item) throws IOException {
            InputStream is = null;
            try {
                URL url = new URL(buildUrl(domain, Item));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(1500);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                conn.connect();
                int response = conn.getResponseCode();
                Log.d("DownloadURL", "The Response is: " + response);
                is = conn.getInputStream();

                return readIt(is);

            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        // Return response using Buffer Reader instead of Reader
        // Remove length so that character limit is not restricted to 4000
        private String readIt(InputStream is) throws IOException {
            StringBuilder response = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        }

        // URL Query Appender to merge url parameters to the Uri
        private String buildUrl(String uri, String query) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(uri)
                    .appendQueryParameter("find", query);
            return builder.build().toString();
        }
    }
}

