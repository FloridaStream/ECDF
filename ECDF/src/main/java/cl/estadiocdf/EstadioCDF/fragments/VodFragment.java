package cl.estadiocdf.EstadioCDF.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.activities.VideoActivity;
import cl.estadiocdf.EstadioCDF.datamodel.Media;
import cl.estadiocdf.EstadioCDF.datamodel.Thumbnail;
import cl.estadiocdf.EstadioCDF.dialogs.MessageDialog;
import cl.estadiocdf.EstadioCDF.dialogs.MessageDialogConfirm;
import cl.estadiocdf.EstadioCDF.dialogs.PostDialog;
import cl.estadiocdf.EstadioCDF.serializables.MediaSerializable;
import cl.estadiocdf.EstadioCDF.services.ServiceManager;

/**
 * Created by Franklin Cruz on 26-02-14.
 */
public class VodFragment extends Fragment {

    public final String VOD_CATEGORY_HIGHLIGHT      = "Destacados";
    public final String VOD_CATEGORY_LAST_PROGRAM   = "Programa";
    public final String VOD_CATEGORY_LAST_MATCHES   = "Partido";

    private LinearLayout highlightsContainer;
    private LinearLayout lastProgramsContainer;
    private LinearLayout lastMatchesContainer;

    private List<Media> lastMatchesList;
    private List<Media> lastShowsList;
    private List<Media> highliightsList;

    private View prevShow = null;
    private View prevShare = null;

    private ImageView refreshDestacado;
    private ImageView refreshLastPrograms;
    private ImageView refreshLastMatches;

    private boolean higlightsLoaded = false;
    private boolean matchesLoaded = false;
    private boolean showsLoaded = false;
    private boolean showMessageLimit = false;

    private ProgressDialog progress;

    private long inicio, fin, delta ;

    private void message(){
        MessageDialogConfirm dialog = new MessageDialogConfirm(MessageDialog.LENGTH_LONG);
        dialog.setTitle("ERROR");
        dialog.setMessage("Tiempo de espera excedido, Revisa tu conexión a internet");
        dialog.show(getFragmentManager(), "dialog");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inicio = System.currentTimeMillis();
        Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        View rootView = inflater.inflate(R.layout.fragment_vod, container, false);
        CheckTimer checkTimer = new CheckTimer();
        checkTimer.execute();

        TextView highlightTitleLabel = (TextView)rootView.findViewById(R.id.highligth_title_label);
        highlightTitleLabel.setTypeface(lightCondensedItalic2);
        TextView lastShowsTitleLabel = (TextView)rootView.findViewById(R.id.last_programs_title_label);
        lastShowsTitleLabel.setTypeface(lightCondensedItalic2);
        TextView lastMatchesTitleLabel = (TextView)rootView.findViewById(R.id.last_matches_title_label);
        lastMatchesTitleLabel.setTypeface(lightCondensedItalic2);

        TextView highlightTitleLabel2 = (TextView)rootView.findViewById(R.id.highligth_title_label_2);
        highlightTitleLabel2.setTypeface(lightCondensedItalic2);
        TextView lastShowsTitleLabel2 = (TextView)rootView.findViewById(R.id.last_programs_title_label_2);
        lastShowsTitleLabel2.setTypeface(lightCondensedItalic2);
        TextView lastMatchesTitleLabel2 = (TextView)rootView.findViewById(R.id.last_matches_title_label_2);
        lastMatchesTitleLabel2.setTypeface(lightCondensedItalic2);


        highlightsContainer = (LinearLayout)rootView.findViewById(R.id.highlights_container);
        lastProgramsContainer = (LinearLayout)rootView.findViewById(R.id.last_programs_container);
        lastMatchesContainer = (LinearLayout)rootView.findViewById(R.id.last_matches_container);

        refreshDestacado = (ImageView) rootView.findViewById(R.id.refresh_destacado);
        refreshLastMatches = (ImageView) rootView.findViewById(R.id.refresh_last_matches);
        refreshLastPrograms = (ImageView) rootView.findViewById(R.id.refresh_last_program);

        progress = new ProgressDialog(getActivity());
        progress.show();
        progress.setContentView(R.layout.progress_dialog);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);

