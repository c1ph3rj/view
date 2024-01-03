package tech.c1ph3rj.view.products;

import static tech.c1ph3rj.view.Services.checkNull;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import java.util.List;

import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.line_of_business.LineOfBusinessModel;

public class ProductsAdapter extends ArrayAdapter<ProductsModel> {
    onProductSelectionListener listener;

    private static class ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        ImageView iconView;
        CheckBox checkBox;
        CardView layout;
    }

    public ProductsAdapter(Context context, List<ProductsModel> productsList, onProductSelectionListener listener) {
        super(context, R.layout.list_item_product, productsList);
        this.listener = listener;
    }


    @Override
    public void addAll(ProductsModel... items) {
        super.addAll(items);
        notifyDataSetChanged();
    }

    @Nullable
    @Override
    public ProductsModel getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }



    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ProductsModel productsModel = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_product, parent, false);
            viewHolder.titleTextView = convertView.findViewById(R.id.titleView);
            viewHolder.descriptionTextView = convertView.findViewById(R.id.descriptionView);
            viewHolder.iconView = convertView.findViewById(R.id.iconView);
            viewHolder.checkBox = convertView.findViewById(R.id.checkBoxView);
            viewHolder.layout = convertView.findViewById(R.id.productLayout);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(productsModel != null) {
            if(checkNull(productsModel.productUniqueID)) {
                viewHolder.titleTextView.setText(productsModel.productUniqueID);
            } else {
                viewHolder.titleTextView.setText(" - ");
            }

            if(checkNull(productsModel.productName)) {
                viewHolder.descriptionTextView.setText(productsModel.productName);
            } else {
                viewHolder.descriptionTextView.setText(" - ");
            }

            viewHolder.layout.setOnClickListener(onClickLayout -> viewHolder.checkBox.performClick());

            viewHolder.checkBox.setChecked(productsModel.isChecked);

            viewHolder.checkBox.setOnClickListener(onClickCheckBox -> {
                if(listener != null) {
                    if(!productsModel.isChecked) {
                        listener.itemChecked(productsModel);
                    } else {
                        listener.itemRemoved(productsModel);
                    }
                }
            });
        }

        return convertView;
    }

    public interface onProductSelectionListener {

        void itemChecked(ProductsModel model);

        void itemRemoved(ProductsModel model);
    }
}

