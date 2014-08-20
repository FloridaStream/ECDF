package cl.estadiocdf.EstadioCDF.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.auth.FacebookHandle;
import com.androidquery.auth.TwitterHandle;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Franklin Cruz on 20-03-14.
 */
public final class SocialUtil {

    AQuery aq;
    Context context;

    public SocialUtil(Context context) {
        this.context = context;
        aq = new AQuery(context);
    }

    public void fbshare(Activity activity, String text, String imageUrl, String title, final SocialUtilHandler handler) {

        Map<String, String> fbparams = new HashMap<String, String>();
        fbparams.put("message", text);
        fbparams.put("picture", imageUrl);
        fbparams.put("link", "www.estadiocdf.cl");
        fbparams.put("description", "Disfruta de los partidos del fútbol chileno en vivo y en VOD, además de lo mejor de la programación de CDF.");
        fbparams.put("name", title);
        fbparams.put("caption", "Estadio CDF");

        FacebookHandle handle = new FacebookHandle(activity, "669627823083035", "publish_stream, email");             //  ECDF
        //FacebookHandle handle = new FacebookHandle(activity, "238809549653178", "publish_stream, email");                //  Win Sports
        AQuery aq = new AQuery(activity);
        aq.auth(handle).ajax("https://graph.facebook.com/me/feed", fbparams, JSONObject.class, new AjaxCallback<JSONObject>(){

            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {
                Log.e("Object",object.toString());

                try{
                    if(status.getCode() == 200) {
                        handler.done(null);
                    }
                    else {
                        Log.e("Error","--> "+status.getMessage());
                        handler.done(new Exception(status.getMessage()));
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                    Log.e("Facebook", e.toString());
                    handler.done(new Exception(status.getMessage()));
                }
            }
        });

    }

    public void tweet(Activity activity, String tweet, final SocialUtilHandler handler) {

        if(tweet.length() > 140) {
            tweet = tweet.substring(0,140);
        }

        TwitterHandle handle = new TwitterHandle(activity, "WYGXn3E4f4uIvQwHeOQPeonjj",
                "kTKoObcIMhjfF6fRNUFBl3EoqryD8ObdOAxkgOXfL2iklJzZMy");

        String url = "https://api.twitter.com/1.1/statuses/update.json";

        Map<String,String> params = new HashMap<String, String>();
        params.put("status", tweet);

        aq.auth(handle).ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {
                try {
                    Log.d("Twitter", object.toString());

                    if(status.getCode() == 200) {
                        handler.done(null);
                    }
                    else {
                        handler.done(new Exception(status.getMessage()));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Twitter", e.toString());
                    handler.done(new Exception(status.getMessage()));
                }

            }
        });
    }
    public abstract static class SocialUtilHandler {

        public void done(Exception e) {

        }
    }

}
