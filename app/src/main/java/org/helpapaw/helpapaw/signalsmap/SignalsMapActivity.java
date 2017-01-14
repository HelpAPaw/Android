package org.helpapaw.helpapaw.signalsmap;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.JobApi;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import net.vrallev.android.cat.Cat;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseActivity;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.utils.backgroundscheduler.SignalsSyncJob;
import org.helpapaw.helpapaw.utils.services.WakeupAlarm;

import java.util.List;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class SignalsMapActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        showSideNavigationPrompt();
        if (null == savedInstanceState) {
            if(getIntent().hasExtra(Signal.KEY_SIGNAL)){
                initFragment(SignalsMapFragment.newInstance((Signal) getIntent().getParcelableExtra(Signal.KEY_SIGNAL)));
            }else {
                initFragment(SignalsMapFragment.newInstance());
            }
        }
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.app_name);
    }

    private void initFragment(Fragment signalsMapFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.grp_content_frame, signalsMapFragment);
        transaction.commit();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }

    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawers();
        } else {
            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            if (fragmentList != null) {
                for (Fragment fragment : fragmentList) {
                    if (fragment instanceof SignalsMapFragment) {
                        ((SignalsMapFragment) fragment).onBackPressed();
                    }
                }
            }
        }
    }


    MaterialTapTargetPrompt mFabPrompt;

    public void showFabPrompt()
    {
        if (mFabPrompt != null)
        {
            return;
        }
        mFabPrompt = new MaterialTapTargetPrompt.Builder(SignalsMapActivity.this)
                .setTarget(findViewById(R.id.fab_add_signal))
                .setPrimaryText(R.string.text_tutorial_send_signal)
                .setSecondaryText(R.string.text_fab_add_signal)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener()
                {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget)
                    {
                        mFabPrompt = null;

                        //Do something such as storing a value so that this prompt is never shown again
                    }

                    @Override
                    public void onHidePromptComplete()
                    {

                    }
                })
                .create();
        mFabPrompt.show();
    }

    public void showSideNavigationPrompt()
    {
        final MaterialTapTargetPrompt.Builder tapTargetPromptBuilder = new MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText(R.string.menu_prompt_title)
                .setSecondaryText(R.string.menu_prompt_description)
                .setBackgroundColourFromRes(R.color.color_primary_dark)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setMaxTextWidth(R.dimen.tap_target_menu_max_width);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tapTargetPromptBuilder.setIcon(R.drawable.ic_menu);
        }else{
            tapTargetPromptBuilder.setIcon(R.drawable.ic_menu_white_24dp);
        }
        final Toolbar tb = (Toolbar) this.findViewById(R.id.toolbar);
        tapTargetPromptBuilder.setTarget(tb.getChildAt(1));

        tapTargetPromptBuilder.setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener()
        {
            @Override
            public void onHidePrompt(MotionEvent event, boolean tappedTarget)
            {

                //Do something such as storing a value so that this prompt is never shown again
            }

            @Override
            public void onHidePromptComplete()
            {
                showOverflowPromptRefresh();
            }
        });
        tapTargetPromptBuilder.show();
    }

    public void showOverflowPromptRefresh()
    {
        final MaterialTapTargetPrompt.Builder tapTargetPromptBuilder = new MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText(R.string.overflow_prompt_title_refresh)
                .setSecondaryText(R.string.overflow_prompt_description)
                .setTarget(R.id.menu_item_refresh)
                .setBackgroundColourFromRes(R.color.color_primary_dark)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                .setIcon(R.drawable.ic_refresh);
//        final Toolbar tb = (Toolbar) this.findViewById(R.id.toolbar);
//        tapTargetPromptBuilder.setTarget(tb.getChildAt(2));

        tapTargetPromptBuilder.setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener()
        {
            @Override
            public void onHidePrompt(MotionEvent event, boolean tappedTarget)
            {
                //Do something such as storing a value so that this prompt is never shown again
            }

            @Override
            public void onHidePromptComplete()
            {
                showOverflowPromptFilter();
            }
        });
        tapTargetPromptBuilder.show();
    }


    public void showOverflowPromptFilter()
    {
        final MaterialTapTargetPrompt.Builder tapTargetPromptBuilder = new MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText(R.string.overflow_prompt_title_filter)
                .setBackgroundColourFromRes(R.color.color_primary_dark)
                .setSecondaryText(R.string.overflow_prompt_description_filter)
                .setTarget(R.id.menu_item_filter)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                .setIcon(R.drawable.ic_filter_list_white_24dp);
//        final Toolbar tb = (Toolbar) this.findViewById(R.id.toolbar);
//        tapTargetPromptBuilder.setTarget(tb.getChildAt(2));

        tapTargetPromptBuilder.setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener()
        {
            @Override
            public void onHidePrompt(MotionEvent event, boolean tappedTarget)
            {
                //Do something such as storing a value so that this prompt is never shown again
            }

            @Override
            public void onHidePromptComplete()
            {
                showFabPrompt();

            }
        });
        tapTargetPromptBuilder.show();
    }
//    public void showStylePrompt(View view)
//    {
//        final MaterialTapTargetPrompt.Builder builder = new MaterialTapTargetPrompt.Builder(this, R.style.MaterialTapTargetPromptTheme_FabTarget);
//        final Toolbar tb = (Toolbar) this.findViewById(R.id.toolbar);
//        builder.setIcon(R.drawable.ic_filter_list_white_24dp)
//                .setTarget(tb.getChildAt(2))
//                .show();
//    }




}
