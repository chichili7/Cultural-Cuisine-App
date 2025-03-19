package com.example.culturalcuisineapp;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class VerifyCredentials {
    private static final String dataUrl =
            "http://christopherhield-001-site4.htempurl.com/api/UserAccounts/VerifyUserCredentials";

    private static final String TAG = "CredentialsChecker";

    private final RequestQueue queue;
    private final String urlToUse;

    private final MainActivity mainActivity;

    public VerifyCredentials(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.queue = Volley.newRequestQueue(mainActivity);
        Uri.Builder buildURL = Uri.parse(dataUrl).buildUpon();
        urlToUse = buildURL.build().toString();
    }

    public void checkCredentials(String username, String password) {

        Response.Listener<JSONObject> listener = jsonObject ->
                mainActivity.handleVerifyUserCredentialsSuccess(
                        jsonObject.optString("userName"),
                        jsonObject.optString("firstName"),
                        jsonObject.optString("lastName"));

        Response.ErrorListener error = volleyError -> {

            if (volleyError.networkResponse != null) {
                String s = new String(volleyError.networkResponse.data);
                Log.d(TAG, "onErrorResponse: " + s);
            }
            Log.d(TAG, "checkCredentials: " + volleyError.getMessage());
            mainActivity.handleVerifyUserCredentialsFail();
        };

        JSONObject jsonParams;
        try {
            jsonParams = new JSONObject();
            jsonParams.put("userName", username);
            jsonParams.put("password", password);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(
                        Request.Method.PUT,
                        urlToUse,
                        jsonParams,
                        listener,
                        error);

        queue.add(jsonObjectRequest);
    }
}
