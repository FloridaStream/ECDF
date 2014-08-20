package cl.estadiocdf.EstadioCDF.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import com.androidquery.AQuery;
import com.androidquery.auth.TwitterHandle;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.datamodel.Filter;
import cl.estadiocdf.EstadioCDF.delegates.ImageChooserDelegate;
import cl.estadiocdf.EstadioCDF.delegates.SlideMenuDelegate;
import cl.estadiocdf.EstadioCDF.delegates.VideoDelegate;
import cl.estadiocdf.EstadioCDF.dialogs.ImagePickerDialog;
import cl.estadiocdf.EstadioCDF.fragments.FilteredFragment;
import cl.estadiocdf.EstadioCDF.fragments.HelpFragment;
import cl.estadiocdf.EstadioCDF.fragments.LiveFragment;
import cl.estadiocdf.EstadioCDF.fragments.SlideMenu;
import cl.estadiocdf.EstadioCDF.fragments.VodFragment;
import cl.estadiocdf.EstadioCDF.utils.GlobalECDF;

/**
 * Created by Franklin Cruz on 17-02-14.
 */
public class MainActivity extends ActionBarActivity {

    private SlideMenu slideMenu;
    private boolean isSlideMenuOpen = false;

    private View blockOverlay;

    private LiveFragment liveFragment;
    private VodFragment vodFragment;
    private FilteredFragment filteredFragment;

    private ImageButton menuButton;
    private ImageButton helpButton;
    private ImageButton liveButton;
    private ImageButton vodButton;

    private VideoDelegate videoDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoDelegate = new VideoDelegate() {

            @Override
            public void displayImageChooser(String image1, String image2, ImageChooserDelegate delegate) {
                hideSlideMenu();

                ImagePickerDialog newFragment = new ImagePickerDialog(image1,image2);
                newFragment.setDelegate(delegate);
                newFragment.show(getSupportFragmentManager(), "dialog");
            }
        };

        blockOverlay = findViewById(R.id.block_overlay);
        blockOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSlideMenu();
            }
        });

        slideMenu = (SlideMenu)getSupportFragmentManager().findFragmentById(R.id.slide_menu);

        slideMenu.setDelegate(new SlideMenuDelegate() {
            @Override
            public void onFilterSelected(SlideMenu slidemenu, Filter filter) {
                liveButton.setSelected(false);
                vodButton.setSelected(true);
                loadVodContent(filter);
            }

            @Override
            public void onVodSelected(SlideMenu slidemenu) {
                liveButton.setSelected(false);
                vodButton.setSelected(true);
                loadVodContent();
                hideSlideMenu();
            }

            @Override
            public void onLiveSelected(SlideMenu slidemenu) {
                liveButton.setSelected(true);
                vodButton.setSelected(false);
                loadLiveContent();
                hideSlideMenu();
            }
        });

        createActionBar();

        if (savedInstanceState == null) {
            loadLiveContent();
            findViewById(R.id.main_container).bringToFront();
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        }
        else {
            super.onBackPressed();
        }
    }

    private void createActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = LayoutInflater.from(this);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        View view = inflater.inflate(R.layout.actionbar, null);

        menuButton = (ImageButton)view.findViewById(R.id.item_menu);
        helpButton = (ImageButton)view.findViewById(R.id.item_help);

        liveButton = (ImageButton)view.findViewById(R.id.actionbar_live_button);
        vodButton = (ImageButton)view.findViewById(R.id.actionbar_vod_button);

        liveButton.setSelected(true);
        vodButton.setSelected(false);

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSlideMenuOpen) {
                    hideSlideMenu();
                }
                else {
                    showSlideMenu();
                }

            }
        });


        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSlideMenu();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, new HelpFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });


        liveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liveButton.setSelected(true);
                vodButton.setSelected(false);

                loadLiveContent();

                hideSlideMenu();
            }
        });


        vodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liveButton.setSelected(false);
                vodButton.setSelected(true);

                loadVodContent();

                hideSlideMenu();
            }
        });

        actionBar.setCustomView(view,layout);
    }

    private void showSlideMenu() {
        float slideMenuPosition;
        float menuButtonPosition;
        float helpButtonPosition;

        blockOverlay.setVisibility(View.VISIBLE);

        slideMenuPosition = 0;
        menuButtonPosition = 260.0f;
        helpButtonPosition = menuButtonPosition + menuButton.getWidth();

        isSlideMenuOpen = true;

        View slideMenuContainer = findViewById(R.id.slide_menu_container);

        ObjectAnimator animatorSlideMenu = ObjectAnimator.ofFloat(slideMenuContainer,"x",slideMenuPosition);
        ObjectAnimator animatorMenuButton = ObjectAnimator.ofFloat(menuButton,"x",menuButtonPosition);
        ObjectAnimator animatorHelpButton = ObjectAnimator.ofFloat(helpButton,"x",helpButtonPosition);


        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animatorSlideMenu, animatorHelpButton, animatorMenuButton);
        animSet.start();
    }

    private void hideSlideMenu() {
        float slideMenuPosition;
        float menuButtonPosition;
        float helpButtonPosition;

        blockOverlay.setVisibility(View.GONE);

        slideMenuPosition = -392.0f;
        menuButtonPosition = 0.0f;
        helpButtonPosition = menuButtonPosition + menuButton.getWidth();

        slideMenu.willHide();

        isSlideMenuOpen = false;

        View slideMenuContainer = findViewById(R.id.slide_menu_container);

        ObjectAnimator animatorSlideMenu = ObjectAnimator.ofFloat(slideMenuContainer,"x",slideMenuPosition);
        ObjectAnimator animatorMenuButton = ObjectAnimator.ofFloat(menuButton,"x",menuButtonPosition);
        ObjectAnimator animatorHelpButton = ObjectAnimator.ofFloat(helpButton,"x",helpButtonPosition);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animatorSlideMenu, animatorHelpButton, animatorMenuButton);
        animSet.start();
    }

    private void loadVodContent() {
        if (vodFragment == null) {
            vodFragment = new VodFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, vodFragment)
                .commit();
    }

    private void loadVodContent(Filter filter) {
        filteredFragment = new FilteredFragment(filter);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, filteredFragment)
                .commit();
        //hideSlideMenu();
    }

    private void loadLiveContent() {
        if (liveFragment == null) {
            liveFragment = new LiveFragment();
            liveFragment.setVideoSelectedDelegate(videoDelegate);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, liveFragment)
                .commit();
    }
    @Override
    public void onStart() {
        super.onStart();
        //EasyTracker.getInstance(getApplication()).activityStart(this); // Add this method.
    }

    @Override
    protected void onStop() {
        super.onStop();
        //EasyTracker.getInstance(getApplication()).activityStop(this);
    }
}