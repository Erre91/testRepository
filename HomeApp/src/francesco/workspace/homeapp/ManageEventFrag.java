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
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class ManageEventFrag extends Fragment implements
		LoaderCallbacks<List<Event_App>> {

	View rootView;
	private List<Event_App> myEvents = new ArrayList<Event_App>();
	LayoutInflater inflater;
	Activity activity;
	private MyListAdapterEvent adapter;
	private Context ctx;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		this.inflater = inflater;
		activity = this.getActivity();
		rootView = inflater.inflate(R.layout.manage_event_frag, container,
				false);
		ctx = getActivity();
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.manage_events_menu, menu);
		// super.onCreateOptionsMenu(menu, inflater);
		((MainActivity) getActivity()).setTitleActionBar("Your events");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_event:
			Intent intent = new Intent(getActivity(), NewEventActivity.class);
			startActivity(intent);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerClickCallback();
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<List<Event_App>> onCreateLoader(int id, Bundle args) {
		String uri = "https://usersdatapp.appspot.com/event";
		JsonLoaderEvent_app loader = new JsonLoaderEvent_app(getActivity(), uri);
		loader.forceLoad();
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<List<Event_App>> loader,
			List<Event_App> data) {
		if (data == null) {
			return;
		} else {
			if (data.size() == 0) {
				TextView tx = (TextView) getActivity().findViewById(
						R.id.no_event);
				tx.setVisibility(View.VISIBLE);
			} else {
				TextView tx = (TextView) getActivity().findViewById(
						R.id.no_event);
				tx.setVisibility(View.INVISIBLE);
				myEvents = data;
				populateListView();
			}
			if (data.size() == 0) {
				getActivity().findViewById(R.id.no_event).setVisibility(
						View.VISIBLE);
			} else {
				getActivity().findViewById(R.id.eventListView2).setVisibility(
						View.VISIBLE);
			}
			getActivity().findViewById(R.id.loadingPanel2).setVisibility(
					View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<Event_App>> loader) {
		// TODO Auto-generated method stub
	}

	private void populateListView() {
		adapter = new MyListAdapterEvent(getActivity(), R.layout.item_event,
				false);
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
		ListView list = (ListView) getView().findViewById(R.id.eventListView2);
		list.setDivider(null);
		list.setDividerHeight(1);
		list.setAdapter(adapter);
	}

	private void registerClickCallback() {
		ListView list = (ListView) getView().findViewById(R.id.eventListView2);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View viewClicked,
					int position, long id) {
				// Toast.makeText(getActivity(), message, Toast.LENGTH_LONG)
				// .show();
				Intent intent = new Intent(getActivity(),
						DetailsEventActivity.class);
				intent.putExtra("singleEvent", adapter.getEvent(position));
				startActivity(intent);
			}
		});
	}
}
