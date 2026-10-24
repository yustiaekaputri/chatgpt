package com.app.solodroidwebviewjson3.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.models.MoreApps;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

@SuppressLint("NotifyDataSetChanged")
public class AdapterMoreApps extends RecyclerView.Adapter<AdapterMoreApps.ViewHolder> {

    private List<MoreApps> moreApps;
    Context context;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, MoreApps obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterMoreApps(Context context, List<MoreApps> moreApps) {
        this.moreApps = moreApps;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView moreAppsTitle;
        public ImageView moreAppsImage;
        public View lytParent;

        public ViewHolder(View v) {
            super(v);
            moreAppsTitle = v.findViewById(R.id.more_apps_title);
            moreAppsImage = v.findViewById(R.id.more_apps_image);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_more_apps, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final MoreApps apps = moreApps.get(position);

        holder.moreAppsTitle.setText(apps.title);

        if (!apps.image.equals("")) {
            Glide.with(context)
                    .load(apps.image.replace(" ", "%20"))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(holder.moreAppsImage);
            holder.moreAppsImage.setVisibility(View.VISIBLE);
        } else {
            holder.moreAppsImage.setVisibility(View.GONE);
        }

        holder.lytParent.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, apps, position);
            }
        });
    }

    public void setListData(List<MoreApps> moreApps) {
        this.moreApps = moreApps;
        notifyDataSetChanged();
    }

    public void resetListData() {
        this.moreApps.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return moreApps.size();
    }

}