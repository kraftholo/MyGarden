package com.example.android.mygarden;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;


public class GridWidgetService extends RemoteViewsService{

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {


    private static final String TAG = GridRemoteViewsFactory.class.getSimpleName();

    Context mContext;
    Cursor mCursor;

    @Override
    public void onCreate() {

    }

    //My Constructor
    public GridRemoteViewsFactory(Context appContext) {
        this.mContext = appContext;
    }

    @Override
    public void onDataSetChanged() {

        Log.e(TAG, "onDataSetChanged: called after notify...was called");
        //get all data on creation or when forced to update via notifyAppWidgetViewDataChanged();
        Uri plantsUri = PlantContract.BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        Log.d(TAG, "onDataSetChanged: uri is "+plantsUri.toString());
        if(mCursor!=null)mCursor.close();

        mCursor = mContext.getContentResolver().query(plantsUri,null,null,null, PlantContract.PlantEntry.COLUMN_CREATION_TIME);

    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if(mCursor==null){
            Log.d(TAG, "getCount: cursor returned is null");
            return 0;
        }
        return mCursor.getCount();
    }

    //much like the onBindViewHolder();



    @Override
    public RemoteViews getViewAt(int position) {

        if(mCursor==null || mCursor.getCount()==0){
            Log.e(TAG, "getViewAt: cursor is null");
            return null;
        }

        mCursor.moveToPosition(position);

        int creationTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
        int lastWateredTimeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
        int plantTypeIndex = mCursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);
        int plantIDIndex = mCursor.getColumnIndex(PlantContract.PlantEntry._ID);

        long creationTime;
        long lastWateredTime;
        long plantID;
        int plantType;
        long timeNow = System.currentTimeMillis();

        creationTime = mCursor.getLong(creationTimeIndex);
        lastWateredTime=mCursor.getLong(lastWateredTimeIndex);
        plantType = mCursor.getInt(plantTypeIndex);
        plantID = mCursor.getLong(plantIDIndex);

        Log.d(TAG, "getViewAt: runs "+plantID);

        int imgRes = PlantUtils.getPlantImageRes(mContext,timeNow-creationTime,timeNow-lastWateredTime,plantType);

        RemoteViews views= new RemoteViews(mContext.getPackageName(),R.layout.plant_widget_provider);
        views.setViewVisibility(R.id.widget_water_button, View.GONE);
        views.setImageViewResource(R.id.grass_IV,imgRes);
        views.setTextViewText(R.id.widget_textView,Long.toString(plantID));

        //using fillInIntent to make use of the PendingIntentTemplate

        Bundle bundle = new Bundle();
        bundle.putLong(PlantWateringService.EXTRA_PLANT_ID_KEY,plantID);

        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(bundle);
        views.setOnClickFillInIntent(R.id.grass_IV,fillInIntent);


        return views;

    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }       //treat all items in the gridView same

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
