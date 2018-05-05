package mikastamm.com.soundmixer.UI;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;


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
