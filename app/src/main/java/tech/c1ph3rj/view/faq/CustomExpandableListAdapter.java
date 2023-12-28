package tech.c1ph3rj.view.faq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import tech.c1ph3rj.view.R;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<String> listTitles;
    private final HashMap<String, List<String>> expandableListData;

    public CustomExpandableListAdapter(Context context, List<String> listTitles, HashMap<String, List<String>> expandableListData) {
        this.context = context;
        this.listTitles = listTitles;
        this.expandableListData = expandableListData;
    }

    @Override
    public int getGroupCount() {
        return listTitles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return expandableListData.get(listTitles.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listTitles.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return expandableListData.get(listTitles.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String title = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, null);
        }

        TextView groupTitle = convertView.findViewById(R.id.group_title);
        groupTitle.setText(title);


        ImageView groupIndicator = convertView.findViewById(R.id.group_indicator);
        if (isExpanded) {
            groupIndicator.setImageResource(R.drawable.up_ic);  // Set expanded icon
        } else {
            groupIndicator.setImageResource(R.drawable.down_ic);  // Set collapsed icon
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String content = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
        }

        TextView itemContent = convertView.findViewById(R.id.item_content);
        itemContent.setText(content);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

