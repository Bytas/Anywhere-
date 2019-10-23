package com.absinthe.anywhere_.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.absinthe.anywhere_.AnywhereApplication;
import com.absinthe.anywhere_.R;
import com.absinthe.anywhere_.model.Const;
import com.absinthe.anywhere_.model.GlobalValues;
import com.absinthe.anywhere_.ui.main.MainFragment;
import com.absinthe.anywhere_.utils.ShortcutsUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;


public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListPreference workingModePreference = findPreference(Const.SP_KEY_WORKING_MODE);
        ListPreference darkModePreference = findPreference(Const.SP_KEY_DARK_MODE);
        Preference changeBgPreference = findPreference(Const.SP_KEY_CHANGE_BACKGROUND);
        Preference resetBgPreference = findPreference(Const.SP_KEY_RESET_BACKGROUND);
        Preference helpPreference = findPreference(Const.SP_KEY_HELP);
        Preference clearShortcutsPreference = findPreference(Const.SP_KEY_CLEAR_SHORTCUTS);

        if (workingModePreference != null) {
            workingModePreference.setOnPreferenceChangeListener(this);
        }
        if (changeBgPreference != null) {
            changeBgPreference.setOnPreferenceClickListener(this);
        }
        if (resetBgPreference != null) {
            resetBgPreference.setOnPreferenceClickListener(this);
        }
        if (darkModePreference != null) {
            darkModePreference.setOnPreferenceChangeListener(this);
        }
        if (helpPreference != null) {
            helpPreference.setOnPreferenceClickListener(this);
        }
        if (clearShortcutsPreference != null) {
            clearShortcutsPreference.setOnPreferenceClickListener(this);
        }

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case Const.SP_KEY_CHANGE_BACKGROUND:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                SettingsActivity.getInstance().startActivityForResult(intent, Const.REQUEST_CODE_IMAGE_CAPTURE);
                break;
            case Const.SP_KEY_RESET_BACKGROUND:
                new MaterialAlertDialogBuilder(SettingsActivity.getInstance())
                        .setTitle(R.string.dialog_reset_background_confirm_title)
                        .setMessage(R.string.dialog_reset_background_confirm_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_delete_positive_button, (dialogInterface, i) -> MainFragment.getViewModelInstance().getBackground().setValue(""))
                        .setNegativeButton(R.string.dialog_delete_negative_button,
                                (dialogInterface, i) -> { })
                        .show();
                break;
            case Const.SP_KEY_HELP:
                String url = "https://zhaobozhen.github.io/Anywhere-Docs/";
                CustomTabsIntent tabsIntent = new CustomTabsIntent.Builder()
                        .build();
                tabsIntent.launchUrl(Objects.requireNonNull(getActivity()), Uri.parse(url));
                break;
            case Const.SP_KEY_CLEAR_SHORTCUTS:
                new MaterialAlertDialogBuilder(SettingsActivity.getInstance())
                        .setTitle(R.string.dialog_reset_background_confirm_title)
                        .setMessage(R.string.dialog_reset_shortcuts_confirm_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_delete_positive_button, (dialogInterface, i) -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                ShortcutsUtil.clearShortcuts();
                            }
                        })
                        .setNegativeButton(R.string.dialog_delete_negative_button,
                                (dialogInterface, i) -> { })
                        .show();
                break;
            default:
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case Const.SP_KEY_WORKING_MODE:
                if (MainFragment.getViewModelInstance() != null) {
                    MainFragment.getViewModelInstance().getWorkingMode().setValue(newValue.toString());
                }
                break;
            case Const.SP_KEY_DARK_MODE:
                AnywhereApplication.setTheme(newValue.toString());
                GlobalValues.setsDarkMode(newValue.toString());
                break;
            default:
        }
        return true;
    }
}
