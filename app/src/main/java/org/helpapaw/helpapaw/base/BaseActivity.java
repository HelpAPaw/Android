package org.helpapaw.helpapaw.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.about.AboutActivity;
import org.helpapaw.helpapaw.authentication.AuthenticationActivity;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.databinding.ActivityBaseBinding;
import org.helpapaw.helpapaw.faq.FAQsView;
import org.helpapaw.helpapaw.mysignals.MySignalsActivity;
import org.helpapaw.helpapaw.mynotifications.MyNotificationsActivity;
import org.helpapaw.helpapaw.privacypolicy.PrivacyPolicyActivity;
import org.helpapaw.helpapaw.reusable.AlertDialogFragment;
import org.helpapaw.helpapaw.settings.SettingsActivity;
import org.helpapaw.helpapaw.share.ShareActivity;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.SharingUtils;
import org.helpapaw.helpapaw.utils.Utils;

/**
 * Created by iliyan on 6/22/16
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final int RC_MY_SIGNALS = 321;

    protected ActivityBaseBinding binding;
    private ActionBarDrawerToggle drawerToggle;
    protected UserManager userManager;

    private static final String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, getLayoutId());
        setSupportActionBar(binding.toolbar);
        userManager = Injection.getUserManagerInstance();

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            VectorDrawableCompat indicator =
                    VectorDrawableCompat.create(getResources(), R.drawable.ic_menu, getTheme());
            if (indicator != null) {
                indicator.setTint(ResourcesCompat.getColor(getResources(), android.R.color.white, getTheme()));
            }
            supportActionBar.setHomeAsUpIndicator(indicator);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
            binding.toolbarTitle.setText(getToolbarTitle());
        }

        drawerToggle = setupDrawerToggle();
        binding.drawer.addDrawerListener(drawerToggle);

        binding.navView.setNavigationItemSelectedListener(getNavigationItemSelectedListener());
    }

    public NavigationView.OnNavigationItemSelectedListener getNavigationItemSelectedListener() {
        return new NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.nav_item_sign_in_out:
                        if (userManager.isLoggedIn()) {
                            logOut();
                        } else {
                            logIn();
                        }
                        break;

                    case R.id.nav_item_my_signals:
                        if (userManager.isLoggedIn()) {
                            navigateMySignalsSection();
                        } else {
                            showRegistrationRequiredAlert();
                        }
                        break;

                    case R.id.nav_item_my_notifications:
                        navigateMyNotificationsSection();
                        break;

                    case R.id.nav_item_faqs:
                        menuItem.setChecked(false);
                        navigateFAQsSection();
                        break;

                    case R.id.nav_item_feedback:
                        SharingUtils.contactSupport(BaseActivity.this);
                        menuItem.setChecked(false);
                        break;

                    case R.id.nav_item_about:
                        menuItem.setChecked(true);
                        Intent aboutIntent = new Intent(BaseActivity.this, AboutActivity.class);
                        startActivity(aboutIntent);
                        break;

                    case R.id.nav_item_share:
                        menuItem.setChecked(true);
                        Intent shareIntent = new Intent(BaseActivity.this, ShareActivity.class);
                        startActivity(shareIntent);
                        break;

                    case R.id.nav_item_privacy_policy:
                        menuItem.setChecked(true);
                        Intent ppIntent = new Intent(BaseActivity.this, PrivacyPolicyActivity.class);
                        startActivity(ppIntent);
                        break;

                    case R.id.nav_item_settings:
                        menuItem.setChecked(false);
                        navigateSettingsSection();
                        break;
                }

                return true;
            }
        };
    }

    private void logIn() {
        Intent intent = new Intent(PawApplication.getContext(), AuthenticationActivity.class);
        startActivity(intent);
    }

    protected void logOut() {
        if (Utils.getInstance().hasNetworkConnection()) {
            userManager.logout(new UserManager.LogoutCallback() {
                @Override
                public void onLogoutSuccess() {
                    Snackbar.make(binding.getRoot(), R.string.txt_logout_succeeded, Snackbar.LENGTH_LONG).show();
                    binding.navView.getMenu().findItem(R.id.nav_item_sign_in_out).setTitle(R.string.txt_log_in);
                    Intent intent = new Intent(getApplicationContext(), AuthenticationActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onLogoutFailure(String message) {
                    AlertDialogFragment.showAlert(getString(R.string.txt_logout_failed), message, true, BaseActivity.this.getSupportFragmentManager());
                }
            });
        } else {
            AlertDialogFragment.showAlert(getString(R.string.txt_logout_failed), getResources().getString(R.string.txt_no_internet), false, BaseActivity.this.getSupportFragmentManager());
        }
    }

    private void navigateSettingsSection() {
        Intent intent = new Intent(BaseActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void navigateFAQsSection() {
        Intent intent = new Intent(this, FAQsView.class);
        startActivity(intent);
    }

    private void navigateMySignalsSection() {
        Intent intent = new Intent(this, MySignalsActivity.class);
        startActivity(intent);
    }

    private void showRegistrationRequiredAlert() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this)
                .setTitle(R.string.txt_registration_required)
                .setMessage(R.string.txt_only_registered_users_can_see_my_signals)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(PawApplication.getContext(), AuthenticationActivity.class);
                        startActivityForResult(intent, RC_MY_SIGNALS);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        alertBuilder.create().show();
    }

    private void navigateMyNotificationsSection() {
        Intent intent = new Intent(this, MyNotificationsActivity.class);
        startActivity(intent);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this,
                binding.drawer,
                binding.toolbar,
                R.string.drawer_open,
                R.string.drawer_close);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            binding.drawer.openDrawer(GravityCompat.START);
        }
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();

        binding.navView.getMenu().findItem(R.id.nav_item_sign_in_out).setChecked(false);

        if (userManager.isLoggedIn()) {
            binding.navView.getMenu().findItem(R.id.nav_item_sign_in_out).setTitle(R.string.txt_log_out);
            final TextView title = binding.navView.getHeaderView(0).findViewById(R.id.nav_title);
            if (title != null) {
                userManager.getUserName(new UserManager.GetUserPropertyCallback() {
                    @Override
                    public void onSuccess(Object value) {
                        if (value instanceof String) {
                            title.setText(value.toString());
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        Injection.getCrashLogger().recordException(new Throwable(message));
                    }
                });
            }
        } else {
            binding.navView.getMenu().findItem(R.id.nav_item_sign_in_out).setTitle(R.string.txt_log_in);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_MY_SIGNALS) {
            Intent intent = new Intent(this, MySignalsActivity.class);
            startActivity(intent);
        }
    }

    protected abstract String getToolbarTitle();

    protected abstract int getLayoutId();
}
