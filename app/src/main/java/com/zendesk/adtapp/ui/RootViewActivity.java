package com.zendesk.adtapp.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by rahulramachandra on 24/07/17.
 */

public class RootViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zendesk.adtapp.R.layout.root_view_activity);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.show();
        if (savedInstanceState == null) {
            PlaceholderFragment frag = new PlaceholderFragment();
            getFragmentManager().beginTransaction()
                    .add(com.zendesk.adtapp.R.id.container, frag)
                    .commit();
            frag.getView();
        }
    }
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
            // Intentionally empty
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(com.zendesk.adtapp.R.layout.fragment_main, container, false);
        }
    }
}
