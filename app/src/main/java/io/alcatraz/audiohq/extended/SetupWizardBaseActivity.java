package io.alcatraz.audiohq.extended;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.adapters.SetupPagerAdapter;
import io.alcatraz.audiohq.beans.SetupPage;
import io.alcatraz.audiohq.utils.AnimateUtils;
import io.alcatraz.audiohq.utils.Utils;

public abstract class SetupWizardBaseActivity extends CompatWithPipeActivity {
    TextView setup_title;
    NoScrollViewPager setup_pager;
    ProgressBar setup_progress;
    Button setup_forward;
    Button setup_next;
    LinearLayout setup_nav;
    FrameLayout setup_progress_bar_limit;

    SetupPagerAdapter adapter;

    //Data
    List<SetupPage> pages = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        initViews();
        initPages();
    }

    public abstract void onPageInit(List<SetupPage> pages);

    public abstract void onFinishSetup();

    public ProgressBar getProgressBar() {
        return setup_progress;
    }

    public void setTitle(CharSequence title) {
        setTitle(title, true);
    }

    public void setTitle(CharSequence title, boolean animate) {
        if (animate)
            AnimateUtils.textChange(setup_title, title);
        else
            setup_title.setText(title);
    }

    public void removeAllPages() {
        pages.clear();
        adapter.notifyDataSetChanged();
    }

    public void addPage(SetupPage page) {
        pages.add(page);
        adapter.notifyDataSetChanged();
    }

    public void removePage(Object o) {
        if (o instanceof SetupPage)
            pages.remove(o);
        else
            pages.remove((int) o);
        adapter.notifyDataSetChanged();
    }

    public NoScrollViewPager getPager() {
        return setup_pager;
    }

    public List<SetupPage> getPageList() {
        return pages;
    }

    public void setShowProgress(boolean showProgress) {
        if (showProgress)
            setup_progress_bar_limit.setVisibility(View.VISIBLE);
        else
            setup_progress_bar_limit.setVisibility(View.GONE);
    }

    public Button getBtnForward() {
        return setup_forward;
    }

    public Button getBtnNext() {
        return setup_next;
    }

    public void restoreState() {
        setShowProgress(false);
        getBtnForward().setEnabled(true);
        getBtnForward().setTextColor(Color.BLACK);
        getBtnNext().setEnabled(true);
        getBtnNext().setTextColor(Color.BLACK);
    }

    public void banNextStep(){
        getBtnNext().setEnabled(false);
        getBtnNext().setTextColor(Color.GRAY);
    }

    public void banPageSwitch(){
        getBtnNext().setEnabled(false);
        getBtnForward().setEnabled(false);
        getBtnNext().setTextColor(Color.GRAY);
        getBtnForward().setTextColor(Color.GRAY);
    }

    public void startPending(){
        setShowProgress(true);
        banPageSwitch();
    }

    public void endPending(){
        setShowProgress(false);
        restoreState();
    }
    private void findViews() {
        setup_title = findViewById(R.id.setup_title);
        setup_pager = findViewById(R.id.setup_pager);
        setup_progress = findViewById(R.id.setup_progress);
        setup_forward = findViewById(R.id.setup_btn_forward);
        setup_next = findViewById(R.id.setup_btn_next);
        setup_nav = findViewById(R.id.setup_nav_bar);
        setup_progress_bar_limit = findViewById(R.id.setup_progress_bar_limit);
    }

    private void initViews() {
        findViews();
        setup_nav.setPadding(setup_nav.getPaddingLeft(),
                setup_nav.getPaddingTop(),
                setup_nav.getPaddingRight(),
                setup_nav.getPaddingBottom() + Utils.getNavigationBarHeight(this));
        setup_pager.setPadding(setup_pager.getPaddingLeft(),
                setup_pager.getPaddingTop(),
                setup_pager.getPaddingRight(),
                setup_pager.getPaddingBottom() + Utils.getNavigationBarHeight(this));
        setup_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                setTitle(pages.get(i).getTitle());
                if (i == 0) {
                    setup_forward.setVisibility(View.GONE);
                } else if (i == pages.size() - 1) {
                    setup_next.setText(R.string.setup_step_next_final);
                } else {
                    setup_next.setVisibility(View.VISIBLE);
                    setup_forward.setVisibility(View.VISIBLE);
                    setup_next.setText(R.string.setup_step_next);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        setup_forward.setOnClickListener(view -> {
            if (setup_pager.getCurrentItem() != 0)
                setup_pager.setCurrentItem(setup_pager.getCurrentItem() - 1);
        });
        setup_next.setOnClickListener(view -> {
            if (setup_pager.getCurrentItem() != pages.size() - 1)
                setup_pager.setCurrentItem(setup_pager.getCurrentItem() + 1);
            else
                onFinishSetup();
        });
    }

    private void initPages() {
        pages.clear();
        onPageInit(pages);
        adapter = new SetupPagerAdapter(pages, this);
        setup_pager.setAdapter(adapter);
        setup_forward.setVisibility(View.GONE);
        setTitle(pages.get(0).getTitle());
    }
}
