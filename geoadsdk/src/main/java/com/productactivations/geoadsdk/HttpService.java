package com.productactivations.geoadsdk;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;



    public class HttpService extends AsyncTask<String, Integer, Object> {

        private boolean returnJson = true;

        public HttpService(boolean returnJson){

            this.returnJson = returnJson;
        }

        private Object doGet(String url) throws MalformedURLException, ProtocolException, IOException {

            String full_url = Config.url +url;
            EasyLogger.log("Full url  " + full_url);
            URL obj = new URL(full_url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("Key", "campusfi_passworded");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();

        }


        @Override
        protected Object doInBackground(String... strings){

            String result = "";

            //check if url is present; if not return
            if(strings.length <1){

                result = "{\"status\", \"failed\"}";
                return result;
            }

            String url = strings[0];

            Object response ;

            try {
                response = doGet(url);
            }
            catch(Exception es){

                response = "{\"status\":\"failed\", \"message\":\""+es.getMessage()+"\"}";
            }


            if(returnJson) {
                return resultToJson(response.toString());
            }
            else{

                return response.toString();
            }

        }

        public Object resultToJson(String result){

            Object json = null;
            try{

                EasyLogger.log("String from result is " + result);
                if(result.trim().charAt(0)=='['){

                    json = new JSONArray(result);
                }
                else {
                    json = new JSONObject(result);
                }
            }
            catch(JSONException es){

                EasyLogger.log(es.getMessage());
            }
            catch(Exception es){

                EasyLogger.log(es.getMessage());
            }

            return json;

        }

    }


