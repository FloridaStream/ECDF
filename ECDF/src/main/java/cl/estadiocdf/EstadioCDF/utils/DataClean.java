package cl.estadiocdf.EstadioCDF.utils;

import android.util.Log;

/**
 * Created by Esteban- on 18-08-14.
 */
public class DataClean {

    public static void garbageCollector(String TAG){

        Runtime garbage = Runtime.getRuntime();
        /*Log.e(TAG + " Garbage Collector", "Memoria Total: " + garbage.totalMemory());
        Log.e(TAG+" Garbage Collector", "Memoria Max: " + garbage.maxMemory());
        Log.e(TAG+" Garbage Collector", "Memoria libre antes de limpieza: " + garbage.freeMemory());*/
        garbage.gc();
       // Log.e(TAG+" Garbage Collector", "Memoria libre después s la limpieza:" + garbage.freeMemory());
    }
}
