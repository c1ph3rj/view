package tech.c1ph3rj.view.line_of_business;

import static tech.c1ph3rj.view.Services.checkNull;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

import tech.c1ph3rj.view.R;

public class LineOfBusinessAdapter extends ArrayAdapter<LineOfBusinessModel> {

    private static class ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        ImageView iconView;
    }

    public LineOfBusinessAdapter(Context context, List<LineOfBusinessModel> lineOfBusinessList) {
        super(context, R.layout.list_item_line_of_business, lineOfBusinessList);
    }


    @Override
    public void addAll(LineOfBusinessModel... items) {
        super.addAll(items);
        notifyDataSetChanged();
    }

    @Nullable
    @Override
    public LineOfBusinessModel getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }



    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LineOfBusinessModel lineOfBusiness = getItem(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_line_of_business, parent, false);
            viewHolder.titleTextView = convertView.findViewById(R.id.lobTitleView);
            viewHolder.descriptionTextView = convertView.findViewById(R.id.lobDescriptionView);
            viewHolder.iconView = convertView.findViewById(R.id.lobIconView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (lineOfBusiness != null) {
            if (checkNull(lineOfBusiness.mdTitle)) {
                viewHolder.titleTextView.setText(lineOfBusiness.mdTitle);
            } else {
                viewHolder.titleTextView.setText("-");
            }

            if (checkNull(lineOfBusiness.mdDesc)) {
                viewHolder.descriptionTextView.setText(lineOfBusiness.mdDesc);
            } else {
                viewHolder.descriptionTextView.setText("-");
            }

            if (checkNull(lineOfBusiness.iconURL)) {
                Glide.with(getContext())
                        .load(lineOfBusiness.iconURL)
                        .error(R.drawable.warning_ic)
                        .into(viewHolder.iconView);
            } else {
                Glide.with(getContext())
                        .load(R.drawable.warning_ic)
                        .into(viewHolder.iconView);
            }
        }

        return convertView;
    }
}

