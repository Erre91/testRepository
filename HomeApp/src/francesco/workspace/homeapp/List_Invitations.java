package francesco.workspace.homeapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class List_Invitations extends Fragment implements
		LoaderCallbacks<List<Event_App>> {

	View rootView;
	private String url = "https://usersdatapp.appspot.com/confirmevent";
	LayoutInflater inflater;
	Activity activity;
	private List<Event_App> myEvents = new ArrayList<Event_App>();
	private MyListAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		activity = this.getActivity();
		rootView = inflater
				.inflate(R.layout.list_invitations, container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().findViewById(R.id.no_event).setVisibility(View.INVISIBLE);
		registerClickCallback();
		((MainActivity) getActivity()).setTitleActionBar("My invitations");
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		((MainActivity) getActivity()).setTitleActionBar("My invitations");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("main", "onResume");
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<List<Event_App>> onCreateLoader(int id, Bundle args) {
		Log.i("main", "creating loader");
		JsonLoaderEvent_app loader = new JsonLoaderEvent_app(
				this.getActivity(), url);
		loader.forceLoad();
		return loader;
	}

	@Override
	public void onLoadFinished(
			Loader<List<francesco.workspace.homeapp.Event_App>> loader,
			List<francesco.workspace.homeapp.Event_App> data) {
		if (data == null) {
			return;
		}
		if (data.size() == 0) {
			TextView tx = (TextView) getActivity().findViewById(R.id.no_event);
			tx.setVisibility(View.VISIBLE);
		} else {
			TextView tx = (TextView) getActivity().findViewById(R.id.no_event);
			tx.setVisibility(View.INVISIBLE);
			myEvents = data;
			populateListView();
		}
		if (data.size() == 0) {
			getActivity().findViewById(R.id.no_event).setVisibility(
					View.VISIBLE);
		}
		getActivity().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		((MainActivity) getActivity()).setTitleActionBar("My invitations");
	}

	private void populateListView() {
		adapter = new MyListAdapter(getActivity(), R.layout.item_view, true);
		// ordino la lista degli eventi sulla data crescente
		Collections.sort(myEvents, new Comparator<Event_App>() {
			@Override
			public int compare(Event_App ea1, Event_App ea2) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				Date date1 = null;
				Date date2 = null;
				try {
					date1 = sdf.parse(ea1.getData());
					date2 = sdf.parse(ea2.getData());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return date1.compareTo(date2);
			}
		});
		for (int i = 0; i < myEvents.size(); i++) {
			Log.i("list", myEvents.get(i).getData());
			if (i == 0) {
				adapter.addEtichetta(myEvents.get(i));
				adapter.add(myEvents.get(i));
			} else {
				if (myEvents.get(i).getData()
						.equals(myEvents.get(i - 1).getData()))
					adapter.add(myEvents.get(i));
				else {
					adapter.addEtichetta(myEvents.get(i));
					adapter.add(myEvents.get(i));
				}
			}
		}
		ListView list = (ListView) getView().findViewById(R.id.eventListView);
		list.setDivider(null);
		list.setDividerHeight(1);
		list.setAdapter(adapter);
	}

	private void registerClickCallback() {
		ListView list = (ListView) getView().findViewById(R.id.eventListView);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View viewClicked,
					int position, long id) {
				// Toast.makeText(getActivity(), message, Toast.LENGTH_LONG)
				// .show();
				Intent intent = new Intent(getActivity(),
						Event_details_invitation.class);
				intent.putExtra("singleEvent", adapter.getEvent(position));
				startActivity(intent);
			}
		});
	}

	@Override
	public void onLoaderReset(
			Loader<List<francesco.workspace.homeapp.Event_App>> loader) {
		// TODO Auto-generated method stub

	}
}
