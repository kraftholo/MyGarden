package com.example.android.mygarden;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;

public class PlantWateringService extends IntentService{


    public static final String ACTION_WATER_PLANT= "com.example.android.mygarden.action.water_plant";
    public static final String ACTION_UPDATE_PLANT_WIDGET = "com.example.android.mygarden.action.update_plant_widget";
    public static final String EXTRA_PLANT_ID_KEY = "plant-id-key";

    private static final String TAG = PlantWateringService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    public PlantWateringService(String name) {
        super(name);
    }

    public PlantWateringService() {
        super("PlantWateringService");
    }

    //A method to explicitly start the service
    public static void startActionUpdatePlantWidget(Context context){
        Intent intent = new Intent(context,PlantWateringService.class);
        intent.setAction(ACTION_UPDATE_PLANT_WIDGET);
        Log.e(TAG, "startActionUpdatePlantWidget: intentservice will now launch");
        context.startService(intent);
    }

    public static void startActionWaterPlant(Context context,long plantID){
        Intent intent = new Intent(context,PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANT);
        intent.putExtra(EXTRA_PLANT_ID_KEY,plantID);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();
            if(ACTION_WATER_PLANT.equals(action)){
                long plantID = intent.getLongExtra(EXTRA_PLANT_ID_KEY,0);
                handleActionWaterPlant(plantID);
            }else if(ACTION_UPDATE_PLANT_WIDGET.equals(action)){
                handleActionUpdatePlantWidget();
            }
        }
    }

    //query for the longest unwatered plant! Get its info, updateTheWidget
    private void handleActionUpdatePlantWidget() {

        Uri plantsUri = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        Cursor cursor = getContentResolver().query(plantsUri,
                                                        null,null,null,PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);

        int imgResToBePassed = 0;
        long plantIDToBePassed = 0;
        boolean canBeWatered = false;


        if(cursor!=null && cursor.moveToFirst()){
            cursor.moveToFirst();


            long creationTime;
            long lastWateredTime;
            int plantType;
            long timeNow = System.currentTimeMillis();

            plantIDToBePassed = cursor.getLong(cursor.getColumnIndex(PlantContract.PlantEntry._ID));
            creationTime = cursor.getLong(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME));
            lastWateredTime = cursor.getLong(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME));
            plantType = cursor.getInt(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE));

            imgResToBePassed =  PlantUtils.getPlantImageRes(this,timeNow-creationTime,timeNow-lastWateredTime,plantType);

            canBeWatered = timeNow-lastWateredTime >= PlantUtils.MIN_AGE_BETWEEN_WATER;


//            if(timeNow-lastWateredTime>=PlantUtils.MIN_AGE_BETWEEN_WATER){
//                canBeWatered=true;
//            }else{
//                canBeWatered=false;
//            }

            cursor.close();

        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,PlantWidgetProvider.class));

        //Trigger data update to handle the GridView and force a data refresh
        Log.e(TAG, "handleActionUpdatePlantWidget: notifyAppWidgetViewDataChanged called");
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widget_gridView);

        PlantWidgetProvider.updatePlantWidget(this,appWidgetManager,plantIDToBePassed,imgResToBePassed,appWidgetIds,canBeWatered);
        

    }

    //run updateQuery to change the lastWatered time to NOW for the plants that are still alive!
    private void handleActionWaterPlant(long plantID){
        Log.e(TAG, "handleActionWaterPlant: with id "+plantID);

        Uri plantUri = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).appendPath(Long.toString(plantID)).build();
        ContentValues cv = new ContentValues();
        long timeNow = System.currentTimeMillis();
        cv.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME,timeNow);

        getContentResolver().update(plantUri,cv,null,null);

        PlantWateringService.startActionUpdatePlantWidget(this);

    }
}
