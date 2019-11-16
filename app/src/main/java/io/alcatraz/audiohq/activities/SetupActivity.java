package io.alcatraz.audiohq.activities;

import android.graphics.Color;
import android.support.v4.view.ViewPager;

import java.util.List;

import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.SetupPage;
import io.alcatraz.audiohq.extended.SetupWizardBaseActivity;

public class SetupActivity extends SetupWizardBaseActivity {
    @Override
    public void onPageInit(List<SetupPage> pages) {
        String[] setup_titles = getResources().getStringArray(R.array.setup_page_titles);
        int[] page_layout_ids = {R.layout.setup_1, R.layout.setup_2, R.layout.setup_3,
                R.layout.setup_4, R.layout.setup_5, R.layout.setup_6};

        for (int i = 0; i < setup_titles.length; i++) {
            SetupPage page = new SetupPage(setup_titles[i], page_layout_ids[i]);
            pages.add(page);
        }

        getPager().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i){
                    case 1:
                        setShowProgress(true);
                        getBtnNext().setEnabled(false);
                        getBtnNext().setTextColor(Color.GRAY);
                        break;
                }
                if(i != 1)
                    restoreState();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public void onFinishSetup() {

    }

    private void restoreState(){
        setShowProgress(false);
        getBtnNext().setEnabled(true);
        getBtnNext().setTextColor(Color.BLACK);
    }
}
