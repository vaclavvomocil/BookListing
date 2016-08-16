package com.example.android.booklisting;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String book_url = "https://www.googleapis.com/books/v1/volumes?q=";
    private String max_result = "&maxResults=20";
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private String bookSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button search = (Button) findViewById(R.id.search);
        View.OnClickListener searchListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable() == false) {
                    Toast.makeText(getApplicationContext(),"network not available",Toast.LENGTH_LONG).show();
                } else {
                    EditText bookToSearch = (EditText) findViewById(R.id.bootToSearch);
                    bookSearch = bookToSearch.getText().toString();
                    BookAsyncTask task = new BookAsyncTask();
                    task.execute();
                }
            }
        };

        search.setOnClickListener(searchListener);

    }


    private void updateUi(ArrayList<Book> bookList) {

        TextView help = (TextView) findViewById(R.id.help);
        help.setVisibility(View.GONE);

        ListView bookListView = (ListView) findViewById(R.id.bookList);
        BookAdapter adapter = new BookAdapter(this, bookList);
        bookListView.setAdapter(adapter);

    }


    private class BookAsyncTask extends AsyncTask<URL, Void, ArrayList<Book>> {

        @Override
        protected ArrayList<Book> doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(book_url, bookSearch);

            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }


            ArrayList<Book> bookList = extractFeatureFromJson(jsonResponse);

            return bookList;
        }


        @Override
        protected void onPostExecute(ArrayList<Book> bookList) {
            if (bookList == null) {
                return;
            }

            updateUi(bookList);
        }


        private URL createUrl(String stringUrl, String bookSearch) {
            URL url = null;
            try {
                String finalUrl = stringUrl + bookSearch + max_result;
                Log.v(LOG_TAG, finalUrl);
                url = new URL(finalUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }


        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            if (url == null) {
                return jsonResponse;
            }

            int httpResponse = 0;
            String stringHttpResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                httpResponse = urlConnection.getResponseCode();
                if (httpResponse == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "not 200" + httpResponse);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "problem", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }


        private ArrayList<Book> extractFeatureFromJson(String bookJSON) {
            // If JSON string is empty or null, then return early
            if (TextUtils.isEmpty(bookJSON)) {
                return null;
            }



            ArrayList<Book> books = new ArrayList<>();


            try {
                JSONObject baseJsonResponse = new JSONObject(bookJSON);
                JSONArray itemsArray = baseJsonResponse.getJSONArray("items");

                if (itemsArray.length() > 0) {

                    for (int i = 0; i < itemsArray.length(); i++) {

                        JSONObject book = itemsArray.getJSONObject(i);
                        JSONObject properties = book.getJSONObject("volumeInfo");

                        // Extract out the title, time, and tsunami values
                        String title = properties.getString("title");

                        JSONArray authorsArray = new JSONArray();
                        String author = "";
                        try {
                            authorsArray = properties.getJSONArray("authors");
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "no authors available", e);
                            author = "no author";
                        }


                        if (authorsArray.length() > 0) {
                            StringBuilder outputAuthor = new StringBuilder();
                            for (int j = 0; j < authorsArray.length(); j++) {
                                outputAuthor.append(authorsArray.getString(j));
                                outputAuthor.append(" ");
                            }
                            author = outputAuthor.toString();
                        }

                        Book bookTemp = new Book(author, title);
                        books.add(bookTemp);
                    }
                    return books;

                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the book list JSON results", e);
            }
            return null;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

}
