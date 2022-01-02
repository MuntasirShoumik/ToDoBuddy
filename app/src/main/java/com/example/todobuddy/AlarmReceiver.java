package com.example.todobuddy;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;


import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.todobuddy.database.TodoDatabase;
import com.example.todobuddy.entity.Todos;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {


    String des;

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onReceive(Context context, Intent intent) {


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String str = sdf.format(new Date());


        int hour = Integer.parseInt(str);
        int min = calendar.get(Calendar.MINUTE);
        int day = calendar.get(Calendar.DAY_OF_MONTH);




        class getTodosTask extends AsyncTask<Void,Void, List<Todos>>{

            @Override
            protected List<Todos> doInBackground(Void... voids) {
                return TodoDatabase.getTodoDatabase(context.getApplicationContext()).todoDau().getTitle(hour,min,day);
            }


            @Override
            protected void onPostExecute(List<Todos> todos) {

                super.onPostExecute(todos);
                if(todos.size() != 0){
                    des = todos.get(0).getTitle();

                    Intent i = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,0);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"showAlarm")
                            .setSmallIcon(R.drawable.ic_clock)
                            .setContentTitle("Reminder")
                            .setContentText(des)
                            .setAutoCancel(true)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                    notificationManagerCompat.notify(123,builder.build());

            }
                else {
                    Intent i = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,0);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"showAlarm")
                            .setSmallIcon(R.drawable.ic_clock)
                            .setContentTitle("Reminder")
                            .setContentText("Tap to open the app")
                            .setAutoCancel(true)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                    notificationManagerCompat.notify(123,builder.build());
                }





            }
        }
        new  getTodosTask().execute();




    }


}
