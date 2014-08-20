package cl.estadiocdf.EstadioCDF.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.auth.TwitterHandle;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.activities.VideoActivity;
import cl.estadiocdf.EstadioCDF.datamodel.LiveStream;
import cl.estadiocdf.EstadioCDF.datamodel.LiveStreamSchedule;
import cl.estadiocdf.EstadioCDF.delegates.DelegateTwitter;
import cl.estadiocdf.EstadioCDF.delegates.ImageChooserDelegate;
import cl.estadiocdf.EstadioCDF.delegates.VideoDelegate;
import cl.estadiocdf.EstadioCDF.dialogs.MessageDialog;
import cl.estadiocdf.EstadioCDF.dialogs.PostDialog;
import cl.estadiocdf.EstadioCDF.dialogs.ShareDialog;
import cl.estadiocdf.EstadioCDF.notification.ReceiverNotification;
import cl.estadiocdf.EstadioCDF.serializables.MediaSerializable;
import cl.estadiocdf.EstadioCDF.services.ServiceManager;
import cl.estadiocdf.EstadioCDF.utils.GlobalECDF;

/**
 * Created by Franklin Cruz on 17-02-14.
 */
public class LiveFragment extends Fragment {

    public static final String LIVE_LEFT_HEADER_URL_FORMATSTR = "https://estadiocdf.cl/img/headers/%s_left.jpg";
    public static final String LIVE_RIGHT_HEADER_URL_FORMATSTR = "https://estadiocdf.cl/img/headers/%s_right.jpg";
    public static final String LIVE_THUMBNAIL_VISIT_IMAGE_URL = "https://estadiocdf.cl/img/headers/%s_visita.jpg";
    public static final String LIVE_THUMBNAIL_LOCAL_IMAGE_URL = "https://estadiocdf.cl/img/headers/%s_local.jpg";

    public static final String LIVE_LOCAL_TEAM_URL_FORMATSTR = "https://estadiocdf.cl/img/headers/%s_local.jpg";
    public static final String LIVE_VISIT_TEAM_URL_FORMATSTR = "https://estadiocdf.cl/img/headers/%s_visita.jpg";

    private LinearLayout nextShowContainer;

    private List<LiveStreamSchedule> liveStreamSchedules = new ArrayList<LiveStreamSchedule>();
    LiveStreamSchedule nextShow;

    private View rootView;

    private View prevShow = null;
    private View prevShare = null;

    private static VideoDelegate videoDelegate;

    private int loadedSources = 0;

   // public Activity actividad;

