package cl.estadiocdf.EstadioCDF.services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import cl.estadiocdf.EstadioCDF.datamodel.DataMember;
import cl.estadiocdf.EstadioCDF.datamodel.DataModel;
import cl.estadiocdf.EstadioCDF.datamodel.Filter;
import cl.estadiocdf.EstadioCDF.datamodel.LiveStream;
import cl.estadiocdf.EstadioCDF.datamodel.LiveStreamSchedule;
import cl.estadiocdf.EstadioCDF.datamodel.Media;
import cl.estadiocdf.EstadioCDF.datamodel.TokenIssue;
import cl.estadiocdf.EstadioCDF.datamodel.User;

/**
 * Created by Franklin Cruz on 06-03-14.
 */
public class ServiceManager {

    public static final String TAG = "ServiceManager";

    public static final String BASE_URL = "https://api.streammanager.co/api/";
    public static final String API_TOKEN = "bace2022792e7943635001c8696a013f";
    public static final String UPDATE_SERVICE = "http://190.215.44.18/cdf/UpdateService.svc/NeedUpdate/";
    public static final String CURRENT_VERSION = "1";

    /*private String PERMISSIONS = "publish_stream, email";
    private String APP_ID = "669627823083035";
    public FacebookHandle handler;*/

    private Context context;
    private AQuery aq;

    public ServiceManager(Context context) {
        this.context = context;
        aq = new AQuery(context);
        //handler = new FacebookHandle((Activity) context, PERMISSIONS, APP_ID);
    }

