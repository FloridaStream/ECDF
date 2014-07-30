package cl.estadiocdf.EstadioCDF.dialogs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.datamodel.LiveStreamSchedule;
import cl.estadiocdf.EstadioCDF.delegates.ImageChooserDelegate;
import cl.estadiocdf.EstadioCDF.delegates.VideoDelegate;
import cl.estadiocdf.EstadioCDF.fragments.LiveFragment;

/**
 * Created by Boris on 14-04-14.
 */
public class ShareDialog extends DialogFragment{

    private String[] splited;
    AQuery aq;
    VideoDelegate videoDelegate;
    FragmentActivity activity;
    LiveStreamSchedule media;

    public ShareDialog(String[] splited, AQuery aq, VideoDelegate videoDelegate, FragmentActivity activity, LiveStreamSchedule media) {
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE, getTheme());
        this.splited = splited;
        this.aq = aq;
        this.videoDelegate = videoDelegate;
        this.activity = activity;
        this.media = media;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        View rootView = inflater.inflate(R.layout.share_dialog, container, false);

        TextView shareLabel = (TextView)rootView.findViewById(R.id.share_label);
        shareLabel.setTypeface(lightCondensedItalic2);

        View facebookButton = rootView.findViewById(R.id.facebook_button_live);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().cancel();
                if (videoDelegate != null) {
                    if (splited.length == 2) {
                        videoDelegate.displayImageChooser(
                                String.format(LiveFragment.LIVE_THUMBNAIL_LOCAL_IMAGE_URL, splited[0]),
                                String.format(LiveFragment.LIVE_THUMBNAIL_VISIT_IMAGE_URL, splited[1]),
                                new ImageChooserDelegate() {
                                    @Override
                                    public void onImageSelected(String url) {
                                        String text = String.format("No me falta fútbol: Estoy viendo EN VIVO %s por Estadio CDF", media.getName());

                                        PostDialog postDialog = new PostDialog(text, media.getName(), url, PostDialog.FACEBOOK_SHARE);
                                        postDialog.show(activity.getSupportFragmentManager(), "dialog");
                                    }
                                });
                    }else{
                        videoDelegate.displayImageChooser(
                                String.format(LiveFragment.LIVE_LEFT_HEADER_URL_FORMATSTR, splited[0]),
                                String.format(LiveFragment.LIVE_RIGHT_HEADER_URL_FORMATSTR, splited[0]),
                                new ImageChooserDelegate() {
                                    @Override
                                    public void onImageSelected(String url) {
                                        String text = String.format("Estoy viendo EN VIVO %s por Estadio CDF", media.getName());

                                        PostDialog postDialog = new PostDialog(text, media.getName(), url, PostDialog.FACEBOOK_SHARE);
                                        postDialog.show(activity.getSupportFragmentManager(), "dialog");
                                    }
                                });
                    }
                }
            }
        });

        View twitterButton = rootView.findViewById(R.id.twitter_button_live);
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().cancel();
                if (splited.length == 2) {
                    String text = String.format("No me falta fútbol: Estoy viendo EN VIVO %s por Estadio CDF", media.getName());
                    PostDialog postDialog = new PostDialog(text, media.getName(), "", PostDialog.TWITTER_SHARE);
                    postDialog.show(getFragmentManager(), "dialog");
                }else{
                    String text = String.format("Estoy viendo EN VIVO %s por Estadio CDF", media.getName());
                    PostDialog postDialog = new PostDialog(text, media.getName(), "", PostDialog.TWITTER_SHARE);
                    postDialog.show(getFragmentManager(), "dialog");
                }
            }
        });

        View emailButton = rootView.findViewById(R.id.mail_button_live);

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
                if (videoDelegate != null) {
                    if (splited.length == 2) {
                        videoDelegate.displayImageChooser(
                                String.format(LiveFragment.LIVE_THUMBNAIL_LOCAL_IMAGE_URL, splited[0]),
                                String.format(LiveFragment.LIVE_THUMBNAIL_VISIT_IMAGE_URL, splited[1]),
                                new ImageChooserDelegate() {
                                    @Override
                                    public void onImageSelected(String url) {
                                        String text = String.format("No me falta fútbol: Estoy viendo EN VIVO %s por Estadio CDF", media.getName());

                                        Intent i = new Intent(Intent.ACTION_SEND);
                                        i.setType("message/rfc822");

                                        i.putExtra(Intent.EXTRA_SUBJECT, "Estadio CDF");
                                        i.putExtra(Intent.EXTRA_TEXT, text);

                                        Bitmap image = aq.getCachedImage(url);

                                        File cacheImage = new File(activity.getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                                        try {
                                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));
                                            if (image != null) {
                                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                                            }
                                            activity.startActivity(Intent.createChooser(i, "Send mail..."));
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
                    }else{
                        videoDelegate.displayImageChooser(
                                String.format(LiveFragment.LIVE_LEFT_HEADER_URL_FORMATSTR, splited[0]),
                                String.format(LiveFragment.LIVE_RIGHT_HEADER_URL_FORMATSTR, splited[0]),
                                new ImageChooserDelegate() {
                                    @Override
                                    public void onImageSelected(String url) {
                                        String text = String.format("Estoy viendo EN VIVO %s por Estadio CDF", media.getName());

                                        Intent i = new Intent(Intent.ACTION_SEND);
                                        i.setType("message/rfc822");

                                        i.putExtra(Intent.EXTRA_SUBJECT, "Estadio CDF");
                                        i.putExtra(Intent.EXTRA_TEXT, text);

                                        Bitmap image = aq.getCachedImage(url);

                                        File cacheImage = new File(activity.getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                                        try {
                                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));
                                            if (image != null) {
                                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                                            }
                                            activity.startActivity(Intent.createChooser(i, "Send mail..."));
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
                }
            }
        });

        View clipboardButton = rootView.findViewById(R.id.clipboard_button_live);
        clipboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("CDF", "www.estadiocdf.cl"));
                MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                dialog.setTitle("");
                dialog.setMessage("Enlace copiado al portapapeles.");
                dialog.show(getFragmentManager(), "dialog");
            }
        });

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));

        return rootView;
    }
}
