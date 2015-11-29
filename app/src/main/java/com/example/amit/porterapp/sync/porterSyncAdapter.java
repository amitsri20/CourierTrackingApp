package com.example.amit.porterapp.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.amit.porterapp.R;
import com.example.amit.porterapp.data.ParcelsDBContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by amit on 7/23/2015.
 */
public class porterSyncAdapter extends AbstractThreadedSyncAdapter{
    public final String LOG_TAG = porterSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL =1000;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;




    public porterSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String JsonStr = null;

        try {

            final String BASE_URL =
                    "https://porter.0x10.info/api/parcel?type=json&query=list_parcel";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon().build();

            URL url = new URL(builtUri.toString());
//
//            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
           InputStream inputStream = urlConnection.getInputStream();
            //InputStream inputStream = getClass().getResourceAsStream("game_data.json");
            //FileInputStream inputStream = openFileInput("game_data.json");

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
            }
          reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.

            }
            JsonStr = buffer.toString();
            Log.v(LOG_TAG,JsonStr);
            getDataFromJson(JsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getDataFromJson(String JsonStr)
            throws JSONException {

        //Log.v(LOG_TAG,JsonStr);
        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String PARCEL_NAME = "name";
        final String PARCEL_IMAGE_URL= "image";
        final String PARCEL_DATE="date";
        final String PARCEL_TYPE="type";
        final String PARCEL_WEIGHT="weight";
        final String PARCEL_PHONE="phone";
        final String PARCEL_PRICE="price";
        final String PARCEL_QUANTITY="quantity";
        final String PARCEL_COLOR="color";
        final String PARCEL_LINK= "link";
        final String PARCEL_LOC_LAT= "latitude";
        final String PARCEL_LOC_LONG= "longitude";

        try {
            JSONObject parcelsJsonMainObject = new JSONObject(JsonStr);
            JSONArray parcelsJsonArray = parcelsJsonMainObject.getJSONArray("parcels");
            Vector<ContentValues> cVVector = new Vector<ContentValues>(parcelsJsonArray.length());
            for(int i=0;i<parcelsJsonArray.length();i++)
            {
                JSONObject parcelsJson = parcelsJsonArray.getJSONObject(i);
                String parcel_name = parcelsJson.getString(PARCEL_NAME);
                String parcel_imageurl = parcelsJson.getString(PARCEL_IMAGE_URL);
                String parcel_date = parcelsJson.getString(PARCEL_DATE);
                String parcel_type = parcelsJson.getString(PARCEL_TYPE);
                String parcel_weight = parcelsJson.getString(PARCEL_WEIGHT);
                String parcel_phone = parcelsJson.getString(PARCEL_PHONE);
                String parcel_price = parcelsJson.getString(PARCEL_PRICE);
                String parcel_quantity = parcelsJson.getString(PARCEL_QUANTITY);
                String parcel_color = parcelsJson.getString(PARCEL_COLOR);
                String parcel_link = parcelsJson.getString(PARCEL_LINK);
                JSONObject parcelLocation = parcelsJson.getJSONObject("live_location");
                String parcel_location_lat = parcelLocation.getString(PARCEL_LOC_LAT);
                String parcel_location_long = parcelLocation.getString(PARCEL_LOC_LONG);
                Log.v(LOG_TAG,parcel_name+parcel_date+parcel_phone+parcel_location_lat);



                ContentValues parcelsValues = new ContentValues();
                parcelsValues.put(ParcelsDBContract.ParcelsEntry.COLUMN_PARCEL_NAME, parcel_name);
                parcelsValues.put(ParcelsDBContract.ParcelsEntry.COLUMN_PARCEL_IMAGE_URL, parcel_imageurl);
                parcelsValues.put(ParcelsDBContract.ParcelsEntry.COLUMN_PARCEL_DATE, parcel_date);
                parcelsValues.put(ParcelsDBContract.ParcelsEntry.COLUMN_PARCEL_TYPE, parcel_type);
                parcelsValues.put(ParcelsDBContract.ParcelsEntry.COLUMN_PARCEL_WEIGHT, parcel_weight);
                parcelsValues.put(ParcelsDBContract.ParcelsEntry.COLUMN_PARCEL_PHONE, parcel_phone);
                parcelsValues.put(ParcelsDBContract.ParcelsEntry.COLUMN_PARCEL_PRICE, parcel_price);
                parcelsValues.put(ParcelsDBContract.ParcelsEntry.COLUMN_PARCEL_QUANTITY, parcel_quantity);
                parcelsValues.put(ParcelsDBContract.ParcelsEntry.COLUMN_PARCEL_COLOR, parcel_color);
                parcelsValues.put(ParcelsDBContract.ParcelsEntry.COLUMN_PARCEL_LINK, parcel_link);
                parcelsValues.put(ParcelsDBContract.ParcelsEntry.COLUMN_PARCEL_LOC_LAT, parcel_location_lat);
                parcelsValues.put(ParcelsDBContract.ParcelsEntry.COLUMN_PARCEL_LOC_LONG, parcel_location_long);

                cVVector.add(parcelsValues);
            }

            int inserted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(ParcelsDBContract.ParcelsEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");

        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }



    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        porterSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
