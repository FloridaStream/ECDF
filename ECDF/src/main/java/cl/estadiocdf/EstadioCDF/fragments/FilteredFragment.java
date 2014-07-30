package cl.estadiocdf.EstadioCDF.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.activities.VideoActivity;
import cl.estadiocdf.EstadioCDF.adapters.FilteredVodArrayAdapter;
import cl.estadiocdf.EstadioCDF.datamodel.Filter;
import cl.estadiocdf.EstadioCDF.datamodel.Media;
import cl.estadiocdf.EstadioCDF.delegates.FilteredArrayAdapterDelegate;
import cl.estadiocdf.EstadioCDF.serializables.MediaSerializable;
import cl.estadiocdf.EstadioCDF.services.ServiceManager;

/**
 * Created by Franklin Cruz on 27-02-14.
 */
public class FilteredFragment extends Fragment {

    private Filter filter;

    private GridView gridView;
    private TextView titleTextView;

    public FilteredFragment() { }

    public FilteredFragment(Filter filter) {
        this.filter = filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
        loadData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Typeface lightCondensedItalic2 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");

        View rootView = inflater.inflate(R.layout.fragment_filtered, container, false);

        gridView = (GridView)rootView.findViewById(R.id.gridview);
        titleTextView = (TextView)rootView.findViewById(R.id.filters_title_label);
        titleTextView.setTypeface(lightCondensedItalic2);

        ImageView refresh = (ImageView) rootView.findViewById(R.id.refresh);

        refresh.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });

        loadData();

        return rootView;
    }

    private void loadData() {

        if (gridView == null) {
            return;
        }

        Filter topFilter = filter;
        String titleText = null;
        do {

            if (titleText == null) {
                titleText = "<b>" + topFilter.getName() + "</b>";
            }
            else {
                titleText = topFilter.getName() + " | " + titleText;
            }

            topFilter = topFilter.getParent();
        }while (topFilter != null);

        titleTextView.setText(Html.fromHtml(titleText));

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.show();
        progress.setContentView(R.layout.progress_dialog);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);

        ServiceManager serviceManager = new ServiceManager(getActivity());
        serviceManager.loadVODMediaByCategoryId(filter.getCategories().toArray(new String[0]), new ServiceManager.DataLoadedHandler<Media>() {
            @Override
            public void loaded(List<Media> media) {

                FilteredVodArrayAdapter adapter = new FilteredVodArrayAdapter(getActivity(), getActivity()
                        .getApplicationContext(), media);

                adapter.setDelegate(new FilteredArrayAdapterDelegate() {
                    @Override
                    public void onShowViewClicked(Media media) {
                        Intent intent = new Intent(getActivity(), VideoActivity.class);
                        MediaSerializable mediaSerializable = new MediaSerializable();
                        mediaSerializable.setMedia(media);
                        intent.putExtra("media",mediaSerializable);
                        startActivity(intent);
                    }
                });

                gridView.setAdapter(adapter);

                progress.dismiss();
            }

            @Override
            public void error(String error) {
                progress.dismiss();
            }
        });
    }
}
