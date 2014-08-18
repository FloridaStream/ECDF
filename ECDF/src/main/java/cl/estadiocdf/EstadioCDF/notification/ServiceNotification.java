package cl.estadiocdf.EstadioCDF.notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.activities.MainActivity;
import cl.estadiocdf.EstadioCDF.dialogs.MessageDialog;

/**
 * Created by Esteban- on 23-06-14.
 */
public class ServiceNotification extends Service {

    //private AlarmNotification alarmNotification = new AlarmNotification();
    private NotificationManager nManager;
    private Activity activity;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //alarmNotification.SetAlarm(ServiceNotification.this);
        triggerNotification();
        this.stopSelf();
        return START_STICKY;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //Toast.makeText(this, "Servicio destruido", Toast.LENGTH_SHORT).show();
    }
    private void triggerNotification(){

        String mensaje;
        String nombre;

        try{
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("programa", Context.MODE_PRIVATE);
            mensaje = prefs.getString("name","Tu programa")+" comienza en dos minutos ";
            nombre = prefs.getString("name","Tu programa")+" está por comenzar ";
            Log.e("Nombre", "--> "+prefs.getString("name","Tu programdxga"));
        }
        catch (Exception e){
            e.printStackTrace();
            mensaje = "Tu programa comienza en dos minutos ";
            nombre =  "Tu programa está por comenzar";
        }

        nManager = (NotificationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(this.getApplicationContext(),MainActivity.class);


        Notification notification = new Notification(R.drawable.ic_launcher,nombre, System.currentTimeMillis());
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity( this.getApplicationContext(),0, intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.setLatestEventInfo(this.getApplicationContext(), "ECDF", mensaje, pendingNotificationIntent);

        Log.e("OnStart", "OnStart");
        nManager.notify(0, notification);
        getApplicationContext();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrar durante 1,3 segundo
        v.vibrate(1300);
    }

}
