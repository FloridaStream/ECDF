package cl.estadiocdf.EstadioCDF.dialogs;

import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import cl.estadiocdf.EstadioCDF.R;

/**
 * Created by Esteban- on 21-07-14.
 */
public class MessageDialogConfirm extends DialogFragment {

    public static final int LENGTH_SHORT = 2000;
    public static final int LENGTH_LONG = 4000;
    private long milliseconds = 0;

    private String title = "ERROR";
    private String message = "";

    public MessageDialogConfirm(long milliseconds) {
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE, getTheme());
        this.milliseconds = milliseconds;
    }

    public MessageDialogConfirm(long milliseconds, String title, String message) {
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE, getTheme());
        this.milliseconds = milliseconds;
        this.title = title;
        this.message = message;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        View rootView = inflater.inflate(R.layout.message_dialog_confirm, container, false);

        TextView titleLabel = (TextView)rootView.findViewById(R.id.title_label);
        Button regresar = (Button) rootView.findViewById(R.id.regresar);
        titleLabel.setTypeface(lightCondensedItalic2);
        titleLabel.setText(title);

        if(title=="" || title==null){
            titleLabel.setVisibility(View.GONE);
        }
        regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getDialog() != null ){
                    getDialog().dismiss();
                    //getActivity().onBackPressed();
                }
            }
        });

        TextView messageLabel = (TextView)rootView.findViewById(R.id.message_label);
        messageLabel.setTypeface(lightCondensedItalic2);
        messageLabel.setText(message);

        //timerRemoveDialog(milliseconds);

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

    public MessageDialogConfirm setTitle(String title){
        this.title = title;
        return this;
    }

    public MessageDialogConfirm setMessage(String message){
        this.message = message;
        return this;
    }
}
