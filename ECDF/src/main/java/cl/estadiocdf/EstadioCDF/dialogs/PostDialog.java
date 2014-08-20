package cl.estadiocdf.EstadioCDF.dialogs;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.androidquery.AQuery;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.datamodel.Media;
import cl.estadiocdf.EstadioCDF.delegates.DelegateTwitter;
import cl.estadiocdf.EstadioCDF.fragments.LiveFragment;
import cl.estadiocdf.EstadioCDF.services.ServiceManager;
import cl.estadiocdf.EstadioCDF.utils.DataClean;
import cl.estadiocdf.EstadioCDF.utils.SocialUtil;

/**
 * Created by Franklin Cruz on 24-03-14.
 */
public class PostDialog extends DialogFragment {

    public DelegateTwitter delegate;
    public LiveFragment liveFragment;

    public static final int FACEBOOK_SHARE = 1;
    public static final int TWITTER_SHARE = 2;

    private int mode;

    private String text;
    private String imageUrl;
    private String title;
    private String nameUser = "";

    private Typeface normal;

    public PostDialog() {
        setStyle(DialogFragment.STYLE_NO_TITLE, getTheme());
    }

    public PostDialog(Media media, int mode) {

        this.mode = mode;
        setStyle(DialogFragment.STYLE_NO_TITLE, getTheme());
    }

    public PostDialog(String text, String title, String imageUrl, int mode) {

        this.mode = mode;
        this.text = text;
        this.imageUrl = imageUrl;
        this.title = title;

        setStyle(DialogFragment.STYLE_NO_TITLE, getTheme());
    }

    public PostDialog(String text, String title, String imageUrl, int mode, String nameUser) {

        this.mode = mode;
        this.text = text;
        this.imageUrl = imageUrl;
        this.title = title;
        this.nameUser = nameUser;

        setStyle(DialogFragment.STYLE_NO_TITLE, getTheme());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView;
        normal = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");
        if(mode == FACEBOOK_SHARE) {
            rootView = inflater.inflate(R.layout.facebook_compose, container, false);
            AQuery aq = new AQuery(rootView);
            aq.id(R.id.image_preview).image(this.imageUrl);

            TextView name = (TextView) rootView.findViewById(R.id.name_user);
            if(!nameUser.isEmpty())
                name.setText(nameUser);

        }
        else {
            rootView = inflater.inflate(R.layout.twitter_compose, container, false);
        }

        final EditText editText = (EditText)rootView.findViewById(R.id.post_edittext);
        editText.setText(this.text);
        Button tweetButton = (Button)rootView.findViewById(R.id.post_button);

        tweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(editText.getText() != null ) {
                    SocialUtil socialUtil = new SocialUtil(getActivity());

                    if(mode == FACEBOOK_SHARE) {
                        DataClean.garbageCollector("");

                        final ProgressDialog progress = new ProgressDialog(getActivity());
                        progress.show();
                        progress.setContentView(R.layout.progress_bar_share);
                        TextView text = (TextView) progress.findViewById(R.id.logo_image_hhh);
                        text.setTypeface(normal);
                        progress.setCancelable(false);
                        progress.setCanceledOnTouchOutside(false);

                        socialUtil.fbshare(getActivity(), editText.getText().toString(), imageUrl, title, new SocialUtil.SocialUtilHandler(){

                            @Override
                            public void done(Exception e) {
                                if(e == null){
                                    dismiss();
                                    progress.dismiss();
                                    MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_LONG,"Publicado","Su publicación fue realizada correctamente");
                                    dialog.show(getFragmentManager(),"");
                                }
                                else{
                                    dismiss();
                                    progress.dismiss();
                                    MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_LONG,"Error","No se pudo publicar");
                                    dialog.show(getFragmentManager(),"");
                                }
                            }
                        });
                    }
                    else {
                        DataClean.garbageCollector("");
                        socialUtil.tweet(getActivity(), editText.getText().toString(), new SocialUtil.SocialUtilHandler(){

                            @Override
                            public void done(Exception e) {
                                if(e == null){

                                    dismiss();
                                    MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_LONG,"Publicado","Su publicación fue realizada correctamente");
                                    dialog.show(getFragmentManager(),"");
                                }
                                else{
                                    dismiss();
                                    Log.e("Nombre","Exception --> "+e.getMessage());
                                    MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_LONG,"Error","No se pudo publicar");
                                    dialog.show(getFragmentManager(),"");
                                }
                            }
                        });
                    }
                }
            }
        });
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
        return rootView;
    }
}