        refreshDestacado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progress.show();
                progress.setContentView(R.layout.progress_dialog);
                progress.setCancelable(false);
                progress.setCanceledOnTouchOutside(false);

                ServiceManager serviceManager = new ServiceManager(getActivity());
                serviceManager.loadVODMedia( new String[] { VOD_CATEGORY_HIGHLIGHT }, new ServiceManager.DataLoadedHandler<Media>() {
                    @Override
                    public void loaded(List<Media> data) {
                        highliightsList = data;

                        displayHighlights();
                        progress.dismiss();

                    }
                });
            }
        });

        refreshLastPrograms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.show();
                progress.setContentView(R.layout.progress_dialog);
                progress.setCancelable(false);
                progress.setCanceledOnTouchOutside(false);

                ServiceManager serviceManager = new ServiceManager(getActivity());
                serviceManager.loadVODMedia( new String[] { VOD_CATEGORY_LAST_MATCHES }, new ServiceManager.DataLoadedHandler<Media>() {
                    @Override
                    public void loaded(List<Media> data) {
                        lastMatchesList = data;
                        displayLastPrograms();

                        progress.dismiss();

                    }
                });
            }
        });

        refreshLastMatches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.show();
                progress.setContentView(R.layout.progress_dialog);
                progress.setCancelable(false);
                progress.setCanceledOnTouchOutside(false);

                ServiceManager serviceManager = new ServiceManager(getActivity());
                serviceManager.loadVODMedia( new String[] { VOD_CATEGORY_LAST_PROGRAM }, new ServiceManager.DataLoadedHandler<Media>() {
                    @Override
                    public void loaded(List<Media> data) {
                        lastShowsList = data;

                        displayLastMatches();
                        progress.dismiss();

                    }
                });
            }
        });
        ServiceManager serviceManager = new ServiceManager(getActivity());


        serviceManager.loadVODMedia( new String[] { VOD_CATEGORY_LAST_MATCHES }, new ServiceManager.DataLoadedHandler<Media>() {
            @Override
            public void loaded(List<Media> data) {
                lastMatchesList = data;

                displayLastMatches();

                matchesLoaded = true;
                if(higlightsLoaded && matchesLoaded && showsLoaded) {
                    progress.dismiss();
                }
            }
        });
        serviceManager.loadVODMedia( new String[] { VOD_CATEGORY_HIGHLIGHT }, new ServiceManager.DataLoadedHandler<Media>() {

            @Override
            public void loaded(List<Media> data) {
                highliightsList = data;

                displayHighlights();

                higlightsLoaded = true;
                if(higlightsLoaded && matchesLoaded && showsLoaded) {
                    progress.dismiss();
                }
            }
        });
        serviceManager.loadVODMedia( new String[] { VOD_CATEGORY_LAST_PROGRAM }, new ServiceManager.DataLoadedHandler<Media>() {
            @Override
            public void loaded(List<Media> data) {
                lastShowsList = data;

                displayLastPrograms();

                showsLoaded = true;
                if(higlightsLoaded && matchesLoaded && showsLoaded) {
                    progress.dismiss();
                }
            }
        });

        return rootView;
    }

    private void displayHighlights() {

        Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        for (int i = 0; i < highliightsList.size(); ++i) {
            final Media m = highliightsList.get(i);
            View v = getActivity().getLayoutInflater().inflate(R.layout.highlight_cell, null);
            highlightsContainer.addView(v);

            TextView titleLabel = (TextView)v.findViewById(R.id.title_label);
            titleLabel.setTypeface(lightCondensedItalic2);
            titleLabel.setText(m.getTitle().replace(" - ", "\n"));

            TextView timeLabel = (TextView)v.findViewById(R.id.time_label);
            timeLabel.setTypeface(lightCondensedItalic2);
            timeLabel.setText(String.format("%d Min.", m.getDuration() / 60));

            TextView shareHeader = (TextView)v.findViewById(R.id.share_options_header_label);
            shareHeader.setTypeface(lightCondensedItalic2);

            ImageButton shareButton = (ImageButton)v.findViewById(R.id.share_button);

            final View show = v.findViewById(R.id.show_container);
            final View share = v.findViewById(R.id.share_container);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(prevShare != null && prevShow != null) {
                        hideShare(prevShare,prevShow);
                    }

                    displayShare(share,show);
                    prevShare = share;
                    prevShow = show;
                }
            });

            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideShare(share,show);
                    prevShare = null;
                    prevShow = null;
                }
            });

            show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), VideoActivity.class);
                    MediaSerializable mediaSerializable = new MediaSerializable();
                    mediaSerializable.setMedia(m);
                    intent.putExtra("media",mediaSerializable);
                    startActivity(intent);
                }
            });

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)v.getLayoutParams();
            params.setMargins(5,0,5,5);
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            params.width = 295;

            v.setLayoutParams(params);
            final AQuery aq = new AQuery(v);
            Thumbnail t = m.getDefaultThumbnail();
            final String thumbnailUrl;
            if (t != null) {
                thumbnailUrl = t.getUrl();
                View imageFullShare = v.findViewById(R.id.share_image_full);
                imageFullShare.setVisibility(View.VISIBLE);

                View splitViewShare = v.findViewById(R.id.share_split_image_container);
                splitViewShare.setVisibility(View.GONE);

                View vsLabelShare = v.findViewById(R.id.share_vs_label);
                vsLabelShare.setVisibility(View.GONE);

                aq.id(R.id.preview_image).image(t.getUrl());
                aq.id(R.id.share_image_full).image(t.getUrl());
            }
            else if(m.getThumbnails() != null && m.getThumbnails().size() > 0) {
                thumbnailUrl = m.getThumbnails().get(0).getUrl();
                View imageFullShare = v.findViewById(R.id.share_image_full);
                imageFullShare.setVisibility(View.VISIBLE);

                View splitViewShare = v.findViewById(R.id.share_split_image_container);
                splitViewShare.setVisibility(View.GONE);

                View vsLabelShare = v.findViewById(R.id.share_vs_label);
                vsLabelShare.setVisibility(View.GONE);

                aq.id(R.id.preview_image).image(m.getThumbnails().get(0).getUrl());
                aq.id(R.id.share_image_full).image(m.getThumbnails().get(0).getUrl());
            }
            else {
                thumbnailUrl = "";
            }

            View facebookButton = v.findViewById(R.id.facebook_button);

            facebookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Todo lo que me gusta del fútbol: Estoy viendo en VOD %s por Estadio CDF", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                    else {

                        String text = String.format("Todo lo que me gusta del fútbol: Estoy viendo en VOD %s por Estadio CDF", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                }
            });

            View twitterButton = v.findViewById(R.id.twitter_button);

            twitterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Todo lo que me gusta del fútbol: Estoy viendo en VOD %s por Estadio CDF", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), "", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                    else {

                        String text = String.format("Todo lo que me gusta del fútbol: Estoy viendo en VOD %s por Estadio CDF", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), "", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }

                }
            });


            View emailButton = v.findViewById(R.id.mail_button);

            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Todo lo que me gusta del fútbol: Estoy viendo en VOD %s por Estadio CDF", m.getTitle());

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");

                        i.putExtra(Intent.EXTRA_SUBJECT, "Estadio CDF");
                        i.putExtra(Intent.EXTRA_TEXT   , text);

                        Bitmap image = aq.getCachedImage(thumbnailUrl);

                        File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                        try {

                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                            if(image != null) {
                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                            }


                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                            dialog.setTitle("");
                            dialog.setMessage("No existen clientes de correo instalados.");
                            dialog.show(getFragmentManager(), "dialog");
                        } catch (Exception e) {

                        }
                    }
                    else {

                        String text = String.format("Todo lo que me gusta del fútbol: Estoy viendo en VOD %s por Estadio CDF", m.getTitle());

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");

                        i.putExtra(Intent.EXTRA_SUBJECT, "Estadio CDF");
                        i.putExtra(Intent.EXTRA_TEXT   , text);

                        Bitmap image = aq.image(thumbnailUrl).getCachedImage(thumbnailUrl);

                        File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                        try {

                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                            if(image != null) {
                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                            }


                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                            dialog.setTitle("");
                            dialog.setMessage("No existen clientes de correo instalados.");
                            dialog.show(getFragmentManager(), "dialog");
                        } catch (Exception e) {
                            e.printStackTrace();
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

                    MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                    dialog.setTitle("");
                    dialog.setMessage("Enlace copiado al portapapeles.");
                    dialog.show(getFragmentManager(), "dialog");
                }
            });

        }

    }

    private void displayLastPrograms() {
//        Typeface extraBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/AkzidenzGrotesk-ExtraBoldCondItalic.otf");
//        Typeface lightCondensedItalic = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-Oblique.ttf");
        Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        for (int i = 0; i < lastShowsList.size(); ++i) {
            final Media m = lastShowsList.get(i);

            View v = getActivity().getLayoutInflater().inflate(R.layout.last_programs_cell, null);
            lastProgramsContainer.addView(v);


            TextView titleLabel = (TextView)v.findViewById(R.id.title_label);
            titleLabel.setTypeface(lightCondensedItalic2);
            titleLabel.setText(m.getTitle().replace(" - ", "\n"));

            TextView timeLabel = (TextView)v.findViewById(R.id.time_label);
            timeLabel.setTypeface(lightCondensedItalic2);
            timeLabel.setText(String.format("%d Min.", m.getDuration() / 60));

            TextView shareHeader = (TextView)v.findViewById(R.id.share_options_header_label);
            shareHeader.setTypeface(lightCondensedItalic2);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)v.getLayoutParams();
            params.setMargins(5,0,5,5);
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            params.width = 400;

            v.setLayoutParams(params);

            ImageButton shareButton = (ImageButton)v.findViewById(R.id.share_button);

            final View show = v.findViewById(R.id.show_container);
            final View share = v.findViewById(R.id.share_container);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(prevShare != null && prevShow != null) {
                        hideShare(prevShare,prevShow);
                    }

                    displayShare(share,show);
                    prevShare = share;
                    prevShow = show;
                }
            });

            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideShare(share,show);
                    prevShare = null;
                    prevShow = null;
                }
            });


            show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), VideoActivity.class);
                    MediaSerializable mediaSerializable = new MediaSerializable();
                    mediaSerializable.setMedia(m);
                    intent.putExtra("media",mediaSerializable);
                    startActivity(intent);
                }
            });

            final AQuery aq = new AQuery(v);
            Thumbnail t = m.getDefaultThumbnail();
            final String thumbnailUrl;
            if (t != null) {

                thumbnailUrl = t.getUrl();

                View imageFullShare = v.findViewById(R.id.share_image_full);
                imageFullShare.setVisibility(View.VISIBLE);

                View splitViewShare = v.findViewById(R.id.share_split_image_container);
                splitViewShare.setVisibility(View.GONE);

                View vsLabelShare = v.findViewById(R.id.share_vs_label);
                vsLabelShare.setVisibility(View.GONE);

                aq.id(R.id.preview_image).image(t.getUrl());
                aq.id(R.id.share_image_full).image(t.getUrl());
            }
            else if(m.getThumbnails() != null && m.getThumbnails().size() > 0) {

                thumbnailUrl = m.getThumbnails().get(0).getUrl();

                View imageFullShare = v.findViewById(R.id.share_image_full);
                imageFullShare.setVisibility(View.VISIBLE);

                View splitViewShare = v.findViewById(R.id.share_split_image_container);
                splitViewShare.setVisibility(View.GONE);

                View vsLabelShare = v.findViewById(R.id.share_vs_label);
                vsLabelShare.setVisibility(View.GONE);

                aq.id(R.id.preview_image).image(m.getThumbnails().get(0).getUrl());
                aq.id(R.id.share_image_full).image(m.getThumbnails().get(0).getUrl());
            }
            else {
                thumbnailUrl = "";
            }

            View facebookButton = v.findViewById(R.id.facebook_button);

            facebookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Estadio CDF", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                    else {

                        String text = String.format("Estoy viendo EN VIVO %s por Estadio CDF", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                }
            });

            View twitterButton = v.findViewById(R.id.twitter_button);

            twitterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Estadio CDF", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), "", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                    else {

                        String text = String.format("Estoy viendo EN VIVO %s por Estadio CDF", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), "", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }

                }
            });


            View emailButton = v.findViewById(R.id.mail_button);

            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Estadio CDF", m.getTitle());

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");

                        i.putExtra(Intent.EXTRA_SUBJECT, "Estadio CDF");
                        i.putExtra(Intent.EXTRA_TEXT   , text);

                        Bitmap image = aq.getCachedImage(thumbnailUrl);

                        File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                        try {

                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                            if(image != null) {
                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                            }


                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                            dialog.setTitle("");
                            dialog.setMessage("No existen clientes de correo instalados.");
                            dialog.show(getFragmentManager(), "dialog");
                        } catch (Exception e) {

                        }
                    }
                    else {

                        String text = String.format("Estoy viendo EN VIVO %s por Estadio CDF", m.getTitle());

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");

                        i.putExtra(Intent.EXTRA_SUBJECT, "Estadio CDF");
                        i.putExtra(Intent.EXTRA_TEXT   , text);

                        Bitmap image = aq.getCachedImage(thumbnailUrl);

                        File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                        try {

                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                            if(image != null) {
                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                            }


                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                            dialog.setTitle("");
                            dialog.setMessage("No existen clientes de correo instalados.");
                            dialog.show(getFragmentManager(), "dialog");
                        } catch (Exception e) {

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

                    MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                    dialog.setTitle("");
                    dialog.setMessage("Enlace copiado al portapapeles.");
                    dialog.show(getFragmentManager(), "dialog");
                }
            });
        }

    }


    private void displayLastMatches() {
//        Typeface extraBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/AkzidenzGrotesk-ExtraBoldCondItalic.otf");
//        Typeface lightCondensedItalic = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-Oblique.ttf");
        Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        for (int i = 0; i < lastMatchesList.size(); ++i) {
            final Media m = lastMatchesList.get(i);

            View v = getActivity().getLayoutInflater().inflate(R.layout.highlight_cell, null);
            lastMatchesContainer.addView(v);

            TextView titleLabel = (TextView)v.findViewById(R.id.title_label);
            titleLabel.setTypeface(lightCondensedItalic2);
            titleLabel.setText(m.getTitle().replace(" - ", "\n"));

            TextView timeLabel = (TextView)v.findViewById(R.id.time_label);
            timeLabel.setTypeface(lightCondensedItalic2);
            timeLabel.setText(String.format("%d Min.", m.getDuration() / 60));

            TextView shareHeader = (TextView)v.findViewById(R.id.share_options_header_label);
            shareHeader.setTypeface(lightCondensedItalic2);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)v.getLayoutParams();
            params.setMargins(5,0,5,5);
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            params.width = 295;

            v.setLayoutParams(params);

            ImageButton shareButton = (ImageButton)v.findViewById(R.id.share_button);

            final View show = v.findViewById(R.id.show_container);
            final View share = v.findViewById(R.id.share_container);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(prevShare != null && prevShow != null) {
                        hideShare(prevShare,prevShow);
                    }

                    displayShare(share,show);
                    prevShare = share;
                    prevShow = show;
                }
            });

            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideShare(share,show);
                    prevShare = null;
                    prevShow = null;
                }
            });

            show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), VideoActivity.class);
                    MediaSerializable mediaSerializable = new MediaSerializable();
                    mediaSerializable.setMedia(m);
                    intent.putExtra("media",mediaSerializable);
                    startActivity(intent);
                }
            });

            final AQuery aq = new AQuery(v);
            final String thumbnailUrl;
            Thumbnail t = m.getDefaultThumbnail();
            if (t != null) {
                thumbnailUrl = t.getUrl();
                View imageFullShare = v.findViewById(R.id.share_image_full);
                imageFullShare.setVisibility(View.VISIBLE);

                View splitViewShare = v.findViewById(R.id.share_split_image_container);
                splitViewShare.setVisibility(View.GONE);

                View vsLabelShare = v.findViewById(R.id.share_vs_label);
                vsLabelShare.setVisibility(View.GONE);

                aq.id(R.id.preview_image).image(t.getUrl());
                aq.id(R.id.share_image_full).image(t.getUrl());
            }
            else if(m.getThumbnails() != null && m.getThumbnails().size() > 0) {
                thumbnailUrl = m.getThumbnails().get(0).getUrl();
                View imageFullShare = v.findViewById(R.id.share_image_full);
                imageFullShare.setVisibility(View.VISIBLE);

                View splitViewShare = v.findViewById(R.id.share_split_image_container);
                splitViewShare.setVisibility(View.GONE);

                View vsLabelShare = v.findViewById(R.id.share_vs_label);
                vsLabelShare.setVisibility(View.GONE);

                aq.id(R.id.preview_image).image(m.getThumbnails().get(0).getUrl());
                aq.id(R.id.share_image_full).image(m.getThumbnails().get(0).getUrl());
            }
            else {
                thumbnailUrl = "";
            }

            View facebookButton = v.findViewById(R.id.facebook_button);

            facebookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Estadio CDF", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                    else {

                        String text = String.format("Estoy viendo EN VIVO %s por Estadio CDF", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), thumbnailUrl, PostDialog.FACEBOOK_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                }
            });

            View twitterButton = v.findViewById(R.id.twitter_button);

            twitterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Estadio CDF", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), "", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }
                    else {

                        String text = String.format("Estoy viendo EN VIVO %s por Estadio CDF", m.getTitle());

                        PostDialog postDialog = new PostDialog(text, m.getTitle(), "", PostDialog.TWITTER_SHARE);
                        postDialog.show(getActivity().getSupportFragmentManager(), "dialog");
                    }

                }
            });


            View emailButton = v.findViewById(R.id.mail_button);

            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(m.belongsToCategoryByName("Partido")) {
                        String text = String.format("Me repito el plato: Estoy viendo en VOD %s por Estadio CDF", m.getTitle());

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");

                        i.putExtra(Intent.EXTRA_SUBJECT, "Estadio CDF");
                        i.putExtra(Intent.EXTRA_TEXT   , text);

                        Bitmap image = aq.getCachedImage(thumbnailUrl);

                        File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                        try {

                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                            if(image != null) {
                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                            }


                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                            dialog.setTitle("");
                            dialog.setMessage("No existen clientes de correo instalados.");
                            dialog.show(getFragmentManager(), "dialog");
                        } catch (Exception e) {

                        }
                    }
                    else {

                        String text = String.format("Estoy viendo EN VIVO %s por Estadio CDF", m.getTitle());

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");

                        i.putExtra(Intent.EXTRA_SUBJECT, "Estadio CDF");
                        i.putExtra(Intent.EXTRA_TEXT   , text);

                        Bitmap image = aq.getCachedImage(thumbnailUrl);

                        File cacheImage = new File(getActivity().getExternalCacheDir() + File.pathSeparator + UUID.randomUUID().toString() + ".png");

                        try {

                            image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheImage));

                            if(image != null) {
                                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheImage));
                            }


                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                            dialog.setTitle("");
                            dialog.setMessage("No existen clientes de correo instalados.");
                            dialog.show(getFragmentManager(), "dialog");
                        } catch (Exception e) {

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

                    MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                    dialog.setTitle("");
                    dialog.setMessage("Enlace copiado al portapapeles.");
                    dialog.show(getFragmentManager(), "dialog");
                }
            });
        }
    }

    private void displayShare(View share, View show) {
        ObjectAnimator rotationShow = ObjectAnimator.ofFloat(share, "y",share.getMeasuredHeight(), 0.0f);
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
        ObjectAnimator rotationShow = ObjectAnimator.ofFloat(show, "y",-show.getMeasuredHeight(), 0.0f);
        rotationShow.setDuration(500);

        ObjectAnimator rotationShare = ObjectAnimator.ofFloat(share, "y", 0.0f, share.getMeasuredHeight());
        rotationShare.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rotationShow, rotationShare);
        animatorSet.start();
    }

    private class CheckTimer extends AsyncTask<Void,Void,Void>{

        private final long LIMIT = 5000;

        @Override
        protected void onPreExecute() {
            inicio = System.currentTimeMillis();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            do{
                fin = System.currentTimeMillis();
                delta = fin - inicio;
            }
            while(delta < LIMIT);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if(!higlightsLoaded || !matchesLoaded || !showsLoaded){
                highlightsContainer.removeAllViews();
                lastMatchesContainer.removeAllViews();
                lastProgramsContainer.removeAllViews();
                progress.dismiss();
                message();
            }
        }

        @Override
        protected void onCancelled() {
            showMessageLimit = false;
        }
    }
}
