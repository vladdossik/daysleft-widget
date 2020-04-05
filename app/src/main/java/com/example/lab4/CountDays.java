package com.example.lab4;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.widget.RemoteViews;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CountDays extends AppWidgetProvider {
    private static final int DAY_OF_MONTH = (24 * 60 * 60 * 1000);
    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context, CountDays.class);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction()!= null) {
            switch (intent.getAction()) {
                case "com.example.lab4.SCHEDULED_UPDATE":
                    AppWidgetManager manager = AppWidgetManager.getInstance(context);
                    int[] ids = manager.getAppWidgetIds(getComponentName(context));
                    onUpdate(context, manager, ids);
                    break;

                case "com.example.lab4.SCHEDULED_ALARM":
                    showNotification(context);
                    break;
            }
        }
        super.onReceive(context,intent);
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.days_left);
        views.setOnClickPendingIntent(R.id.contentFrame,pendingIntent);
        Calendar calendar = Calendar.getInstance();
        String widgetDate = MainActivity.loadDatePref(context, appWidgetId);
        long timeInMilliseconds = 0;
        if (!widgetDate.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date mDate = sdf.parse(widgetDate);
                timeInMilliseconds = mDate.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // Формула для нахождения разности между днями
            double diffInDays = (double) (timeInMilliseconds - calendar.getTimeInMillis()) / DAY_OF_MONTH;
            int daysLeftCeil = (int) Math.ceil(diffInDays);
            views.setTextViewText(R.id.counter, String.valueOf(Math.max(0, daysLeftCeil)));
            if (daysLeftCeil == 0) {
                scheduleAlarm(context, appWidgetId);
                views.setTextViewText(R.id.counter, " 0");
            } else if (diffInDays > 0) {
                if (diffInDays > 100) {
                    // Меняем шрифт в зависимоти от размера  числа
                    views.setTextViewTextSize(R.id.counter, TypedValue.COMPLEX_UNIT_SP, 65);
                } else if(diffInDays > 9 && diffInDays < 100) {
                    views.setTextViewTextSize(R.id.counter, TypedValue.COMPLEX_UNIT_SP, 75);
                }

            }
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    private static void scheduleAlarm(Context context, int appWidgetId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CountDays.class).setAction("com.example.lab4.SCHEDULED_ALARM");
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
        // Ровно в 9:00 будет показано уведомление
        long alarmTime = getTimeTillHour(9);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }
    private static long getTimeTillHour(int hour) {
        // Ровно в 9:00:00:00
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    // При достижении определенного времени показывается уведомление в шторке
    private static void showNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "alarm_ch_1";
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context,CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Событие сегодня")
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0))
                .setAutoCancel(true);
        mNotificationManager.notify(1, mBuilder.build());
    }
}

