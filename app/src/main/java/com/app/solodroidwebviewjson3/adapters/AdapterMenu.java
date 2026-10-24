package com.app.solodroidwebviewjson3.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.FrameLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.activities.MainActivity;
import com.app.solodroidwebviewjson3.models.Menu;
import com.app.solodroidwebviewjson3.utils.Constant;
import com.app.solodroidwebviewjson3.utils.SharedPref;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.solodroid.ads.sdk.format.NativeAdViewHolder;

import java.util.List;

public class AdapterMenu extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 0;
    private final int VIEW_AD = 1;
    private List<Menu> items;
    Context context;
    private OnItemClickListener mOnItemClickListener;
    private int clickedItemPosition = -1;
    SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Menu obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterMenu(Context context, List<Menu> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView menuName;
        public ImageView menuIcon;
        public ConstraintLayout lytItem;
        public FrameLayout lytParent;

        public OriginalViewHolder(View v) {
            super(v);
            menuName = v.findViewById(R.id.menu_name);
            menuIcon = v.findViewById(R.id.menu_icon);
            lytItem = v.findViewById(R.id.lyt_item);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_AD) {
            View v;
            if (Constant.native_ad_style.equals("small")) {
                v = LayoutInflater.from(parent.getContext()).inflate(com.solodroid.ads.sdk.R.layout.view_native_ad_radio, parent, false);
            } else if (Constant.native_ad_style.equals("medium")) {
                v = LayoutInflater.from(parent.getContext()).inflate(com.solodroid.ads.sdk.R.layout.view_native_ad_news, parent, false);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(com.solodroid.ads.sdk.R.layout.view_native_ad_news, parent, false);
            }
            vh = new NativeAdViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drawer, parent, false);
            vh = new OriginalViewHolder(v);
        }
        return vh;
    }

    @SuppressLint({"RecyclerView", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof OriginalViewHolder) {
            final Menu obj = items.get(position);
            OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.menuName.setText(obj.name);
            Glide.with(context)
                    .load(obj.icon.replace(" ", "%20"))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_circle)
                    .centerCrop()
                    .into(vItem.menuIcon);

            if (sharedPref.getIsDarkTheme()) {
                vItem.lytParent.setBackgroundColor(context.getResources().getColor(R.color.color_dark_background_nav_drawer));
            } else {
                vItem.lytParent.setBackgroundColor(context.getResources().getColor(R.color.color_light_background_nav_drawer));
            }

            vItem.lytParent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, obj, position);
                    clickedItemPosition = position;
                    notifyDataSetChanged();
                    ((MainActivity) context).loadWebPage(obj.name, obj.type, obj.url, obj.url_dark, obj.toolbar);
                }
            });

            if (clickedItemPosition == position) {
                vItem.lytItem.setBackgroundResource(R.drawable.bg_item_selected);
                vItem.menuName.setTextColor(ContextCompat.getColor(context, R.color.color_light_primary));
                if (Constant.navigationIconSolidColor) {
                    vItem.menuIcon.setColorFilter(context.getResources().getColor(R.color.color_light_primary), PorterDuff.Mode.SRC_IN);
                }
            } else {
                vItem.lytItem.setBackgroundResource(R.drawable.bg_item_unselected);
                if (sharedPref.getIsDarkTheme()) {
                    vItem.menuName.setTextColor(ContextCompat.getColor(context, R.color.color_dark_text_nav_drawer));
                    if (Constant.navigationIconSolidColor) {
                        vItem.menuIcon.setColorFilter(context.getResources().getColor(R.color.color_dark_text_nav_drawer), PorterDuff.Mode.SRC_IN);
                    }
                } else {
                    vItem.menuName.setTextColor(ContextCompat.getColor(context, R.color.color_light_text_nav_drawer));
                    if (Constant.navigationIconSolidColor) {
                        vItem.menuIcon.setColorFilter(context.getResources().getColor(R.color.color_light_text_nav_drawer), PorterDuff.Mode.SRC_IN);
                    }
                }
            }
        } else if (holder instanceof NativeAdViewHolder) {

            final NativeAdViewHolder vItem = (NativeAdViewHolder) holder;

            if (Constant.ad_status) {
                vItem.loadNativeAd(context,
                        "1",
                        1,
                        Constant.main_ad,
                        Constant.backup_ad,
                        Constant.admob_native_unit_id,
                        Constant.ad_manager_native_unit_id,
                        Constant.fan_native_unit_id,
                        Constant.applovin_native_manual_unit_id,
                        Constant.applovin_banner_zone_id,
                        "",
                        Constant.alien_ads_native_unit_id,
                        sharedPref.getIsDarkTheme(),
                        false,
                        Constant.native_ad_style,
                        R.color.color_light_native_ad_background,
                        R.color.color_dark_native_ad_background
                );
            }

            vItem.setNativeAdMargin(
                    context.getResources().getDimensionPixelOffset(R.dimen.gnt_no_margin),
                    context.getResources().getDimensionPixelOffset(R.dimen.gnt_no_margin),
                    context.getResources().getDimensionPixelOffset(R.dimen.gnt_no_margin),
                    context.getResources().getDimensionPixelOffset(R.dimen.spacing_medium)
            );

        }

    }

    @Override
    public int getItemViewType(int position) {
        Menu post = items.get(position);
        if (post != null) {
                if (post.name == null || post.name.equals("")) {
                    return VIEW_AD;
                } else {
                    return VIEW_ITEM;
                }
        } else {
            return VIEW_ITEM;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setListData(List<Menu> items) {
        items.add(0, new Menu());
        this.items = items;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}