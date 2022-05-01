package com.example.cryptocurrencypricetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CoinItemAdapter extends RecyclerView.Adapter<CoinItemAdapter.ViewHolder> implements Filterable {
    // Member variables.
    private ArrayList<CoinItem> mItemData = new ArrayList<>();
    private ArrayList<CoinItem> mItemDataAll = new ArrayList<>();
    private Context mContext;
    private int lastPosition = -1;

    CoinItemAdapter(Context context, ArrayList<CoinItem> itemsData) {
        this.mItemData = itemsData;
        this.mItemDataAll = itemsData;
        this.mContext = context;
    }

    @Override
    public CoinItemAdapter.ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CoinItemAdapter.ViewHolder holder, int position) {
        // Get current sport.
        CoinItem currentItem = mItemData.get(position);

        // Populate the textviews with data.
        holder.bindTo(currentItem);


        if(holder.getAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mItemData.size();
    }


    /**
     * RecycleView filter
     * **/
    @Override
    public Filter getFilter() {
        return shopingFilter;
    }

    private Filter shopingFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<CoinItem> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(charSequence == null || charSequence.length() == 0) {
                results.count = mItemDataAll.size();
                results.values = mItemDataAll;
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(CoinItem item : mItemDataAll) {
                    if(item.getSymbol().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }

                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mItemData = (ArrayList)filterResults.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {

        // Member Variables for the TextViews
        private TextView mSymbolText;
        private TextView mPriceText;
        private ImageView mItemImage;

        ViewHolder(View itemView) {
            super(itemView);

            // Initialize the views.
            mSymbolText = itemView.findViewById(R.id.itemSymbol);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mPriceText = itemView.findViewById(R.id.itemPrice);

            itemView.findViewById(R.id.addToWatchlist).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //((CryptocurrencyListActivity)mContext).updateAlertIcon();
                }
            });
        }

        void bindTo(CoinItem currentItem){
            mSymbolText.setText(currentItem.getSymbol());
            mPriceText.setText(new StringBuilder().append('$').append(String.valueOf(currentItem.getPrice())).toString());

            // Load the images into the ImageView using the Glide library.
            Glide.with(mContext).load(currentItem.getImageResource()).into(mItemImage);
        }
    }
}
