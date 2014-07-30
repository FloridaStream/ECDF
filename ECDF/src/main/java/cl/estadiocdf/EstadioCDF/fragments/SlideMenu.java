package cl.estadiocdf.EstadioCDF.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.idunnololz.widgets.AnimatedExpandableListView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.activities.LoginActivity;
import cl.estadiocdf.EstadioCDF.adapters.FilterLevel1Adapter;
import cl.estadiocdf.EstadioCDF.adapters.FilterLevel3Adapter;
import cl.estadiocdf.EstadioCDF.datamodel.Filter;
import cl.estadiocdf.EstadioCDF.datamodel.User;
import cl.estadiocdf.EstadioCDF.delegates.SlideMenuDelegate;
import cl.estadiocdf.EstadioCDF.services.ServiceManager;
import cl.estadiocdf.EstadioCDF.utils.CheckBoxCustom;
import cl.estadiocdf.EstadioCDF.utils.NaturalOrderComparator;

/**
 * Created by Franklin Cruz on 18-02-14.
 */
public class SlideMenu extends Fragment {

    private View rootView;
    private View listViewLevel3Container;
    private int expandedFilterListIndex = -1;
    private SlideMenuDelegate delegate;

    private CheckBoxCustom evento;
    private CheckBoxCustom notificacion;
    private RelativeLayout containerFilter;

    private Filter currentParentFilter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        Typeface extraBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/AkzidenzGrotesk-ExtraBoldCondItalic.otf");
        Typeface lightCondensedItalic = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");
        Typeface condensedItalic = Typeface.createFromAsset(getActivity().getAssets(), "fonts/AkzidenzGrotesk-CondItalic.otf");

        final View rootView = inflater.inflate(R.layout.slide_menu_2, container, false);

        assert rootView != null;

        TextView mainLabel = (TextView)rootView.findViewById(R.id.main_label);
        mainLabel.setTypeface(lightCondensedItalic);

        TextView filterLabel = (TextView)rootView.findViewById(R.id.filters_label);
        filterLabel.setTypeface(lightCondensedItalic);

        Button liveButton = (Button)rootView.findViewById(R.id.live_button);
        liveButton.setTypeface(lightCondensedItalic);

        Button vodButton = (Button)rootView.findViewById(R.id.vod_button);
        vodButton.setTypeface(lightCondensedItalic);

        Button logoutButton =(Button)rootView.findViewById(R.id.logout_button);
        logoutButton.setTypeface(lightCondensedItalic);

        TextView logoutLabel = (TextView)rootView.findViewById(R.id.logout_message_label);
        logoutLabel.setTypeface(condensedItalic);

        TextView recordatorio = (TextView)rootView.findViewById(R.id.recordatorio);
        recordatorio.setTypeface(lightCondensedItalic);

        containerFilter = (RelativeLayout) rootView.findViewById(R.id.contenedor_filtros);

        evento = (CheckBoxCustom)rootView.findViewById(R.id.recordatorio_evento_check);
        notificacion = (CheckBoxCustom)rootView.findViewById(R.id.recordatorio_notificacion_check);

        TextView eventoTxt = (TextView)rootView.findViewById(R.id.recordatorio_evento_text);
        eventoTxt.setTypeface(lightCondensedItalic);

        TextView notificacionTxt = (TextView)rootView.findViewById(R.id.recordatorio_notificacion_text);
        notificacionTxt.setTypeface(lightCondensedItalic);

        SharedPreferences  prefs = getActivity().getSharedPreferences("recordatorio", Context.MODE_PRIVATE);
        if(prefs.getBoolean("evento_e",false)){
            evento.setChecked(true);
        }
        else if(prefs.getBoolean("evento_n",false)){
            notificacion.setChecked(true);
        }

        //evento.setChecked(true);
        evento.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                SharedPreferences  prefs = getActivity().getSharedPreferences("recordatorio", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("evento_e",b);
                editor.commit();
            }
        });

        notificacion.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences  prefs = getActivity().getSharedPreferences("recordatorio", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("evento_n",b);
                editor.commit();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
                sharedPreferences.edit().clear().commit();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        liveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null) delegate.onLiveSelected(SlideMenu.this);
            }
        });

        vodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null) delegate.onVodSelected(SlideMenu.this);
            }
        });

        final AnimatedExpandableListView listViewLevel1Filters = (AnimatedExpandableListView)rootView.findViewById(R.id.filters_expandable_listview);
        final ListView listViewLevel3 = (ListView)rootView.findViewById(R.id.level3_filter_listview);


        listViewLevel3Container = rootView.findViewById(R.id.level3_filter_container);



        ServiceManager serviceManager = new ServiceManager(getActivity());

        User user = serviceManager.loadUserData();

        if (user != null) {
            TextView userLabel = (TextView)rootView.findViewById(R.id.username_label);
            userLabel.setTypeface(condensedItalic);
            userLabel.setText(user.getFirstName() + " " + user.getLastName());

            AQuery aq = new AQuery(rootView);
            aq.id(R.id.profile_picture).image(user.getImageUrl());
        }

        serviceManager.loadFilters(new ServiceManager.DataLoadedHandler<Filter>() {
            @Override
            public void loaded(List<Filter> data) {

                for(Filter f : data) {
                    if(f.getFilters() != null && f.getFilters().size() > 0) {
                        sortFilters(f.getFilters());
                    }
                }

                Filter darkFilter = new Filter();
                darkFilter.setOrder(Integer.MIN_VALUE);
                data.add(darkFilter);
                final FilterLevel1Adapter adapter = new FilterLevel1Adapter(getActivity(),data);
                listViewLevel1Filters.setAdapter(adapter);

                // In order to show animations, we need to use a custom click handler
                // for our ExpandableListView.

////////////////////////////////////////
                listViewLevel1Filters.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

                    @Override
                    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                        // We call collapseGroupWithAnimation(int) and
                        // expandGroupWithAnimation(int) to animate group
                        // expansion/collapse.
                        if (listViewLevel1Filters.isGroupExpanded(groupPosition)) {
                            listViewLevel1Filters.collapseGroupWithAnimation(groupPosition);
                        } else {
                            listViewLevel1Filters.expandGroupWithAnimation(groupPosition);
                        }
                        return true;
                    }

                });
