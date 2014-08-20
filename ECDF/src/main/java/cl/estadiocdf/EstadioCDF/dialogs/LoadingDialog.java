package cl.estadiocdf.EstadioCDF.dialogs;

import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cl.estadiocdf.EstadioCDF.R;

/**
 * Created by Esteban- on 20-08-14.
 */
public class LoadingDialog extends DialogFragment {

    public static final int LENGTH_SHORT = 3000;
    public static final int LENGTH_LONG = 4200;
    private long milliseconds = 0;

    private String title = "ERROR";
    private String message = "";

    public LoadingDialog(long milliseconds) {
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE, getTheme());
        this.milliseconds = milliseconds;
    }

    public LoadingDialog(long milliseconds, String title, String message) {
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE, getTheme());
        this.milliseconds = milliseconds;
        this.title = title;
        this.message = message;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        View rootView = inflater.inflate(R.layout.progress_bar_share, container, false);

        TextView titleLabel = (TextView)rootView.findViewById(R.id.logo_image_hhh);
        titleLabel.setTypeface(lightCondensedItalic2);
        titleLabel.setText(title);

        timerRemoveDialog(milliseconds);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));

        return rootView;
    }

    private void timerRemoveDialog(final long milliseconds) {
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                if(getDialog() != null ){
                    getDialog().dismiss();
                }
            }
        }, milliseconds);
    }

    public LoadingDialog setTitle(String title){
        this.title = title;
        return this;
    }

    public LoadingDialog setMessage(String message){
        this.message = message;
        return this;
    }
}
