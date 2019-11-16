package io.alcatraz.audiohq.activities;

import android.os.Bundle;
import android.widget.TextView;

import io.alcatraz.audiohq.CompatWithPipeActivity;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.extended.NoScrollViewPager;

public class SetupActivity extends CompatWithPipeActivity{
    TextView setup_title;
    NoScrollViewPager setup_pager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
    }
}