/////////////////////////
                listViewLevel1Filters.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
                    @Override
                    public void onGroupCollapse(int groupPosition) {
                        expandedFilterListIndex = -1;
                        Filter filter = (Filter)adapter.getGroup(groupPosition);

                        LinearLayout.LayoutParams paramsContainerFilter = (LinearLayout.LayoutParams) containerFilter.getLayoutParams();
                        int delta = filter.getFilters().size() * 58;
                        if(filter.getFilters().size()>2){
                            delta = delta +13;
                        }

                        paramsContainerFilter.height = paramsContainerFilter.height - delta;
                        containerFilter.setLayoutParams(paramsContainerFilter);

                        RelativeLayout contenedor = (RelativeLayout) rootView.findViewById(R.id.contenedor);
                        RelativeLayout.LayoutParams params2;
                        int height = contenedor.getMeasuredHeight();
                        params2 =(RelativeLayout.LayoutParams) listViewLevel3Container.getLayoutParams();
                        params2.height = height - delta;
                        listViewLevel3Container.setLayoutParams(params2);

                        hideLevel3Filters();

                    }
                });
/////////////////////////////
                listViewLevel1Filters.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                    @Override
                    public void onGroupExpand(int groupPosition) {

                        if (expandedFilterListIndex >= 0) {
                            listViewLevel1Filters.collapseGroup(expandedFilterListIndex);
                        }

                        hideLevel3Filters();
                        expandedFilterListIndex = groupPosition;

                        Filter filter = (Filter)adapter.getGroup(groupPosition);
                        if(filter.getOrder()>Integer.MIN_VALUE){
                            if (filter.getFilters().size() == 0) {
                                if (delegate != null) delegate.onFilterSelected(SlideMenu.this, filter);
                            }
                        }

                        LinearLayout.LayoutParams paramsContainerFilter = (LinearLayout.LayoutParams) containerFilter.getLayoutParams();
                        int delta = filter.getFilters().size() * 58;
                        if(filter.getFilters().size()>2){
                            delta = delta + 13;
                        }
                        paramsContainerFilter.height = paramsContainerFilter.height + delta;
                        containerFilter.setLayoutParams(paramsContainerFilter);

                        ///
                        RelativeLayout r = (RelativeLayout) rootView.findViewById(R.id.contenedor);
                        RelativeLayout.LayoutParams params2;
                        params2 =(RelativeLayout.LayoutParams) listViewLevel3Container.getLayoutParams();
                        int height = r.getMeasuredHeight();
                        params2.height = height + delta;
                        listViewLevel3Container.setLayoutParams(params2);
                        ///

                    }
                });

                listViewLevel1Filters.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                        Filter parentFilter = (Filter)adapter.getGroup(groupPosition);
                        Filter filter = (Filter)adapter.getChild(groupPosition,childPosition);
                        filter.setParent(parentFilter);

                        if(filter.getFilters().size() == 0) {
                            if (delegate != null) delegate.onFilterSelected(SlideMenu.this, filter);
                        }

                        currentParentFilter = filter;

                        if (filter.getFilters().size() > 0) {
                            Log.e("Cantidad de elementos", "-> " +filter.getFilters().size());
                            showLevel3Filters();
                            listViewLevel3.setAdapter(new FilterLevel3Adapter(getActivity(), filter.getFilters()));
                        }

                        return true;
                    }
                });

                listViewLevel3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Filter filter = ((FilterLevel3Adapter)listViewLevel3.getAdapter()).getItem(position);
                        filter.setParent(currentParentFilter);
                        if (delegate != null) {
                            delegate.onFilterSelected(SlideMenu.this, filter);
                        }
                    }
                });
            }
        });



        this.rootView = rootView;

        return rootView;
    }

    private void sortFilters(List<Filter> filters) {

        final NaturalOrderComparator comparator = new NaturalOrderComparator();

        Collections.sort(filters, new Comparator<Filter>() {
            @Override
            public int compare(Filter lhs, Filter rhs) {
                return comparator.compare(lhs.getName(), rhs.getName());
            }
        });

        for(Filter f : filters) {
            if(f.getFilters() != null && f.getFilters().size() > 0) {
                sortFilters(f.getFilters());
            }
        }
    }

    @Override
    public View getView() {
        return rootView;
    }

    public void willHide() {
        hideLevel3Filters();
    }

    private void showLevel3Filters() {
        //ObjectAnimator animator = ObjectAnimator.ofFloat(listViewLevel3Container, "x", 490);
        listViewLevel3Container.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(listViewLevel3Container, "scale_x", 1);
        animator.setDuration(300);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator);
        animatorSet.start();
    }

    private void hideLevel3Filters() {
        //ObjectAnimator animator = ObjectAnimator.ofFloat(listViewLevel3Container, "x", 0);
        ObjectAnimator animator = ObjectAnimator.ofFloat(listViewLevel3Container, "scale_x", 0);
        animator.setDuration(300);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                listViewLevel3Container.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }

    public SlideMenuDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(SlideMenuDelegate delegate) {
        this.delegate = delegate;
    }
}
