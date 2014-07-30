package cl.estadiocdf.EstadioCDF.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;


import com.idunnololz.widgets.AnimatedExpandableListView;

import java.util.List;

import cl.estadiocdf.EstadioCDF.R;
import cl.estadiocdf.EstadioCDF.datamodel.Filter;

/**
 * Created by Franklin Cruz on 27-02-14.
 */
public class FilterLevel1Adapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

    private List<Filter> filters;
    private Context context;
    private LayoutInflater inflater;

    private Typeface lightCondensedItalic;

    public FilterLevel1Adapter(Context context, List<Filter> objects) {
        this.filters = objects;
        this.context = context;

        this.inflater = LayoutInflater.from(context);

        lightCondensedItalic = Typeface.createFromAsset(context.getAssets(), "fonts/FuturaLT-CondensedOblique.ttf");
    }


    @Override
    public int getGroupCount() {
        return filters.size();
    }

    public int _getChildrenCount(int groupPosition) {
        return filters.get(groupPosition).getFilters().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return filters.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return filters.get(groupPosition).getFilters().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return Long.parseLong(String.valueOf(groupPosition) + String.valueOf(childPosition));
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (filters.get(groupPosition).getOrder()==Integer.MIN_VALUE){
            View group = inflater.inflate(R.layout.filter_level_1_dark_cell, parent, false);
            group.setClickable(true);
            group.setFocusable(true);
            return group;
        }else{
            View group = inflater.inflate(R.layout.filter_level_1_cell, parent, false);

            TextView titleText = (TextView)group.findViewById(R.id.title_label);
            titleText.setTypeface(lightCondensedItalic);

            titleText.setText(filters.get(groupPosition).getName());
            Button moreButton = (Button)group.findViewById(R.id.more_button);

            if(isExpanded) {
                moreButton.setText("-");
            }
            else {
                moreButton.setText("+");
            }
            return group;
        }

    }

    @Override
    public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return _getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        return _getChildrenCount(groupPosition);
    }

    public View _getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View child = inflater.inflate(R.layout.filter_level_2_cell, parent, false);

        TextView titleText = (TextView)child.findViewById(R.id.title_label);
        titleText.setTypeface(lightCondensedItalic);

        titleText.setText(filters.get(groupPosition).getFilters().get(childPosition).getName());

        return child;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
