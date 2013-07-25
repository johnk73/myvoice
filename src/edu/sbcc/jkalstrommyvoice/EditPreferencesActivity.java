package edu.sbcc.jkalstrommyvoice;

import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.*;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;

public class EditPreferencesActivity extends PreferenceActivity implements OnPreferenceChangeListener {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		String[] prefNames = new String[] { "username" };

		Preference p = null;

		SharedPreferences prefs = getSharedPreferences("myVoice", 0);

		// Set this object to be the listener for changes to any preference. Also set the preference summary values
		for (String prefName : prefNames) {
			p = findPreference(prefName);
			p.setOnPreferenceChangeListener(this);
			String s = prefs.getString(prefName, "");
			setPreferenceSummary(p, s);
		}
	}

	/**
	 * Changes the summary property of the given preference.
	 */
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		setPreferenceSummary(preference, newValue.toString());
		SharedPreferences prefs = getSharedPreferences("myVoice", 0);
		Editor editor = prefs.edit();
		editor.putString(preference.getKey(), newValue.toString());
		editor.commit();
		return true;
	}

	private void setPreferenceSummary(Preference preference, String newSummary) {

		if (preference.getKey().equalsIgnoreCase("username")) {
			if ((newSummary == null) || (newSummary.equals("")))
				newSummary = "Username"; // initial value
		}

		preference.setSummary(newSummary);
	}
}
