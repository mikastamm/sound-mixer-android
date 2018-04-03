package mikastamm.com.soundmixer.UI;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;


import mikastamm.com.soundmixer.R;

/**
 * Created by Mika on 03.04.2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_preferences);
    }
}
