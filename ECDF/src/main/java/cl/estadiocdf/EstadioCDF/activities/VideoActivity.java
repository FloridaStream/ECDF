package cl.estadiocdf.EstadioCDF.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.longtailvideo.jwplayer.JWPlayer;
import com.longtailvideo.jwplayer.JWPlayerView;

import java.util.Date;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.datamodel.LiveStreamSchedule;
import cl.estadiocdf.EstadioCDF.datamodel.Media;
import cl.estadiocdf.EstadioCDF.datamodel.Meta;
import cl.estadiocdf.EstadioCDF.datamodel.TokenIssue;
import cl.estadiocdf.EstadioCDF.delegates.VideoDelegate;
import cl.estadiocdf.EstadioCDF.dialogs.MessageDialog;
import cl.estadiocdf.EstadioCDF.serializables.MediaSerializable;
import cl.estadiocdf.EstadioCDF.services.ServiceManager;

public class VideoActivity extends Activity implements
        JWPlayer.OnFullscreenListener, JWPlayer.OnPlayListener, JWPlayer.OnBufferListener, JWPlayer.OnIdleListener,
        JWPlayer.OnQualityChangeListener, JWPlayer.OnQualityLevelsListener, JWPlayer.OnErrorListener{

    private static final String MEDIA_HLS_BASE_URL = "https://mdstrm.com/video/";
    private static final String MEDIA_LS_BASE_URL = "https://mdstrm.com/live-stream-playlist/";
    private static final int MAX_RESTORE_ATTEMPTS = 5;

    private JWPlayerView playerViewJW;
    private VideoView playerViewNative;

    private int restoreAttempts = 0;

    private Handler handler = null;
    private Runnable runnable = null;

    private ProgressDialog progress = null;
    private boolean isBack = false;

    private MediaSerializable mediaSerializable;
    private VideoDelegate videoDelegate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);

        restoreAttempts = 0;

        playerViewJW = (JWPlayerView)findViewById(R.id.jwplayerView);
        playerViewJW.setVisibility(View.VISIBLE);
        playerViewJW.setOnFullscreenListener(this);
        playerViewJW.setOnPlayListener(this);
        playerViewJW.setOnErrorListener(this);

        playerViewNative = (VideoView)findViewById(R.id.nativeVideoPlayer);


        videoDelegate = new VideoDelegate() {
            @Override
            public void onVideoSelected(Media media) {
                displayMedia(media);
            }

            @Override
            public void onVideoSelected(LiveStreamSchedule media) {
                displayMedia(media);
            }
        };

        Intent intent = getIntent();
        mediaSerializable = (MediaSerializable)intent.getSerializableExtra("media");

        if(mediaSerializable.getMedia() != null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                showProgress();
                Meta meta = mediaSerializable.getMedia().getMP4((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE));
                displayNative(meta,mediaSerializable.getMedia());
            }else{
                videoDelegate.onVideoSelected(mediaSerializable.getMedia());
            }
        }else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                showProgress();
                displayNative(mediaSerializable.getLiveStreamSchedule());
            }else{
                videoDelegate.onVideoSelected(mediaSerializable.getLiveStreamSchedule());
            }
        }
    }

    private void showProgress() {
        progress = new ProgressDialog(this);
        progress.show();
        progress.setContentView(R.layout.progress_dialog);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
    }

    private void displayNative(final Meta meta, final Media media){
        playerViewNative.setVisibility(View.VISIBLE);
        ServiceManager serviceManager = new ServiceManager(this);
        serviceManager.issueTokenForMedia(media.getMediaId(), new ServiceManager.DataLoadedHandler<TokenIssue>() {
            @Override
            public void loaded(TokenIssue data) {
                if (data.getStatus().equalsIgnoreCase("OK")) {
                    String url = String.format("%s%s.mp4?access_token=%s", MEDIA_HLS_BASE_URL, meta.getId(), data.getAccessToken());
                    player(url,true);
                }
            }
        });
    }

    private void displayNative(final LiveStreamSchedule live){
        playerViewNative.setVisibility(View.VISIBLE);
        ServiceManager serviceManager = new ServiceManager(this);
        serviceManager.issueTokenForLive(live.getStream().getLiveStreamId(), new ServiceManager.DataLoadedHandler<TokenIssue>() {
            @Override
            public void loaded(TokenIssue data) {
                if (data.getStatus().equalsIgnoreCase("OK")) {
                    String url = String.format("%s%s.m3u8?access_token=%s", MEDIA_LS_BASE_URL, live.getStream().getLiveStreamId(), data.getAccessToken());
                    player(url, false);
                }
            }
        });
    }
      
    private void player(String url, boolean seekBar){
        isBack = true;
        playerViewNative.setVideoPath(url);
        if(!seekBar){
            playerViewNative.setMediaController(new MediaController(this));
        }
        playerViewNative.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (progress != null) {
                    progress.dismiss();
                }
                playerViewNative.requestFocus();
                playerViewNative.start();
            }
        });
    }

    private void displayMedia(final Media media) {
        playerViewJW.setVisibility(View.VISIBLE);
        ServiceManager serviceManager = new ServiceManager(this);
        serviceManager.issueTokenForMedia(media.getMediaId(), new ServiceManager.DataLoadedHandler<TokenIssue>() {
            @Override
            public void loaded(TokenIssue data) {
                if (data.getStatus().equalsIgnoreCase("OK")) {
                    String url = String.format("%s%s.m3u8?access_token=%s", MEDIA_HLS_BASE_URL, media.getMediaId(), data.getAccessToken());
                    playerViewJW.setFullscreen(true);
                    playerViewJW.release();
                    isBack = true;

                    playerViewJW.setOnErrorListener(new JWPlayer.OnErrorListener() {
                        @Override
                        public void onError(String message) {
                            if(restoreAttempts < MAX_RESTORE_ATTEMPTS) {

                                Log.e("Numeros de intentos","--> "+restoreAttempts);
                                Log.e("MAXIMO de intentos","--> "+MAX_RESTORE_ATTEMPTS);
                                if(mediaSerializable.getMedia() != null){
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                                        showProgress();
                                        Meta meta = mediaSerializable.getMedia().getMP4((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE));
                                        displayNative(meta,mediaSerializable.getMedia());
                                    }else{
                                        videoDelegate.onVideoSelected(mediaSerializable.getMedia());
                                    }
                                }else{
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        showProgress();
                                        displayNative(mediaSerializable.getLiveStreamSchedule());
                                    }else{
                                        videoDelegate.onVideoSelected(mediaSerializable.getLiveStreamSchedule());
                                    }
                                }
                                restoreAttempts++;
                            }else {
                                Log.e("STREAM", "Max restore attempts for streaming reached!!! May God have mercy of our souls!!!!");
                                MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_LONG);
                                dialog.setTitle("ERROR");
                                dialog.setMessage("301 No se puede reproducir este video");
                                //dialog.show(getFragmentManager(),"");
                            }
                        }
                    });
                    playerViewJW.load(url);
                    playerViewJW.play();


                }
            }
        });
    }

    private void displayMedia(final LiveStreamSchedule live) {
        playerViewJW.setVisibility(View.VISIBLE);
        ServiceManager serviceManager = new ServiceManager(this);
        serviceManager.issueTokenForLive(live.getStream().getLiveStreamId(), new ServiceManager.DataLoadedHandler<TokenIssue>() {
            @Override
            public void loaded(TokenIssue data) {
                if (data.getStatus().equalsIgnoreCase("OK")) {
                    String url = String.format("%s%s.m3u8?access_token=%s", MEDIA_LS_BASE_URL, live.getStream().getLiveStreamId(), data.getAccessToken());
                    playerViewJW.setFullscreen(true);
                    playerViewJW.release();
                    isBack = true;
                    playerViewJW.load(url);
                    playerViewJW.play();

                    long time = (long) ((live.getEndDate().getTime() - live.getStartDate().getTime()) * 1.1f);
                    time = live.getStartDate().getTime() + time - new Date().getTime();
                    handler = new Handler();
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    };
                    handler.postDelayed(runnable, time);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(isBack){
            isBack = false;
            playerViewJW.stop();
            playerViewNative.stopPlayback();
            super.onBackPressed();
        }

    }

    @Override
    public void onBuffer() {

    }

    @Override
    public void onFullscreen(boolean state) {
        if(!state) {
            playerViewJW.stop();
            onBackPressed();
        }
    }

    @Override
    public void onIdle() {

    }

    @Override
    public void onPlay() {
        this.restoreAttempts = 0;
    }

    @Override
    public void onQualityChange(JWPlayer.QualityLevel currentQuality) {

    }

    @Override
    public void onQualityLevels(JWPlayer.QualityLevel[] levels) {

    }

    @Override
    public void onError(String message) {
        if(this.restoreAttempts < MAX_RESTORE_ATTEMPTS) {

            Log.e("Numeros de intentos","--> "+this.restoreAttempts);
            Log.e("MAXIMO de intentos","--> "+MAX_RESTORE_ATTEMPTS);
            if(mediaSerializable.getMedia() != null){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                    showProgress();
                    Meta meta = mediaSerializable.getMedia().getMP4((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE));
                    displayNative(meta,mediaSerializable.getMedia());
                }else{
                    videoDelegate.onVideoSelected(mediaSerializable.getMedia());
                }
            }else{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    showProgress();
                    displayNative(mediaSerializable.getLiveStreamSchedule());
                }else{
                    videoDelegate.onVideoSelected(mediaSerializable.getLiveStreamSchedule());
                }
            }
            this.restoreAttempts++;
        }else {
            Log.e("STREAM", "Max restore attempts for streaming reached!!! May God have mercy of our souls!!!!");
            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_LONG);
            dialog.setTitle("ERROR");
            dialog.setMessage("301 No se puede reproducir este video");
            //dialog.show(getFragmentManager(),"");
        }
    }
}
