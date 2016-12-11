package com.example.himalacharya.sunshineweather;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ForeCastFragment extends Fragment {

    private ArrayAdapter<String> foreCastAdapter;

    public ForeCastFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Added: to make fragment to handle menu events
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_refresh){
            FetchWeatherTask fetchWeatherTask=new FetchWeatherTask();
            fetchWeatherTask.execute("40507");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String[] forecastArray={
                "Today - Sunny - 88/63",
                "Tomorrow - Foggy - 70/40",
                "Weds - Cloudy - 72/63",
                "Thurs - Asteroids - 75/65",
                "Fri - Heavy Rain -65/56",
                "Sat - HELP TRAPPED IN WEATHERSTATION - 60/51",
                "Sun - Sunny - 80/68"
        };

        List<String> weekForeCast=new ArrayList<String>(Arrays.asList(forecastArray));


        foreCastAdapter=new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,weekForeCast);

        View rootView=inflater.inflate(R.layout.fragment_main,container,false);

        ListView listView= (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(foreCastAdapter);
        return rootView;

    }

    public class FetchWeatherTask extends AsyncTask<String,Void,Void> {

        private final String LOG_TAG=FetchWeatherTask.class.getSimpleName();
        @Override
        protected Void doInBackground(String... params) {

            //if there is no zip code, nothing to look up
            if (params.length==0){
                return null;
            }



            HttpURLConnection urlConnection=null;
            BufferedReader reader=null;

            //contain the raw JSON response as string
            String forecastJsonStr=null;

            String format="json";
            String units="metric";
            int numDays=7;

            try{
                //Construct the URL for OpenWeatherMap query

                //URL url=new URL("http://api.openweathermap.org/data/2.5/forecast/daily?&APPID=4783566288fb230b62215845131bb510");

                final String FORECAST_BASE_URL="http://api.openweathermap.org/data/2.5/forecast/daily?APPID=4783566288fb230b62215845131bb510";
                final String QUERY_PARAM="q";
                final String FORMAT_PARAM="mode";
                final String UNITS_PARAM="units";
                final String DAYS_PARAM="cnt";
                Uri builturi=Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM,format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                        .build();

                URL url=new URL(builturi.toString());

                Log.v(LOG_TAG,"Built URI "+builturi.toString());
                ////Create the request to OpenWeatherMap, and open the connection
                urlConnection= (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                //Read the input Stream into String
                InputStream inputStream=urlConnection.getInputStream();
                StringBuffer buffer=new StringBuffer();
                if (inputStream==null){
                    forecastJsonStr=null;
                    return null;

                }
                reader=new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line=reader.readLine())!=null){
                    buffer.append(line +"\n");
                }

                if (buffer.length()==0){
                    //Stream was empty . No meaning of parsing
                    forecastJsonStr=null;
                    return null;
                }

                forecastJsonStr=buffer.toString();

            }
            catch(IOException e){
                Log.e(LOG_TAG,"Error",e);
                // If the code didnt successfully get the weatehr data there is
                //no point of parsing data
                forecastJsonStr=null;
                return null;
            }finally {
                if (urlConnection!=null){
                    urlConnection.disconnect();
                }

                if (reader!=null){
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG,"Error closing stream",e);
                    }
                }
            }

        return null;

        }


    }

}
