package io.alcatraz.audiohq.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.alcatraz.support.v4.appcompat.DrawerLayoutUtil;

import io.alcatraz.audiohq.CompatWithPipeActivity;
import io.alcatraz.audiohq.Constants;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.fragments.PrefFragment;

public class PreferenceActivity extends CompatWithPipeActivity {
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference);
        initViews();
        DrawerLayoutUtil.Immersive(toolbar, true, this);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        PrefFragment prefFragment = new PrefFragment();
        transaction.replace(R.id.pref_fragment_replace, prefFragment);
        transaction.commit();
    }

    public void initViews() {
        toolbar = findViewById(R.id.preferenceToolbar1);
        toolbar.setNavigationOnClickListener(p1 -> finish());
        toolbar.setTitle(R.string.activity_pref);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent().setAction(Constants.BROADCAST_ACTION_UPDATE_PREFERENCES));
    }
}
