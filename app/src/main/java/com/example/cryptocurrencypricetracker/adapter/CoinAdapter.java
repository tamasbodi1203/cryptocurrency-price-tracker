package com.example.cryptocurrencypricetracker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cryptocurrencypricetracker.R;
import com.example.cryptocurrencypricetracker.activity.BaseActivity;
import com.example.cryptocurrencypricetracker.entity.Coin;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.ViewHolder> implements Filterable {

    private static final String LOG_TAG = CoinAdapter.class.getName();
    private static final DecimalFormat df_price = new DecimalFormat("0.0000");
    private static final DecimalFormat df_percentage = new DecimalFormat("0.00");
    private ArrayList<Coin> mCoinsData;
    private ArrayList<Coin> mWatchlistData;
    private ArrayList<Coin> mItemDataAll;
    private final Context mContext;
    private int lastPosition = -1;
    private boolean isWatchlist;
    private long mLastClickTime = 0;

    public CoinAdapter(Context context) {
        this.mContext = context;
    }

    public void setCoins(ArrayList<Coin> coins) {
        this.mCoinsData = coins;
    }

    public void setWatchlist(ArrayList<Coin> watchlist) {
        this.mWatchlistData = watchlist;
    }

    public void setIsWatchlist(Boolean isWatchlist) {
        this.isWatchlist = isWatchlist;
    }

    @Override
    public CoinAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CoinAdapter.ViewHolder holder, int position) {
        Coin currentItem;
        if (!isWatchlist) {
            currentItem = mCoinsData.get(position);
        } else {
            currentItem = mWatchlistData.get(position);
        }

        holder.bindTo(currentItem);

        if(holder.getAdapterPosition() > lastPosition) {
            Animation animation;
            if (!isWatchlist) {
                animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            } else {
                animation = AnimationUtils.loadAnimation(mContext, R.anim.fall_down_row);
            }
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mCoinsData.size();
    }

    @Override
    public Filter getFilter() {
        return coinFilter;
    }

    private final Filter coinFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Coin> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(charSequence == null || charSequence.length() == 0) {
                results.count = mItemDataAll.size();
                results.values = mItemDataAll;
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(Coin item : mItemDataAll) {
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
            mCoinsData = (ArrayList)filterResults.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mSymbolText;
        private final TextView mPriceText;
        private final TextView mPercentageChangeText;
        private final Switch mWatchlistToggleButton;
        private final ImageView mItemImage;

        ViewHolder(View itemView) {
            super(itemView);

            mItemImage = itemView.findViewById(R.id.itemImage);
            mSymbolText = itemView.findViewById(R.id.itemSymbol);
            mPriceText = itemView.findViewById(R.id.itemPrice);
            mPercentageChangeText = itemView.findViewById(R.id.itemChangePercent);
            mWatchlistToggleButton = itemView.findViewById(R.id.itemWatchlistSwitch);

            mWatchlistToggleButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View view) {
                    // Gombnyomás spamolás elleni védelem
                    mWatchlistToggleButton.setClickable(false);
                    Coin coin = mCoinsData.stream()
                            .filter(e -> e.getSymbol().equals(mSymbolText.getText().toString())).findAny().orElseThrow(null);
                    if (mWatchlistToggleButton.isChecked()) {
                        if (!mWatchlistData.contains(coin)) {
                            ((BaseActivity) mContext).addToWatchlist(coin);
                        }
                    } else {
                        ((BaseActivity) mContext).removeFromWatchlist(coin);
                        if (isWatchlist) {
                            deleteItem(itemView);
                        }
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            mWatchlistToggleButton.setClickable(true);
                        }
                    }, 400);
                }
            });
        }

        void bindTo(Coin currentItem){
            mSymbolText.setText(currentItem.getSymbol());
            String price = currentItem.getPrice() < 10 ? df_price.format(currentItem.getPrice()) : df_percentage.format(currentItem.getPrice());
            mPriceText.setText(new StringBuilder().append('$').append(price).toString());

            String percentageChange = df_percentage.format(currentItem.getPercentageChange());
            mPercentageChangeText.setText(new StringBuilder().append(percentageChange).append('%').toString());
            if (currentItem.getPercentageChange() < 0) {
                mPercentageChangeText.setTextColor(Color.RED);
            } else {
                mPercentageChangeText.setText(new StringBuilder().append("+").append(mPercentageChangeText.getText().toString()));
                mPercentageChangeText.setTextColor(Color.parseColor("#32A400"));
            }

            if (mWatchlistData.contains(currentItem)) {
                mWatchlistToggleButton.setChecked(true);
            } else {
                mWatchlistToggleButton.setChecked(false);
            }
            Glide.with(mContext).load(currentItem.getImageResource()).into(mItemImage);
        }
    }

    private void deleteItem(View itemView) {

        Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_out_right);
        animation.setDuration(400);
        itemView.startAnimation(animation);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (mWatchlistData.size() == 0) {

                    RelativeLayout grandParent = (RelativeLayout) itemView.getParent().getParent();
                    TextView mEmptyListTextView = (TextView) grandParent.findViewById(R.id.emptyListTextView);
                    mEmptyListTextView.setVisibility(View.VISIBLE);
                }
                notifyDataSetChanged();
            }

        }, animation.getDuration());
    }
}