    public void setVideoSelectedDelegate(VideoDelegate delegate) {
        this.videoDelegate = delegate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_live, container, false);
        Log.e("Activity 1",""+getActivity());
        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.show();
        progress.setContentView(R.layout.progress_dialog);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);

        ((GlobalECDF)getActivity().getApplication()).sendAnaliticsScreen("Pantalla Live");
        final Typeface extraBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/AkzidenzGrotesk-ExtraBoldCondItalic.otf");
        final Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        nextShowContainer = (LinearLayout) rootView.findViewById(R.id.container_next_shows);

        final View commingShowContainer = rootView.findViewById(R.id.comming_show_container);

        final ServiceManager serviceManager = new ServiceManager(getActivity());
        serviceManager.loadLiveStreamList(new ServiceManager.DataLoadedHandler<LiveStream>() {
            @Override
            public void loaded(List<LiveStream> data) {
                loadedSources = 0;
                final int totalSources = data.size();

                liveStreamSchedules.clear();
                for (int i = 0; i < data.size(); ++i) {
                    serviceManager.loadLiveStreamSchedule(data.get(i), new ServiceManager.DataLoadedHandler<LiveStreamSchedule>() {
                        @Override
                        public void loaded(List<LiveStreamSchedule> data) {

                            liveStreamSchedules.addAll(data);

                            Collections.sort(liveStreamSchedules, new Comparator<LiveStreamSchedule>() {
                                @Override
                                public int compare(LiveStreamSchedule lhs, LiveStreamSchedule rhs) {
                                    if (lhs.getStartDate().getTime() > rhs.getStartDate().getTime()) {
                                        return 1;
                                    } else if (lhs.getStartDate().getTime() < rhs.getStartDate().getTime()) {
                                        return -1;
                                    } else {
                                        return 0;
                                    }
                                }
                            });
                            ++loadedSources;
                            if (liveStreamSchedules.size() > 0 && loadedSources == totalSources) {
                                nextShow = liveStreamSchedules.get(0);
                                Date now = new Date();
                                if (liveStreamSchedules.size() > 1 &&
                                        now.getTime() > liveStreamSchedules.get(0).getStartDate().getTime() && now.getTime() < liveStreamSchedules.get(0).getEndDate().getTime() &&
                                        now.getTime() > liveStreamSchedules.get(1).getStartDate().getTime() && now.getTime() < liveStreamSchedules.get(1).getEndDate().getTime()) {
//                                if(true){
                                    displayLiveShow(liveStreamSchedules.get(0), rootView.findViewById(R.id.left_live_show_container), 1);
                                    displayLiveShow(liveStreamSchedules.get(1), rootView.findViewById(R.id.right_live_show_container), 2);

                                }else if(liveStreamSchedules.size() > 0){
                                    if (now.getTime() > liveStreamSchedules.get(0).getStartDate().getTime() && now.getTime() < liveStreamSchedules.get(0).getEndDate().getTime()) {
                                        displayLiveShow(liveStreamSchedules.get(0), rootView.findViewById(R.id.live_show_container), 0);

                                    } else {
                                        commingShowContainer.setVisibility(View.VISIBLE);
                                    }
                                    displayNextShow(liveStreamSchedules.get(0));
                                    Button shareCurrentButton = (Button) rootView.findViewById(R.id.button_share_live);
                                    shareCurrentButton.setTypeface(extraBold);
                                }
                                nextShowContainer.removeAllViews();
                                for (int i = 1; i < liveStreamSchedules.size(); ++i) {
                                    createLiveMediaCell(liveStreamSchedules.get(i));
                                }
                                //TimerClass timerClass = new TimerClass();
                                //timerClass.execute();
                            }
                            //TimerClass timerClass = new TimerClass();
                            //timerClass.execute();
                        }
                    });
                }
                progress.dismiss();
            }
        });

        Button remindButton = (Button) rootView.findViewById(R.id.live_remind_button);
        remindButton.setTypeface(lightCondensedItalic2);
        remindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GlobalECDF)getActivity().getApplication()).sendAnalitics("Click-Agendar");
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.button_discrete_bounce);
                v.startAnimation(animation);

                if (nextShow != null) {

                    SharedPreferences  prefs = getActivity().getSharedPreferences("recordatorio", Context.MODE_PRIVATE);
                    if(prefs.getBoolean("evento_e",false) && prefs.getBoolean("evento_n",false)){
                        createReminder(nextShow,false,false);
                        createNotifications(nextShow,false,true);
                    }
                    else if(prefs.getBoolean("evento_n",false)){
                        createNotifications(nextShow,true, false);
                        //Log.e("Notificación creada","Si");
                    }
                    else if(prefs.getBoolean("evento_e",false)){
                        createReminder(nextShow,true,true);
                    }
                    else if(!prefs.getBoolean("evento_e",true) && !prefs.getBoolean("evento_n",true)){
                        noSelectionMessage();
                    }
                }
            }
        });

        ImageView refresh = (ImageView) rootView.findViewById(R.id.refresh);
        refresh.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh(0);
            }
        });
        TextView nextShowsHeader = (TextView) rootView.findViewById(R.id.nextshows_label);
        nextShowsHeader.setTypeface(lightCondensedItalic2);

        return rootView;
    }

    private void displayLiveShow(final LiveStreamSchedule media, View liveShowContainer, int type) {

        Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        TextView timeLabel = (TextView)liveShowContainer.findViewById(R.id.time_label);
        timeLabel.setTypeface(lightCondensedItalic2);

        DateFormat df = new SimpleDateFormat("EEEE");

        df = new SimpleDateFormat("HH:mm' HRS'", Locale.ENGLISH);
        timeLabel.setText(df.format(media.getStartDate()));

        Button playButton = (Button)liveShowContainer.findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date now = new Date();
                if(now.getTime() > media.getStartDate().getTime() && now.getTime() < (media.getEndDate().getTime())) {
                    ((GlobalECDF)getActivity().getApplication()).sendAnalitics("Click-Play");
                    Intent intent = new Intent(getActivity(), VideoActivity.class);
                    MediaSerializable mediaSerializable = new MediaSerializable();
                    mediaSerializable.setLiveStreamSchedule(media);
                    intent.putExtra("media",mediaSerializable);
                    startActivity(intent);
                }else{
                    MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                    dialog.setTitle("ERROR");
                    dialog.setMessage("La transmisión ha terminado.");
                    dialog.show(getFragmentManager(), "dialog");
                }
            }
        });

        TextView liveLabel;
        final AQuery aq = new AQuery(rootView);
        final String[] splited = media.getCode().split("_vs_");
        if(type==0){
            liveLabel = (TextView)liveShowContainer.findViewById(R.id.live_label);
            liveLabel.setTypeface(lightCondensedItalic2);

            View shareLiveContainer = rootView.findViewById(R.id.share_live_container);
            shareLiveContainer.setVisibility(View.VISIBLE);
            liveShowContainer.setVisibility(View.VISIBLE);

        }else if(type==1){
            if (splited.length == 2) {
                aq.id(R.id.main_image_left_left).image(String.format(LIVE_LOCAL_TEAM_URL_FORMATSTR, splited[0]));
                aq.id(R.id.main_image_left_right).image(String.format(LIVE_VISIT_TEAM_URL_FORMATSTR, splited[1]));
            } else {
                aq.id(R.id.main_image_left_left).image(String.format(LIVE_LEFT_HEADER_URL_FORMATSTR, splited[0]));
                aq.id(R.id.main_image_left_right).image(String.format(LIVE_RIGHT_HEADER_URL_FORMATSTR, splited[0]));
            }

            View container = rootView.findViewById(R.id.two_live_show_container);
            container.setVisibility(View.VISIBLE);

            View textLeft = container.findViewById(R.id.text_left);
            liveLabel = (TextView)textLeft.findViewById(R.id.live_label);
            liveLabel.setTypeface(lightCondensedItalic2);

            View shareLeft = rootView.findViewById(R.id.share_live_container_left);
            Button shareLive = (Button)shareLeft.findViewById(R.id.button_share_live);
            shareLive.setTypeface(lightCondensedItalic2);

            TextView titleLabel = (TextView)rootView.findViewById(R.id.left_live_show_container).findViewById(R.id.title_label);
            titleLabel.setTypeface(lightCondensedItalic2);
            titleLabel.setText(media.getName().toUpperCase());

            shareLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShareDialog dialog = new ShareDialog(splited, aq, videoDelegate, getActivity(), media);
                    dialog.show(getFragmentManager(), "dialog");
                }
            });
        }else if(type==2){
            if (splited.length == 2) {
                aq.id(R.id.main_image_right_left).image(String.format(LIVE_LOCAL_TEAM_URL_FORMATSTR, splited[0]));
                aq.id(R.id.main_image_right_right).image(String.format(LIVE_VISIT_TEAM_URL_FORMATSTR, splited[1]));
            } else {
                aq.id(R.id.main_image_right_left).image(String.format(LIVE_LEFT_HEADER_URL_FORMATSTR, splited[0]));
                aq.id(R.id.main_image_right_right).image(String.format(LIVE_RIGHT_HEADER_URL_FORMATSTR, splited[0]));
            }

            rootView.findViewById(R.id.main_image_left).setVisibility(View.GONE);
            rootView.findViewById(R.id.main_image_right).setVisibility(View.GONE);

            rootView.findViewById(R.id.main_image_left_left).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.main_image_left_right).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.main_image_right_left).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.main_image_right_right).setVisibility(View.VISIBLE);

            View container = rootView.findViewById(R.id.two_live_show_container);
            container.setVisibility(View.VISIBLE);

            View textRight = container.findViewById(R.id.text_right);
            liveLabel = (TextView)textRight.findViewById(R.id.live_label);
            liveLabel.setTypeface(lightCondensedItalic2);

            View shareRight = rootView.findViewById(R.id.share_live_container_right);
            Button shareLive = (Button)shareRight.findViewById(R.id.button_share_live);
            shareLive.setTypeface(lightCondensedItalic2);

            TextView titleLabel = (TextView)rootView.findViewById(R.id.right_live_show_container).findViewById(R.id.title_label);
            titleLabel.setTypeface(lightCondensedItalic2);
            titleLabel.setText(media.getName().toUpperCase());

            shareRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShareDialog dialog = new ShareDialog(splited, aq, videoDelegate, getActivity(), media);
                    dialog.show(getFragmentManager(), "dialog");
                }
            });
        }
    }

    private void displayNextShow(final LiveStreamSchedule media) {

        Typeface extraBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/AkzidenzGrotesk-ExtraBoldCondItalic.otf");
        //Typeface lightCondensedItalic = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-Oblique.ttf");
        Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        TextView dayTextView = (TextView) rootView.findViewById(R.id.day_label);
        dayTextView.setTypeface(lightCondensedItalic2);
        TextView timeTextView = (TextView) rootView.findViewById(R.id.time_label);
        timeTextView.setTypeface(extraBold);
        TextView liveLabel = (TextView) rootView.findViewById(R.id.live_label);
        liveLabel.setTypeface(lightCondensedItalic2);

        DateFormat df = new SimpleDateFormat("EEEE");

        Calendar today = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(media.getStartDate());

        if (today.get(Calendar.DAY_OF_YEAR) == startDate.get(Calendar.DAY_OF_YEAR)) {
            dayTextView.setText("HOY");
        } else {
            dayTextView.setText(df.format(media.getStartDate()).toUpperCase());
        }

        df = new SimpleDateFormat("HH:mm' Hrs.'", Locale.ENGLISH);
        timeTextView.setText(df.format(media.getStartDate()));

        final AQuery aq = new AQuery(rootView);
        final String[] splited = media.getCode().split("_vs_");

        if (splited.length == 2) {
            aq.id(R.id.main_image_left).image(String.format(LIVE_LOCAL_TEAM_URL_FORMATSTR, splited[0]));
            aq.id(R.id.main_image_right).image(String.format(LIVE_VISIT_TEAM_URL_FORMATSTR, splited[1]));
        } else {
            aq.id(R.id.main_image_left).image(String.format(LIVE_LEFT_HEADER_URL_FORMATSTR, splited[0]));
            aq.id(R.id.main_image_right).image(String.format(LIVE_RIGHT_HEADER_URL_FORMATSTR, splited[0]));
        }

        TextView leftLabel = (TextView) rootView.findViewById(R.id.left_title_label);
        leftLabel.setTypeface(extraBold);
        TextView rightLabel = (TextView) rootView.findViewById(R.id.right_title_label);
        rightLabel.setTypeface(extraBold);

        String[] splitedTitle = media.getName().toLowerCase().split("v/s");

        if (splitedTitle.length == 2) {
            leftLabel.setText(splitedTitle[0].toUpperCase().trim());
            rightLabel.setText(splitedTitle[1].toUpperCase().trim());
        } else {
            leftLabel.setText(splitedTitle[0].toUpperCase().trim());
            rightLabel.setText("");
        }

        RelativeLayout shareLiveContainer = (RelativeLayout) rootView.findViewById(R.id.share_live_container);

        shareLiveContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareDialog dialog = new ShareDialog(splited, aq, videoDelegate, getActivity(), media);
                dialog.show(getFragmentManager(), "dialog");
            }
        });

        shareLiveContainer.bringToFront();
    }

    private void createLiveMediaCell(final LiveStreamSchedule media) {
        Typeface extraBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/AkzidenzGrotesk-ExtraBoldCondItalic.otf");
        Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        final RelativeLayout v = (RelativeLayout) getLayoutInflater(null).inflate(R.layout.live_show_cell, null);

        TextView title = (TextView) v.findViewById(R.id.title_label);
        title.setTypeface(lightCondensedItalic2);
        title.setText(media.getName().toUpperCase());

        TextView time = (TextView) v.findViewById(R.id.time_label);
        time.setTypeface(lightCondensedItalic2);

        String format = "dd MMM, HH:mm 'Hrs'";
        DateFormat df = new SimpleDateFormat(format, Locale.getDefault());


        time.setText(df.format(media.getStartDate()));


        TextView shareHeader = (TextView) v.findViewById(R.id.share_options_header_label);
        shareHeader.setTypeface(lightCondensedItalic2);

        final String[] splited = media.getCode().split("_vs_");

        final AQuery aq = new AQuery(v);

        if (splited.length > 1) {
            View imageFull = v.findViewById(R.id.image_full);
            imageFull.setVisibility(View.GONE);

            View splitView = v.findViewById(R.id.split_image_container);
            splitView.setVisibility(View.VISIBLE);

            TextView vsLabel = (TextView) v.findViewById(R.id.vs_label);
            vsLabel.setTypeface(extraBold);
            vsLabel.setVisibility(View.VISIBLE);

            View imageFullShare = v.findViewById(R.id.share_image_full);
            imageFullShare.setVisibility(View.GONE);

            View splitViewShare = v.findViewById(R.id.share_split_image_container);
            splitViewShare.setVisibility(View.VISIBLE);

            View vsLabelShare = v.findViewById(R.id.share_vs_label);
            vsLabelShare.setVisibility(View.VISIBLE);

            aq.id(R.id.image_left).image(String.format(LIVE_LOCAL_TEAM_URL_FORMATSTR, splited[0]));
            aq.id(R.id.image_right).image(String.format(LIVE_LOCAL_TEAM_URL_FORMATSTR, splited[1]));

            aq.id(R.id.share_image_left).image(String.format(LIVE_THUMBNAIL_LOCAL_IMAGE_URL, splited[0]));
            aq.id(R.id.share_image_right).image(String.format(LIVE_THUMBNAIL_VISIT_IMAGE_URL, splited[1]));
        } else {
            View imageFull = v.findViewById(R.id.image_full);
            imageFull.setVisibility(View.VISIBLE);

            View splitView = v.findViewById(R.id.split_image_container);
            splitView.setVisibility(View.GONE);

            TextView vsLabel = (TextView) v.findViewById(R.id.vs_label);
            vsLabel.setTypeface(extraBold);
            vsLabel.setVisibility(View.GONE);

            View imageFullShare = v.findViewById(R.id.share_image_full);
            imageFullShare.setVisibility(View.VISIBLE);

            View splitViewShare = v.findViewById(R.id.share_split_image_container);
            splitViewShare.setVisibility(View.GONE);

            View vsLabelShare = v.findViewById(R.id.share_vs_label);
            vsLabelShare.setVisibility(View.GONE);

            aq.id(R.id.image_full).image(String.format(LIVE_LEFT_HEADER_URL_FORMATSTR, splited[0]));
            aq.id(R.id.share_image_full).image(String.format(LIVE_LEFT_HEADER_URL_FORMATSTR, splited[0]));
        }

        final View show = v.findViewById(R.id.show_container);
        show.setSoundEffectsEnabled(false);
        final View share = v.findViewById(R.id.share_container);

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do nothing!!!
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideShare(share, show);
                prevShare = null;
                prevShow = null;

            }
        });

        ImageButton shareButton = (ImageButton) v.findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View sender) {

                if (prevShare != null && prevShow != null) {
                    hideShare(prevShare, prevShow);
                }

                displayShare(share, show);
                prevShare = share;
                prevShow = show;

            }
        });

        final SimpleDateFormat dia = new SimpleDateFormat("d");
        final SimpleDateFormat mes = new SimpleDateFormat("MMMM");
        final SimpleDateFormat año = new SimpleDateFormat("y");
        final SimpleDateFormat hora = new SimpleDateFormat("HH:mm");
        View facebookButton = v.findViewById(R.id.facebook_button);

        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GlobalECDF)getActivity().getApplication()).sendAnalitics("Facebook-Share");

                ServiceManager serviceManager = new ServiceManager(getActivity());
                serviceManager.getNameFacebook(getActivity(), new ServiceManager.DataLoadedHandler<String>(){
                    @Override
                    public void loaded(final String data) {
                        if (splited.length > 1) {
                            if (videoDelegate != null) {
                                videoDelegate.displayImageChooser(
                                        String.format(LIVE_THUMBNAIL_LOCAL_IMAGE_URL, splited[0]),
                                        String.format(LIVE_THUMBNAIL_VISIT_IMAGE_URL, splited[1]),
                                        new ImageChooserDelegate() {
                                            @Override
                                            public void onImageSelected(String url) {

                                                String text = String.format("Voy a ver  %s por Estadio CDF el día %s de %s del %s a las %s", media.getName(), dia.format(media.getStartDate()),
                                                        mes.format(media.getStartDate()), año.format(media.getStartDate()), hora.format(media.getStartDate()));

                                                PostDialog postDialog = new PostDialog(text, media.getName(), url, PostDialog.FACEBOOK_SHARE, data);
                                                postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                                            }
                                        });
                            }
                        } else {

                            String text = String.format("Voy a ver  %s por Estadio CDF el día %s de %s del %s a las %s", media.getName(),dia.format(media.getStartDate()),
                                    mes.format(media.getStartDate()), año.format(media.getStartDate()), hora.format(media.getStartDate()));

                            PostDialog postDialog = new PostDialog(text, media.getName(), String.format(LIVE_LEFT_HEADER_URL_FORMATSTR, splited[0]), PostDialog.FACEBOOK_SHARE, data);
                            postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                        }
                    }

                    @Override
                    public void error() {
                    }
                });

            }
        });

        View twitterButton = v.findViewById(R.id.twitter_button);
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GlobalECDF)getActivity().getApplication()).sendAnalitics("Twitter-Share");
                try{
                    if (splited.length > 1) {
                        String text = String.format("Voy a ver  %s por Estadio CDF el día %s de %s del %s a las %s", media.getName(),dia.format(media.getStartDate()),
                                mes.format(media.getStartDate()), año.format(media.getStartDate()), hora.format(media.getStartDate()));


                        PostDialog postDialog = new PostDialog(text, media.getName(),"", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    } else {

                        String text = String.format("Voy a ver  %s por Estadio CDF el día %s de %s del %s a las %s", media.getName(),dia.format(media.getStartDate()),
                                mes.format(media.getStartDate()), año.format(media.getStartDate()), hora.format(media.getStartDate()));

                        PostDialog postDialog = new PostDialog(text, media.getName(),"", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Twitter", e.toString());
                }

            }
        });

        View emailButton = v.findViewById(R.id.mail_button);
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GlobalECDF)getActivity().getApplication()).sendAnalitics("eMail-Share");
                if (splited.length > 1) {
                    if (videoDelegate != null) {
                        videoDelegate.displayImageChooser(
                                String.format(LIVE_THUMBNAIL_LOCAL_IMAGE_URL, splited[0]),
                                String.format(LIVE_THUMBNAIL_VISIT_IMAGE_URL, splited[1]),
                                new ImageChooserDelegate() {
                                    @Override
                                    public void onImageSelected(String url) {

                                        String text = String.format("Voy a ver  %s por Estadio CDF el día %s de %s del %s a las %s", media.getName(),dia.format(media.getStartDate()),
                                                mes.format(media.getStartDate()), año.format(media.getStartDate()), hora.format(media.getStartDate()));


                                        Intent i = new Intent(Intent.ACTION_SEND);
                                        i.setType("message/rfc822");

                                        i.putExtra(Intent.EXTRA_SUBJECT, "Estadio CDF");
                                        i.putExtra(Intent.EXTRA_TEXT, text);

                                        Bitmap image = aq.getCachedImage(url);

                                        File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                                        try {
                                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));
                                            if (image != null) {
                                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                                            }
                                            startActivity(Intent.createChooser(i, "Send mail..."));
                                        } catch (android.content.ActivityNotFoundException ex) {
                                            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                                            dialog.setTitle("");
                                            dialog.setMessage("No existen clientes de correo instalados.");
                                            dialog.show(getFragmentManager(), "dialog");
                                        } catch (Exception e) {
                                            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                                            dialog.setTitle("");
                                            dialog.setMessage("No existen clientes de correo instalados.");
                                            dialog.show(getFragmentManager(), "dialog");
                                        }
                                    }
                                });
                    }
                } else {
                    String text = String.format("Voy a ver  %s por Estadio CDF el día %s de %s del %s a las %s", media.getName(),dia.format(media.getStartDate()),
                            mes.format(media.getStartDate()), año.format(media.getStartDate()), hora.format(media.getStartDate()));


                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");

                    i.putExtra(Intent.EXTRA_SUBJECT, "Estadio CDF");
                    i.putExtra(Intent.EXTRA_TEXT, text);

                    Bitmap image = aq.getCachedImage(String.format(LIVE_LEFT_HEADER_URL_FORMATSTR, splited[0]));

                    File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                    try {
                        image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));
                        if (image != null) {
                            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                        }
                        startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                        dialog.setTitle("");
                        dialog.setMessage("No existen clientes de correo instalados.");
                        dialog.show(getFragmentManager(), "dialog");
                    } catch (Exception e) {
                        MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                        dialog.setTitle("");
                        dialog.setMessage("No existen clientes de correo instalados.");
                        dialog.show(getFragmentManager(), "dialog");
                    }
                }
            }
        });

        View clipboardButton = v.findViewById(R.id.clipboard_button);
        clipboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("CDF", "www.estadiocdf.cl"));
                ((GlobalECDF)getActivity().getApplication()).sendAnalitics("Copiar-Link");
                MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                dialog.setTitle("");
                dialog.setMessage("Enlace copiado al portapapeles.");
                dialog.show(getFragmentManager(), "dialog");
            }
        });

        ImageButton reminderButton = (ImageButton) v.findViewById(R.id.remind_button);
        reminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GlobalECDF)getActivity().getApplication()).sendAnalitics("Click-Agendar");
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.button_bounce);
                v.startAnimation(animation);

                SharedPreferences  prefs = getActivity().getSharedPreferences("recordatorio", Context.MODE_PRIVATE);
                if(prefs.getBoolean("evento_e",true) && prefs.getBoolean("evento_n",true)){
                    createReminder(media,false, false);
                    createNotifications(media,false,true);
                }
                else if(prefs.getBoolean("evento_n",false)){
                    createNotifications(media,true, false);
                    Log.e("Notificación creada","Si");
                }
                else if(prefs.getBoolean("evento_e",false)){
                    createReminder(media,true, true);
                    Log.e("Recordatorio creado","Si");
                }
                else if(!prefs.getBoolean("evento_e",true) && !prefs.getBoolean("evento_n",true)){
                    noSelectionMessage();
                }

            }
        });

        nextShowContainer.addView(v);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
        params.setMargins(5, 0, 5, 5);
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        params.width = 367;

        v.setClipChildren(false);
        v.setLayoutParams(params);

    }

    private void noSelectionMessage(){
        MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
        dialog.setTitle("");
        dialog.setMessage("Seleccione una opción para recordar dentro del menú");
        dialog.show(getFragmentManager(), "dialog");
    }

    private void displayShare(View share, View show) {
        ObjectAnimator rotationShow = ObjectAnimator.ofFloat(share, "y", share.getMeasuredHeight(), 0.0f);
        rotationShow.setDuration(500);

        show.setPivotY(0);
        show.setPivotX(show.getMeasuredWidth() / 2.0f);
        ObjectAnimator rotationShare = ObjectAnimator.ofFloat(show, "y", 0.0f, -show.getMeasuredHeight());
        rotationShare.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rotationShow, rotationShare);
        animatorSet.start();
    }

    private void hideShare(View share, View show) {
        ObjectAnimator rotationShow = ObjectAnimator.ofFloat(show, "y", -show.getMeasuredHeight(), 0.0f);
        rotationShow.setDuration(500);

        ObjectAnimator rotationShare = ObjectAnimator.ofFloat(share, "y", 0.0f, share.getMeasuredHeight());
        rotationShare.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rotationShow, rotationShare);
        animatorSet.start();
    }

    private void createNotifications(LiveStreamSchedule liveStreamSchedule, boolean showMessage , boolean showOtherMessage){

        SharedPreferences preferences = getActivity().getSharedPreferences("programa", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("name",liveStreamSchedule.getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        editor.putString("hora",dateFormat.format(liveStreamSchedule.getStartDate()));
        editor.commit();
        long time = liveStreamSchedule.getStartDate().getTime() - 120000;
        //long time = new Date().getTime() + 10000;
        Intent myIntent = new Intent(getActivity(), ReceiverNotification.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), (int) (time / 1000), myIntent, 0);

        //time = System.currentTimeMillis() + 10000;
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(getActivity().ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, time, pendingIntent);

        if(showMessage){
            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
            dialog.setTitle("");
            dialog.setMessage("Notificación creada.");
            dialog.show(getFragmentManager(), "dialog");
        }
        else if(showOtherMessage){
            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
            dialog.setTitle("");
            dialog.setMessage("Notificación y Recordatorio creados.");
            dialog.show(getFragmentManager(), "dialog");
        }


    }
    private void createReminder(LiveStreamSchedule liveStreamSchedule, boolean showMessage, boolean isNotification) {

        SharedPreferences prefs = getActivity().getSharedPreferences("cl.estadiocdf.EstadioCDF", Context.MODE_PRIVATE);

        if (prefs.getBoolean("reminder_" + liveStreamSchedule.getEventId(), false) && isNotification) {
            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
            dialog.setTitle("");
            dialog.setMessage("El recordatorio ya ha sido creado.");
            dialog.show(getFragmentManager(), "dialog");
            return;
        }

        int id_calendars[] = getCalendar(getActivity());

        if (id_calendars.length == 0) {
            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
            dialog.setTitle("");
            dialog.setMessage("Calendario no disponible.");
            dialog.show(getFragmentManager(), "dialog");
            return;
        }

        long calID = id_calendars[0];

        long startMillis = 0;

        long endMillis = 0;

        Calendar beginTime = Calendar.getInstance();
        beginTime.setTime(liveStreamSchedule.getStartDate());
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.setTime(liveStreamSchedule.getEndDate());
        endMillis = endTime.getTimeInMillis();

        TimeZone timeZone = TimeZone.getDefault();

        ContentResolver cr = getActivity().getContentResolver();
        ContentValues values = new ContentValues();

        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, liveStreamSchedule.getName());

        //values.put(CalendarContract.Events.DESCRIPTION, "");

        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.ALL_DAY, 0);


        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        long eventID = Long.parseLong(uri.getLastPathSegment());

        ContentValues reminderValues = new ContentValues();

        reminderValues.put(CalendarContract.Reminders.MINUTES, 3);
        reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventID);
        reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);

        cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);

        prefs.edit().putBoolean("reminder_" + liveStreamSchedule.getEventId(), true).commit();

        if(showMessage){
            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
            dialog.setTitle("");
            dialog.setMessage("Recordatorio creado.");
            dialog.show(getFragmentManager(), "dialog");
        }

    }

    public int[] getCalendar(Context c) {

        String projection[] = {"_id", "calendar_displayName"};

        Uri calendars = Uri.parse("content://com.android.calendar/calendars");

        ContentResolver contentResolver = c.getContentResolver();
        Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);

        int aux[] = new int[0];

        if (managedCursor.moveToFirst()) {

            aux = new int[managedCursor.getCount()];

            int cont = 0;
            do {
                aux[cont] = managedCursor.getInt(0);
                cont++;
            } while (managedCursor.moveToNext());

            managedCursor.close();
        }
        return aux;

    }

    public void refresh(int count){

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.show();
        progress.setContentView(R.layout.progress_dialog);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);

        /*if(count == 1){
            Toast.makeText(getActivity()," Primer Refresh Bitch",Toast.LENGTH_LONG).show();
        }
        else if(count == -1){
            Toast.makeText(getActivity()," Unico Refresh Bitch",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getActivity()," Segundo Refresh Bitch",Toast.LENGTH_LONG).show();
        }*/


        final Typeface extraBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/AkzidenzGrotesk-ExtraBoldCondItalic.otf");

        nextShowContainer = (LinearLayout) rootView.findViewById(R.id.container_next_shows);

        final View commingShowContainer = rootView.findViewById(R.id.comming_show_container);

        final ServiceManager serviceManager = new ServiceManager(getActivity());
        serviceManager.loadLiveStreamList(new ServiceManager.DataLoadedHandler<LiveStream>() {
            @Override
            public void loaded(List<LiveStream> data) {
                loadedSources = 0;
                final int totalSources = data.size();

                liveStreamSchedules.clear();
                for (int i = 0; i < data.size(); ++i) {
                    serviceManager.loadLiveStreamSchedule(data.get(i), new ServiceManager.DataLoadedHandler<LiveStreamSchedule>() {
                        @Override
                        public void loaded(List<LiveStreamSchedule> data) {

                            liveStreamSchedules.addAll(data);

                            Collections.sort(liveStreamSchedules, new Comparator<LiveStreamSchedule>() {
                                @Override
                                public int compare(LiveStreamSchedule lhs, LiveStreamSchedule rhs) {
                                    if (lhs.getStartDate().getTime() > rhs.getStartDate().getTime()) {
                                        return 1;
                                    } else if (lhs.getStartDate().getTime() < rhs.getStartDate().getTime()) {
                                        return -1;
                                    } else {
                                        return 0;
                                    }
                                }
                            });
                            ++loadedSources;
                            if (liveStreamSchedules.size() > 0 && loadedSources == totalSources) {
                                nextShow = liveStreamSchedules.get(0);
                                Date now = new Date();
                                if (liveStreamSchedules.size() > 1 &&
                                        now.getTime() > liveStreamSchedules.get(0).getStartDate().getTime() && now.getTime() < liveStreamSchedules.get(0).getEndDate().getTime() &&
                                        now.getTime() > liveStreamSchedules.get(1).getStartDate().getTime() && now.getTime() < liveStreamSchedules.get(1).getEndDate().getTime()) {
//                                if(true){
                                    displayLiveShow(liveStreamSchedules.get(0), rootView.findViewById(R.id.left_live_show_container), 1);
                                    displayLiveShow(liveStreamSchedules.get(1), rootView.findViewById(R.id.right_live_show_container), 2);

                                }else if(liveStreamSchedules.size() > 0){
                                    if (now.getTime() > liveStreamSchedules.get(0).getStartDate().getTime() && now.getTime() < liveStreamSchedules.get(0).getEndDate().getTime()) {
                                        displayLiveShow(liveStreamSchedules.get(0), rootView.findViewById(R.id.live_show_container), 0);

                                    } else {
                                        commingShowContainer.setVisibility(View.VISIBLE);
                                    }
                                    displayNextShow(liveStreamSchedules.get(0));
                                    Button shareCurrentButton = (Button) rootView.findViewById(R.id.button_share_live);
                                    shareCurrentButton.setTypeface(extraBold);
                                }
                                nextShowContainer.removeAllViews();
                                for (int i = 1; i < liveStreamSchedules.size(); ++i) {
                                    createLiveMediaCell(liveStreamSchedules.get(i));
                                }
                            }
                        }
                    });
                }
                progress.dismiss();
            }
        });
    }

    public class TimerClass extends AsyncTask<Void, Void, Void> {

        public final long MINUTO = 60000;
        private long inicio;
        private long finFirst ;
        private long finSeconds = -1;
        private boolean isRefresh = false;
        private boolean sameSchedule = true;

        private int contador = 1;

        //private Date test;

        @Override
        protected void onPreExecute() {
            inicio = System.currentTimeMillis();
            finFirst = (nextShow.getEndDate().getTime());
            //finFirst = inicio + MINUTO;
            if(liveStreamSchedules.size() > 1){
                finSeconds =  liveStreamSchedules.get(1).getStartDate().getTime();
                //finSeconds =  inicio + (MINUTO );

                if((finSeconds - finFirst) > MINUTO){
                    sameSchedule = false;
                }
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            if(finSeconds != -1){
                if(sameSchedule){

                    Log.e("Horario iguales","finPrograma 1 == inicioPrograma2");
                    contador = -1;
                    do {
                        inicio = System.currentTimeMillis();

                    }while(inicio < finFirst);
                }
                else{
                    Log.e("Horario Distintos","finPrograma 1 != inicioPrograma2");
                    do {
                        inicio = System.currentTimeMillis();
                        if(!isRefresh && inicio > finFirst){
                            isRefresh = true;
                            publishProgress();

                        }
                    }while(inicio < finSeconds);
                }
            }
            else{
                do {
                    inicio = System.currentTimeMillis();
                }while(inicio < finFirst);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            refresh(contador);
            contador++;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            refresh(contador);
            Log.e("post","post");
        }
    }

    public void updateStatus(String tweet) {
        AQuery aq1 = new AQuery(getActivity());
        Log.e("esteban","updateStatus");
        TwitterHandle handle = new TwitterHandle(getActivity(), "hLOEaa7w7tL0vJjESgrOcNOrS", "r1QKZ2PKpjflPD4QMLLPhLPELGlXLLjEtGs22l2Zk2rcEWTJb7");
        String url = "https://api.twitter.com/1.1/statuses/update.json";

        Map<String,String> params = new HashMap<String, String>();
        params.put("status", "dffffsd");
        aq1.auth(handle).ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {
                try {
                    Log.d("Twitter", object.toString());

                    if(status.getCode() == 200) {
                        //handler.done(null);
                    }
                    else {
                        //handler.done(new Exception(status.getMessage()));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Twitter", e.toString());
                }

            }
        });
    }
}
