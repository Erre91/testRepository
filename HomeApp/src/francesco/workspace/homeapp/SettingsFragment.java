package francesco.workspace.homeapp;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;

public class SettingsFragment extends PreferenceFragment {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		((MainActivity) getActivity()).setTitleActionBar("Settings");
	}
}
