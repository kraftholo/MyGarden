package com.example.android.mygarden.provider;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.android.mygarden.provider.PlantContract.PlantEntry;


public class PlantContentProvider extends ContentProvider {


    public static final int PLANTS = 100;
    public static final int PLANT_WITH_ID = 101;


    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String TAG = PlantContentProvider.class.getName();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(PlantContract.AUTHORITY, PlantContract.PATH_PLANTS, PLANTS);
        uriMatcher.addURI(PlantContract.AUTHORITY, PlantContract.PATH_PLANTS + "/#", PLANT_WITH_ID);
        return uriMatcher;
    }


    private PlantDbHelper mPlantDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mPlantDbHelper = new PlantDbHelper(context);
        return true;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mPlantDbHelper.getWritableDatabase();


        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned
        switch (match) {
            case PLANTS:

                long id = db.insert(PlantEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(PlantContract.PlantEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);


        return returnUri;
    }



    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {


        final SQLiteDatabase db = mPlantDbHelper.getReadableDatabase();


        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {

            case PLANTS:
                retCursor = db.query(PlantEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case PLANT_WITH_ID:
                String id = uri.getPathSegments().get(1);
                retCursor = db.query(PlantEntry.TABLE_NAME,
                        projection,
                        "_id=?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }


        retCursor.setNotificationUri(getContext().getContentResolver(), uri);


        return retCursor;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mPlantDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted plants
        int plantsDeleted; // starts as 0
        switch (match) {

            case PLANT_WITH_ID:
                // Get the plant ID from the URI path
                String id = uri.getPathSegments().get(1);

                plantsDeleted = db.delete(PlantEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (plantsDeleted != 0) {

            getContext().getContentResolver().notifyChange(uri, null);
        }

        return plantsDeleted;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        final SQLiteDatabase db = mPlantDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        int plantsUpdated;

        switch (match) {
            case PLANTS:
                plantsUpdated = db.update(PlantEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PLANT_WITH_ID:
                if (selection == null) selection = PlantEntry._ID + "=?";
                else selection += " AND " + PlantEntry._ID + "=?";
                // Get the place ID from the URI path
                String id = uri.getPathSegments().get(1);
                // Append any existing selection options to the ID filter
                if (selectionArgs == null) selectionArgs = new String[]{id};
                else {
                    ArrayList<String> selectionArgsList = new ArrayList<String>();
                    selectionArgsList.addAll(Arrays.asList(selectionArgs));
                    selectionArgsList.add(id);
                    selectionArgs = selectionArgsList.toArray(new String[selectionArgsList.size()]);
                }
                plantsUpdated = db.update(PlantEntry.TABLE_NAME, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }


        if (plantsUpdated != 0) {

            getContext().getContentResolver().notifyChange(uri, null);
        }

        return plantsUpdated;
    }


    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
