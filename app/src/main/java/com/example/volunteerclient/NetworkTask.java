package com.example.volunteerclient;

import android.os.AsyncTask;
import android.util.Log;

import com.example.tools.NetworkHandler;

public class NetworkTask extends AsyncTask<String, Void, String> {
    private final String method;
    private final String jsonBody;
    public String Result;

    public NetworkTask(String method, String jsonBody) {
        this.method = method;
        this.jsonBody = jsonBody;
    }

//    @Override
//    protected String doInBackground(String... params) {
//        String url = params[0];
//        String result = "";
//        switch (method) {
//            case "GET":
//                result = NetworkHandler.get(url);
//                Log.d("NetworkHandler", "GET Response: " + result);
//                break;
//            case "POST":
//                result = NetworkHandler.post(url, jsonBody);
//                Log.d("NetworkHandler", "POST Response: " + result);
//                break;
//            // 可以根据需要添加其他HTTP方法的处理
//            default:
//                result = "Invalid method";
//                Log.d("NetworkHandler", "Error: " + result);
//                break;
//        }
//        Result = result;
//        return result;
//    }

    @Override
    protected String doInBackground(String... params) {
        String url = params[0];
        String result = "";
        switch (method) {
            case "GET":
                result = NetworkHandler.get(url);
                Log.d("NetworkHandler", "GET Response: " + result);
                break;
            case "POST":
                result = NetworkHandler.post(url, jsonBody);
                Log.d("NetworkHandler", "POST Response: " + result);
                break;
            // 可以根据需要添加其他HTTP方法的处理
            default:
                result = "Invalid method";
                Log.d("NetworkHandler", "Error: " + result);
                break;
        }
        Result = result;
        return result;
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d("NetworkHandler", "Final Response: " + result);
    }

    String getResult(){
        return Result;
    }

}