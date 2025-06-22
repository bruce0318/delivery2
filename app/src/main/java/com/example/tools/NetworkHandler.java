package com.example.tools;

//import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.net.MediaType;

import android.util.Log;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class NetworkHandler {
    /**
     * JSON format
     */
    public static final MediaType JSON = MediaType.get("application/json");

    public static final MediaType IMAGE = MediaType.parse("image/*");


    /**
     * Construct
     */
    private NetworkHandler(){}

    /**
     * Default OkHHTP Client
     */
    public static OkHttpClient GetOkHttpClient() {
        return GetOkHttpClient(60, 60, 60);
    }

    /**
     * Custom OkHTTP Client
     * @param connectTimeout
     * @param readTimeOut
     * @param writeTimeOut
     * @return
     */
    public static OkHttpClient GetOkHttpClient(int connectTimeout, int readTimeOut, int writeTimeOut) {
        OkHttpClient.Builder builder = new okhttp3.OkHttpClient().newBuilder();
        builder.connectTimeout(connectTimeout, TimeUnit.SECONDS);
        builder.readTimeout(readTimeOut, TimeUnit.SECONDS);
        builder.writeTimeout(writeTimeOut, TimeUnit.SECONDS);
        return builder.build();
    }

    public static String get(String url) {
        OkHttpClient client = GetOkHttpClient();
        Headers headers = new Headers.Builder().build();
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null)
                return response.body().string();
            else
                return "NetworkHandler::NullPointerException";
        }
        catch (IOException e){
            System.out.println("NetworkHandler::IOException: " + e.getMessage());
            return "IOException:" + e.getMessage();
        }
    }

    public static String get(String url, int connectTimeout, int readTimeOut, int writeTimeOut){
        OkHttpClient client = GetOkHttpClient(connectTimeout, readTimeOut, writeTimeOut);
        Headers headers = new Headers.Builder().build();
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null)
                return response.body().string();
            else
                return "NetworkHandler::NullPointerException";
        }
        catch (IOException e){
            System.out.println("NetworkHandler::IOException: " + e.getMessage());
            return "IOException:" + e.getMessage();
        }
    }

    public static String post(String url, String jsonBody) {
        OkHttpClient client = GetOkHttpClient();
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Headers headers = new Headers.Builder().build();

        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            else
                return "NetworkHandler::NullPointerException";
        }
        catch (IOException e){
            System.out.println("NetworkHandler::IOException: " + e.getMessage());
            return "IOException:" + e.getMessage();
        }
    }

    public static String post(String url, String jsonBody, int connectTimeout, int readTimeOut, int writeTimeOut){
        OkHttpClient client = GetOkHttpClient(connectTimeout, readTimeOut, writeTimeOut);
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Headers headers = new Headers.Builder().build();

        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            else
                return "NetworkHandler::NullPointerException";
        }
        catch (IOException e){
            System.out.println("NetworkHandler::IOException: " + e.getMessage());
            return "IOException:" + e.getMessage();
        }
    }

    public static String delete(String url, String jsonBody) {
        OkHttpClient client = GetOkHttpClient();
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Headers headers = new Headers.Builder().build();

        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .delete(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            else
                return "NetworkHandler::NullPointerException";
        }
        catch (IOException e){
            System.out.println("NetworkHandler::IOException: " + e.getMessage());
            return "IOException:" + e.getMessage();
        }
    }

    public static String put(String url, String jsonBody, int connectTimeout, int readTimeOut, int writeTimeOut) {
        OkHttpClient client = GetOkHttpClient(connectTimeout, readTimeOut, writeTimeOut);
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Headers headers = new Headers.Builder().build();

        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            else
                return "NetworkHandler::NullPointerException";
        }
        catch (IOException e){
            System.out.println("NetworkHandler::IOException: " + e.getMessage());
            return "IOException:" + e.getMessage();
        }
    }

    public static String put(String url, String jsonBody) {
        OkHttpClient client = GetOkHttpClient();
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Headers headers = new Headers.Builder().build();

        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            else
                return "NetworkHandler::NullPointerException";
        }
        catch (IOException e){
            System.out.println("NetworkHandler::IOException: " + e.getMessage());
            return "IOException:" + e.getMessage();
        }
    }

    public static String UploadImage(OkHttpClient client, String url, File img, String id){
        RequestBody imgRequest = RequestBody.create(img, IMAGE);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.ALTERNATIVE)
                .addFormDataPart("id", id)
                .addFormDataPart("image", "image.jpg", imgRequest)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            else
                return "NetworkHandler::NullPointerException";
        }
        catch (IOException e){
            Log.e("NetworkHandler::IOException", Objects.requireNonNull(e.getMessage()));
            return "IOException:" + e.getMessage();
        }
    }

    //请求超时处理
    public static String getWithTimeout(String url, int timeoutMillis) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

}
