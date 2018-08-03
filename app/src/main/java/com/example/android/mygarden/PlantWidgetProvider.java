package com.example.android.mygarden;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.android.mygarden.ui.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,int imgRes,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget_provider);

        Intent intent = new Intent(context, MainActivity.class);

        Intent wateringIntent = new Intent(context, PlantWateringService.class);
        wateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANTS);

        PendingIntent openActivityPI = PendingIntent.getActivity(context,0,intent,0);

        PendingIntent wateringPI = PendingIntent.getService(
                context,
                0,
                wateringIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //sets the ImageView to show the imgRes passed in updation
        views.setImageViewResource(R.id.grass_IV,imgRes);

        views.setOnClickPendingIntent(R.id.widget_water_button, wateringPI);
        views.setOnClickPendingIntent(R.id.grass_IV,openActivityPI);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        PlantWateringService.startActionUpdatePlantWidget(context);
    }


    public static void updatePlantWidget(Context context,AppWidgetManager appWidgetManager,int imgRes,int[] appWidgetIds){
        // There may be multiple widgets active, so update all of them
        for(int appWidgetId : appWidgetIds){
            updateAppWidget(context,appWidgetManager,imgRes,appWidgetId);
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
