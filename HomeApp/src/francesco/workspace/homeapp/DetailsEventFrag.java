package francesco.workspace.homeapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import francesco.workspace.homeapp.R;

public class DetailsEventFrag extends Fragment {

	View rootView;
	LayoutInflater inflater;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		rootView = inflater.inflate(R.layout.detailsevent_frag, container);
		return rootView;
	}
}
