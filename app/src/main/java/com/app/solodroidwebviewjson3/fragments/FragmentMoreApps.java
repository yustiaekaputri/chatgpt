package com.app.solodroidwebviewjson3.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.adapters.AdapterMoreApps;
import com.app.solodroidwebviewjson3.models.MoreApps;
import com.app.solodroidwebviewjson3.utils.SharedPref;

import java.util.ArrayList;
import java.util.List;

public class FragmentMoreApps extends Fragment {

    View rootView;
    public static final String TAG = "FragmentMoreApps";
    SharedPref sharedPref;
    AdapterMoreApps adapterMoreApps;
    RecyclerView recyclerView;
    Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_more_apps, container, false);
        initView();
        return rootView;
    }

    private void initView() {
        sharedPref = new SharedPref(activity);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapterMoreApps = new AdapterMoreApps(activity, new ArrayList<>());
        recyclerView.setAdapter(adapterMoreApps);
        displayData(sharedPref.getMoreAppsList());
    }

    private void displayData(List<MoreApps> moreApps) {
        if (moreApps != null && moreApps.size() > 0) {
            adapterMoreApps.setListData(moreApps);
            adapterMoreApps.setOnItemClickListener((view, obj, position) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(obj.url));
                startActivity(intent);
            });
            showNoItemView(false);
        } else {
            showNoItemView(true);
        }
    }

    private void showNoItemView(boolean show) {
        View lytNoItem = rootView.findViewById(R.id.lyt_no_item);
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(getString(R.string.msg_no_more_apps));
        if (show) {
            lytNoItem.setVisibility(View.VISIBLE);
        } else {
            lytNoItem.setVisibility(View.GONE);
        }
    }

}
