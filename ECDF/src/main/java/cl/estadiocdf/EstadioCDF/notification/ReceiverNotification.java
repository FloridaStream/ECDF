package cl.estadiocdf.EstadioCDF.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Esteban- on 23-06-14.
 */
public class ReceiverNotification extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i= new Intent(context, ServiceNotification.class);
        //Log.e("OnReceive", "OnReceiver");
        context.startService(i);
    }



}
