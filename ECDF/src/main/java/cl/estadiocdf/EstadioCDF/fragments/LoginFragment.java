package cl.estadiocdf.EstadioCDF.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;

import java.util.Date;
import java.util.regex.Pattern;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.activities.MainActivity;
import cl.estadiocdf.EstadioCDF.datamodel.User;
import cl.estadiocdf.EstadioCDF.dialogs.MessageDialog;
import cl.estadiocdf.EstadioCDF.services.ServiceManager;
import cl.estadiocdf.EstadioCDF.utils.CheckBoxCustom;


/**
 * Created by Franklin Cruz on 17-02-14.
 */
public class LoginFragment extends Fragment{

    private View rootView;
    private AQuery aq;
    private CheckBoxCustom checkBox;
    private Boolean rememberPass = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
        if(sharedPreferences.getString("username", null) != null && sharedPreferences.getString("password", null) != null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }

        rootView = inflater.inflate(R.layout.fragment_login, container, false);
        checkBox = (CheckBoxCustom) rootView.findViewById(R.id.check);
        checkBox.setEnabled(true);
        aq = new AQuery(rootView);

        Typeface extraBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/AkzidenzGrotesk-ExtraBoldCondItalic.otf");
        Typeface condensedItalic = Typeface.createFromAsset(getActivity().getAssets(), "fonts/AkzidenzGrotesk-CondItalic.otf");

        aq.id(R.id.login_banner).image("https://estadiocdf.cl/img/landing_ecdf_ipad.jpg#" + new Date().getTime(), false, false, 0, 0, new BitmapAjaxCallback(){
            @Override
            public void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status){

                aq.id(iv).image(bm, AQuery.RATIO_PRESERVE);
                ImageView splash = (ImageView)iv.getRootView().findViewById(R.id.splash);
                Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        getView().findViewById(R.id.splash).setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                splash.startAnimation(fadeOut);
            }
        });


        final EditText usernameTextbox = (EditText)rootView.findViewById(R.id.username_edittext);
        usernameTextbox.setTypeface(condensedItalic);

        final EditText passwordTextbox = (EditText)rootView.findViewById(R.id.password_edittext);
        passwordTextbox.setTypeface(condensedItalic);

        final TextView p = (TextView)rootView.findViewById(R.id.recuerda);
        p.setTypeface(condensedItalic);

        SharedPreferences preferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);


        InputFilter filter = new InputFilter(){

            @Override
            public CharSequence filter(CharSequence charSequence, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isSpaceChar(charSequence.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        if(preferences.getString("user", null) != null && preferences.getString("pass", null) != null) {
            usernameTextbox.setText(preferences.getString("user", null) );
            passwordTextbox.setText(preferences.getString("pass", null));
        }
        else{

        }

        usernameTextbox.setFilters(new InputFilter[]{filter});
        passwordTextbox.setFilters(new InputFilter[]{filter});

        Button loginButton = (Button)rootView.findViewById(R.id.loginButton);
        loginButton.setTypeface(extraBold);

        checkBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                rememberPass = b;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Pattern pMail = Pattern.compile("^[-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\\.[a-zA-Z]{2,4}$");
                Pattern pPass = Pattern.compile("^.+$");
                final String mail = usernameTextbox.getText().toString().trim();
                final String pass = passwordTextbox.getText().toString().trim();
                if(pMail.matcher(mail).matches() && pPass.matcher(pass).matches()) {
                    ServiceManager serviceManager = new ServiceManager(getActivity());
                    serviceManager.login(mail, pass, getActivity(), new ServiceManager.DataLoadedHandler<User>() {
                        @Override
                        public void loaded(User data) {

                            if(rememberPass){
                                SharedPreferences  prefs = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("user",mail);
                                editor.putString("pass",pass);
                                editor.commit();
                            }

                            if(data.isActive()){
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            }
                            else{
                                MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                                dialog.setTitle("ERROR");
                                dialog.setMessage("Su cuenta se encuentra desactivada");
                                dialog.show(getFragmentManager(), "dialog");
                            }
                        }

                        @Override
                        public void error() {
                            MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                            dialog.setTitle("ERROR");
                            dialog.setMessage("Usuario y/o contraseña incorrecto.");
                            dialog.show(getFragmentManager(), "dialog");
                        }
                    });
                }else {
                    MessageDialog dialog = new MessageDialog(MessageDialog.LENGTH_SHORT);
                    dialog.setTitle("ERROR");
                    dialog.setMessage("Usuario y/o contraseña incorrecto.");
                    dialog.show(getFragmentManager(), "dialog");
                }
            }
        });

        return rootView;
    }
}