    public void saveUserData(User user) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("cl.estadiocdf.EstadioCDF", Context.MODE_PRIVATE);
            prefs.edit().putString("userdata", encodeJsonObject(user).toString()).commit();
        }
        catch (Exception e) {

        }
    }

    public void getNameFacebook(Activity activity, final DataLoadedHandler<String> handler ) {

        FacebookHandle handle = new FacebookHandle(activity, "669627823083035", "publish_stream, email");
        aq.auth(handle).ajax("https://graph.facebook.com/me", JSONObject.class, new AjaxCallback<JSONObject>(){

            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {

                if(status.getCode() == 200) {
                    try {
                        String name = object.getString("name");
                        handler.loaded(name);
                    }
                    catch(Exception e ){
                        e.printStackTrace();
                    }
                }
                else {
                    handler.error("");
                }

            }
        });

    }
    public User loadUserData() {
        try {
            SharedPreferences prefs = context.getSharedPreferences("cl.estadiocdf.EstadioCDF", Context.MODE_PRIVATE);
            User u = parseJsonObject(new JSONObject(prefs.getString("userdata", "{}")), User.class);

            return u;
        }
        catch (Exception e) {
            return null;
        }
    }

    public void checkForUpdates(final DataLoadedHandler<Boolean> loadedHandler) {
        String url = String.format("%s%s", UPDATE_SERVICE, CURRENT_VERSION);
        aq.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                loadedHandler.loaded(object.equalsIgnoreCase("TRUE"));
            }
        });
    }

    public void login(final String username, final String password, final Activity activity, final DataLoadedHandler<User> loadedHandler) {
        String url = String.format("https://estadiocdf.cl/api/login?app_id=420a3c546cb7992e5357700d&username=%s&password=%s",username,password);
        aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {

                try {
                    if (!object.isNull("data") && object.getString("status").equals("OK")) {
                        SharedPreferences sharedPreferences = activity.getSharedPreferences("account", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username",username);
                        editor.putString("password",password);
                        editor.commit();

                        User user = parseJsonObject(object.getJSONObject("data"), User.class);
                        saveUserData(user);
                        loadedHandler.loaded(user);
                    }
                    else {
                        loadedHandler.error();
                    }
                }
                catch (Exception e) {
                    loadedHandler.error();
                }
            }
        });
    }

    public void loadFilters(final DataLoadedHandler<Filter> loadedHandler) {
        String url = "https://estadiocdf.cl/api/menu.json";
        aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {

                try{
                    if (!object.isNull("data") && object.getString("status").equals("OK")) {
                        JSONArray list = object.getJSONObject("data").getJSONArray("data");
                        List<Filter> filters = new ArrayList<Filter>();
                        for (int i = 0; i < list.length(); ++i) {
                            JSONObject rawFilter = list.getJSONObject(i);
                            filters.add(parseJsonObject(rawFilter,Filter.class));
                        }

                        loadedHandler.loaded(filters);
                    }
                }
                catch (Exception e) {

                }
            }
        });
    }

    public void loadLiveStreamList(final DataLoadedHandler<LiveStream> loadedHandler) {
        String url = String.format("%slive-stream?token=%s", BASE_URL, API_TOKEN);
        aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {
                try{
                    if (!object.isNull("data") && object.getString("status").equals("OK")) {
                        JSONArray list = object.getJSONArray("data");
                        List<LiveStream> streams = new ArrayList<LiveStream>();
                        for (int i = 0; i < list.length(); ++i) {
                            JSONObject raw = list.getJSONObject(i);
                            streams.add(parseJsonObject(raw, LiveStream.class));
                        }

                        loadedHandler.loaded(streams);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    public void loadLiveStreamSchedule(final LiveStream stream, final DataLoadedHandler<LiveStreamSchedule> loadedHandler) {

        String url = String.format("%slive-stream/%s/schedule?token=%s", BASE_URL, stream.getLiveStreamId(), API_TOKEN);
        aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {
                try{
                    if (!object.isNull("data") && object.getString("status").equals("OK")) {
                        JSONArray list = object.getJSONArray("data");
                        List<LiveStreamSchedule> schedule = new ArrayList<LiveStreamSchedule>();
                        for (int i = 0; i < list.length(); ++i) {
                            JSONObject raw = list.getJSONObject(i);
                            LiveStreamSchedule item = parseJsonObject(raw, LiveStreamSchedule.class);
                            item.setStream(stream);
                            schedule.add(item);
                        }

                        loadedHandler.loaded(schedule);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }
        });

    }

    public void loadVODMedia(String[] categories, final DataLoadedHandler<Media> loadedHandler) {
        String url = String.format("%smedia?token=%s&limit=50", BASE_URL, API_TOKEN);

        if(categories.length > 0) {
            url += "&category_name=";
            for(int i = 0; i < categories.length; ++i) {
                url += categories[i];

                if (i < categories.length - 1) {
                    url += ",";
                }
            }
        }

        aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {
                try {
                    if (!object.isNull("data") && object.getString("status").equals("OK")) {
                        JSONArray list = object.getJSONArray("data");
                        List<Media> media = new ArrayList<Media>();
                        for (int i = 0; i < list.length(); ++i) {
                            JSONObject raw = list.getJSONObject(i);
                            Media item = parseJsonObject(raw, Media.class);
                            media.add(item);
                        }

                        loadedHandler.loaded(media);
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    public void loadVODMediaByCategoryId(String[] categories, final DataLoadedHandler<Media> loadedHandler) {
        String url = String.format("%smedia?token=%s&limit=100", BASE_URL, API_TOKEN);

        if(categories.length > 0) {
            url += "&category_id=";
            for(int i = 0; i < categories.length; ++i) {
                url += categories[i];

                if (i < categories.length - 1) {
                    url += ",";
                }
            }
        }

        aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {
                try {
                    if (!object.isNull("data") && object.getString("status").equals("OK")) {
                        JSONArray list = object.getJSONArray("data");
                        List<Media> media = new ArrayList<Media>();
                        for (int i = 0; i < list.length(); ++i) {
                            JSONObject raw = list.getJSONObject(i);
                            Media item = parseJsonObject(raw, Media.class);
                            media.add(item);
                        }

                        loadedHandler.loaded(media);
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    public void issueTokenForMedia(String mediaId, final DataLoadedHandler<TokenIssue> handler) {
        String url = String.format("%saccess/issue?type=media&token=%s&id=%s", BASE_URL, API_TOKEN, mediaId);

        aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {

                try {
                    TokenIssue tokenIssue = parseJsonObject(object, TokenIssue.class);
                    handler.loaded(tokenIssue);
                }
                catch (Exception exception) {

                }
            }
        });
    }

    public void issueTokenForLive(String mediaId, final DataLoadedHandler<TokenIssue> handler) {
        String url = String.format("%saccess/issue?type=live&token=%s&id=%s", BASE_URL, API_TOKEN, mediaId);

        aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {

                try {
                    TokenIssue tokenIssue = parseJsonObject(object, TokenIssue.class);
                    handler.loaded(tokenIssue);
                }
                catch (Exception exception) {

                }
            }
        });
    }

    public static <T> JSONObject encodeJsonObject(T obj)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, JSONException {

        JSONObject result = new JSONObject();

        for (Method method : obj.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("get")) {
                String variableName = method.getAnnotation(DataMember.class)
                        .member();

                if(method.getReturnType() == Date.class) {
                    Date value = (Date)method.invoke(obj);

                    if(value != null) {
                        String format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
                        DateFormat df = new SimpleDateFormat(format, Locale.ENGLISH);
                        result.put(variableName, df.format(value));
                    }
                    else {
                        result.put(variableName, value);
                    }
                }
                else {
                    Object value = method.invoke(obj);
                    result.put(variableName, value);
                }
            }
        }

        return result;
    }


    public static <T> T parseJsonObject(JSONObject jsonObj, Class<T> type)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, JSONException,
            IOException,ParseException {

        T result = type.newInstance();

        for (Method method : type.getMethods()) {
            if (method.getName().startsWith("set")) {
                DataMember dataMember = method.getAnnotation(DataMember.class);

                if (dataMember == null) {
                    continue;
                }

                String variableName = dataMember.member();
                Object value = jsonObj.isNull(variableName) ? null : jsonObj
                        .get(variableName);

                if (value != null) {
                    @SuppressWarnings("rawtypes")
                    Class[] params = method.getParameterTypes();
                    if (params[0] == String.class) {
                        method.invoke(result, value.toString());
                    } else if(params[0] == Date.class) {
                        String format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
                        DateFormat df = new SimpleDateFormat(format, Locale.ENGLISH);
                        df.setTimeZone(TimeZone.getTimeZone("GMT"));
                        method.invoke(result,df.parse(value.toString()));
                    } else if(java.util.List.class.isAssignableFrom(params[0])){

                        Class<?> genericType = (Class<?>)((ParameterizedType)method.getGenericParameterTypes()[0]).getActualTypeArguments()[0];

                        if (DataModel.class.isAssignableFrom(genericType)) {
                            List untypedList = new ArrayList();
                            JSONArray array = (JSONArray)value;
                            for (int i = 0; i < array.length(); ++i) {
                                untypedList.add(parseJsonObject(array.getJSONObject(i), genericType));
                            }
                            method.invoke(result, untypedList);
                        }
                        else {
                            try {
                                List untypedList = new ArrayList();
                                JSONArray array = (JSONArray)value;
                                for (int i = 0; i < array.length(); ++i) {
                                    untypedList.add(array.get(i));
                                }
                                method.invoke(result, untypedList);
                            }
                            catch (Exception e) {
                                Log.d(TAG, "Attempt to parse a weird type from JSON: " + e.getMessage());
                            }
                        }

                    }else if(DataModel.class.isAssignableFrom(params[0])) {
                        method.invoke(result, parseJsonObject((JSONObject)value, params[0]));
                    }else {
                        method.invoke(result, value);
                    }
                }
            }
        }

        return result;
    }

    public static abstract class DataLoadedHandler<T> {

        public void loaded(T data) {

        }

        public void loaded(List<T> data) {

        }

        public void loaded(HashMap<String, List<T>> data) {

        }

        public void error(String error) {

        }

        public void error() {

        }
    }

}
