package com.example.android.mygarden;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.example.android.mygarden.PlantWateringService.EXTRA_PLANT_ID_KEY;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider {

    @TargetApi(JELLY_BEAN)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,long plantID,int imgRes,
                                int appWidgetId,boolean canBeWatered) {

        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        RemoteViews rv;

        if(width<300){
            rv = getSinglePlantRemoteViews(context,imgRes,plantID,canBeWatered);
        }else{
            rv = getGardenGridRemoteViews(context);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId,rv);
    }

    //setting the gridView for the widget
    private static RemoteViews getGardenGridRemoteViews(Context context){

        Intent intent = new Intent(context,GridWidgetService.class);
        RemoteViews views = new RemoteViews(context.getPackageName(),R.id.widget_gridView);

        // Set the GridWidgetService intent to act as the adapter for the GridView
        views.setRemoteAdapter(R.id.widget_gridView,intent);

        //setting a PendingIntentTemplate to all the the items in the gridview
        Intent appIntent = new Intent(context,PlantDetailActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,appIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_gridView,pendingIntent);

        //setting view if garden is empty
        views.setEmptyView(R.id.widget_gridView,R.id.widget_emptyLayout);

        return views;


    }


    //called whenever any widget option is changed
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        PlantWateringService.startActionUpdatePlantWidget(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    //2 methods to return remoteViews According to the width
    private static RemoteViews getSinglePlantRemoteViews(Context context, int imgRes, long plantID, boolean canBeWatered) {
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget_provider);

        Intent intent= null;
        if(plantID!= PlantContract.INVALID_PLANT_ID){
            //to open DetailsActivity directly on clicking of plant in widget
            intent = new Intent(context, PlantDetailActivity.class);
            intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plantID);
        }else{
            intent = new Intent(context, MainActivity.class);
        }


        Intent wateringIntent = new Intent(context, PlantWateringService.class);
        wateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANT);
        wateringIntent.putExtra(EXTRA_PLANT_ID_KEY,plantID);

        PendingIntent openActivityPI = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent wateringPI = PendingIntent.getService(
                context,
                0,
                wateringIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        if(canBeWatered){
            views.setViewVisibility(R.id.widget_water_button,VISIBLE);
        }else{
            views.setViewVisibility(R.id.widget_water_button,INVISIBLE);
        }

        views.setTextViewText(R.id.widget_textView,Long.toString(plantID));

        //sets the ImageView to show the imgRes passed in updation
        views.setImageViewResource(R.id.grass_IV,imgRes);

        views.setOnClickPendingIntent(R.id.widget_water_button, wateringPI);
        views.setOnClickPendingIntent(R.id.grass_IV,openActivityPI);

        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        PlantWateringService.startActionUpdatePlantWidget(context);
    }


    public static void updatePlantWidget(Context context,AppWidgetManager appWidgetManager
                                                    ,long plantID,int imgRes,int[] appWidgetIds,boolean canBeWatered){
        // There may be multiple widgets active, so update all of them
        for(int appWidgetId : appWidgetIds){
            updateAppWidget(context,appWidgetManager,plantID,imgRes,appWidgetId,canBeWatered);
        }

    }
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

