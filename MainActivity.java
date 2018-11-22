package com.example.d18123347.lab10;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity implements View.OnClickListener
{
    private static final String DEBUG_TAG = "HttpExample";
    private EditText urlText;
    private TextView textView;
    private Button connectButton;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urlText       = (EditText) findViewById(R.id.myUrl);
        textView      = (TextView) findViewById(R.id.myText);
        connectButton = (Button) findViewById(R.id.button);
        connectButton.setOnClickListener(this);
    }

    public void onClick(View view)
    {


        // Get the URL from the UI's edit text field.
        String url = urlText.getText().toString();

        // get an instance of ConnectivityManager
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // get the active network info from the connectivity manager
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
        {
            // if network info returned and a network is available, run the asychronous processing
            DownloadWebpageTask DW = new DownloadWebpageTask();
            DW.execute(url);
        }
        else
        {
            // else, set text to say "no network connection available"
            url = "no network connection available";
        }
    }

    /* Use your own Asynchronous Task to create a task away from the main UI thread. This task takes a URL string and uses it to create an HttpUrlConnection. Once the connection has been established, the AsyncTask downloads the contents of the webpage as an InputStream. Finally, the InputStream is converted into a string, which is displayed in the UI by the AsyncTask's onPostExecute method*/
    private class DownloadWebpageTask extends AsyncTask<String, Void, String>
    {
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try
            {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        protected void onPostExecute(String result)        {
            //json parsing would go here
            String jsonToStr = "";
            Log.v("JSONRESULT", result);
            System.out.println(result);
            JSONArray jsonArray = null;
            try {
                jsonArray =  new JSONArray(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for(int i = 0; i < jsonArray.length(); i++){
                try {
                    JSONObject e = jsonArray.getJSONObject(i);
                    String id = e.getString("id");
                    String title = e.getString("title");
                    String completed = e.getString("completed");
                    jsonToStr += "For ID " + id + "The title is " + title +
                            " And the status is " + completed + "\n\n";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            textView.setText(jsonToStr);
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.

    private String downloadUrl(String myurl) throws IOException
    {
        InputStream is = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        }
        finally
        {
            if (is != null) {
                is.close();
            }
        }
    }
    // Reads an InputStream and converts it to a String.
    // Reads an InputStream and converts it to a String.
    private String readIt(InputStream is)
    {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                sb.append(line).append('\n');
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

}