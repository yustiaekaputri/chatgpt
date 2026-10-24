package com.app.solodroidwebviewjson3.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.fragments.FragmentMoreApps;
import com.app.solodroidwebviewjson3.fragments.FragmentSettings;
import com.app.solodroidwebviewjson3.utils.SharedPref;
import com.app.solodroidwebviewjson3.utils.Tools;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ActivityMoreOptions extends AppCompatActivity {

    ViewPager viewPager;
    SharedPref sharedPref;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_more_options);
        Tools.setNavigation(this);
        sharedPref = new SharedPref(this);
        initViews();
        setupToolbar();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewPager.getCurrentItem() > 0) {
                    viewPager.setCurrentItem(0);
                } else {
                    finish();
                }
            }
        });
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout.post(() -> tabLayout.setupWithViewPager(viewPager));
        if (sharedPref.getIsDarkTheme()) {
            tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_toolbar));
        } else {
            tabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_background));
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new FragmentSettings(), getString(R.string.title_tab_settings));
        adapter.addFrag(new FragmentMoreApps(), getString(R.string.title_tab_more_apps));
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    /** @noinspection deprecation*/
    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, getString(R.string.title_more_options), true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}
