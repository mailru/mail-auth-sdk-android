package ru.mail.auth.sdk.api;

import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import androidx.annotation.NonNull;
import ru.mail.auth.sdk.call.CallException;
import ru.mail.auth.sdk.call.MethodCall;

public abstract class ApiCommand<R> implements MethodCall<R> {

    public static final String O2_API_HOST = "https://o2.mail.ru";
    private static final String TAG = "ApiCommand";
    private static final Charset sDefaultCharset = Charset.forName("UTF-8");

    @NonNull
    public final R execute() throws CallException {
        ApiQuery query = getQuery();
        ResponseProcessor<R> responseProcessor = getResponseProcessor();
        InputStream inputStream = null;
        try {
            inputStream = performRequest(query);
            String response = readResponse(inputStream);
            return responseProcessor.process(response);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Bad url", e);
            throw new CallException(CommonErrorCodes.MALFORMED_URL_ERROR, "Bad url");
        } catch (IOException e) {
            Log.e(TAG, "Connect exception", e);
            throw new CallException(CommonErrorCodes.NETWORK_ERROR, e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Stream close", e);
                }
            }
        }
    }

    private InputStream performRequest(ApiQuery query) throws IOException {
        URL u = new URL(buildUrl(query));
        Log.d(TAG, "Requesting url " + u.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
        urlConnection.setUseCaches(false);
        urlConnection.setConnectTimeout(20000);
        urlConnection.setReadTimeout(20000);
        boolean isPostRequest = !query.getPostArgs().isEmpty();
        urlConnection.setRequestMethod(isPostRequest ? "POST" : "GET");
        if (isPostRequest) {
            byte[] postData = getPostData(query);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Content-Length", String.valueOf(postData.length));
            urlConnection.getOutputStream().write(postData);
        }
        urlConnection.connect();
        return urlConnection.getInputStream();
    }

    private String buildUrl(ApiQuery query) {
        Uri.Builder builder = Uri.parse(query.getHost()).buildUpon();
        builder.path(query.getMethodName());
        if (!query.getGetArgs().isEmpty()) {
            for (Pair<String, String> getParam : query.getGetArgs()) {
                builder.appendQueryParameter(getParam.first, getParam.second);
            }
        }
        return builder.toString();
    }

    private String readResponse(InputStream inputStream) throws IOException {
        byte[] buff = new byte[8192];
        ByteArrayOutputStream bos = new ByteArrayOutputStream(buff.length);
        BufferedInputStream bis = new BufferedInputStream(inputStream, buff.length);
        int read;
        while ((read = bis.read(buff)) > 0) {
            bos.write(buff, 0, read);
        }
        return new String(bos.toByteArray(), sDefaultCharset);
    }

    private byte[] getPostData(ApiQuery apiQuery) {
        try {
            StringBuilder postData = new StringBuilder();
            for (Pair<String, String> param : apiQuery.getPostArgs()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.first, sDefaultCharset.name()));
                postData.append('=');
                postData.append(URLEncoder.encode(param.second, sDefaultCharset.name()));
            }
            return postData.toString().getBytes(sDefaultCharset.name());
        } catch (UnsupportedEncodingException e) {
            return new byte[]{};
        }
    }

    protected abstract ApiQuery getQuery();

    protected abstract ResponseProcessor<R> getResponseProcessor();
}
