package francesco.workspace.homeapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Inviteds_to_Event extends Activity implements
		LoaderCallbacks<List<User>>, DialogClick {

	private Event_App event;
	private ListView listView;
	private List<User> users = null;
	public Activity act = this;
	private LayoutInflater inflater;
	private LoaderCallbacks<List<User>> call = this;
	private String uri = null;
	private Context ctx;
	private SharedPreferences preferences;
	private String my_url = "https://usersdatapp.appspot.com/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inviteds_to_event);
		listView = (ListView) findViewById(R.id.listViewPeople_invited);
		inflater = LayoutInflater.from(this);
		Intent i = getIntent();
		event = (Event_App) i.getSerializableExtra("singleEvent");
		ctx = this;
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		uri = my_url + "invitedpeople?keyEvent=" + event.getKey();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.justmail, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<List<User>> onCreateLoader(int id, Bundle args) {
		findViewById(R.id.listViewPeople_invited).setVisibility(View.INVISIBLE);
		findViewById(R.id.loadingPanel_invited).setVisibility(View.VISIBLE);
		JsonLoaderUsers loader = new JsonLoaderUsers(this, uri, 1);
		loader.forceLoad();
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<List<User>> loader, List<User> data) {
		if (data == null)
			return;
		if (data != null) {

			if (data.size() == 0) {
				findViewById(R.id.loadingPanel_invited)
						.setVisibility(View.GONE);
				findViewById(R.id.listViewPeople_invited).setVisibility(
						View.VISIBLE);
				findViewById(R.id.no_people).setVisibility(View.VISIBLE);
				return;
			}
			findViewById(R.id.no_people).setVisibility(View.INVISIBLE);
			users = data;
			populateListView();
		}
		findViewById(R.id.loadingPanel_invited).setVisibility(View.GONE);
		findViewById(R.id.listViewPeople_invited).setVisibility(View.VISIBLE);
	}

	private void populateListView() {
		final ArrayAdapter<User> adapter = new MyListAdapter_invitedPeople();
		EditText inputSearch = (EditText) findViewById(R.id.inputSearch_invited);
		inputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				// When user changed the Text
				adapter.getFilter().filter(cs);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}
		});
		Collections.sort(users, new Comparator<User>() {
			@Override
			public int compare(User user1, User user2) {
				int res = user1.getCognome().compareToIgnoreCase(
						user2.getCognome());
				if (res != 0)
					return res;
				return user1.getNome().compareToIgnoreCase(user2.getNome());
			}
		});
		ListView list = (ListView) findViewById(R.id.listViewPeople_invited);
		list.setAdapter(adapter);
	}

	private class MyListAdapter_invitedPeople extends ArrayAdapter<User>
			implements Filterable {

		private ArrayFilter mFilter;
		private ArrayList<User> filteredData;
		private ArrayList<User> originalData;
		private Object mLock = new Object();

		public MyListAdapter_invitedPeople() {
			super(act, R.layout.item_people);
			originalData = (ArrayList<User>) users;
			filteredData = (ArrayList<User>) users;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View itemView = convertView;
			if (itemView == null) {
				itemView = inflater
						.inflate(R.layout.item_people, parent, false);
			}
			User ut = filteredData.get(position);

			ImageView imageInvitateView = (ImageView) itemView
					.findViewById(R.id.invited_img);
			if (ut.getTipoUtente().equalsIgnoreCase("student"))
				imageInvitateView.setImageResource(R.drawable.student_black);
			else if (ut.getTipoUtente().equalsIgnoreCase("secretary"))
				imageInvitateView.setImageResource(R.drawable.female_black);
			else if (ut.getTipoUtente().equalsIgnoreCase("professor"))
				imageInvitateView.setImageResource(R.drawable.people_black);
			else if (ut.getTipoUtente().equalsIgnoreCase("guest"))
				imageInvitateView.setImageResource(R.drawable.guest_black_best);

			TextView nameText = (TextView) itemView
					.findViewById(R.id.item_peopleName);
			nameText.setText(ut.getCognome() + " " + ut.getNome());

			TextView tipo = (TextView) itemView
					.findViewById(R.id.item_peopleGroup);
			tipo.setText(ut.getTipoUtente().toUpperCase());

			ImageView imageView = (ImageView) itemView
					.findViewById(R.id.item_status);
			imageView.setVisibility(View.INVISIBLE);

			return itemView;
		}

		public int getCount() {
			return filteredData.size();
		}

		public User getItem(int position) {
			return filteredData.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		/**
		 * {@inheritDoc}
		 */
		public Filter getFilter() {
			if (mFilter == null) {
				mFilter = new ArrayFilter();
			}
			return mFilter;
		}

		private class ArrayFilter extends Filter {
			@Override
			protected FilterResults performFiltering(CharSequence prefix) {
				FilterResults results = new FilterResults();

				if (prefix == null || prefix.length() == 0) {
					ArrayList<User> list;
					synchronized (mLock) {
						list = new ArrayList<User>(originalData);
					}
					results.values = list;
					results.count = list.size();
				} else {
					String prefixString = prefix.toString().toLowerCase();

					ArrayList<User> values;
					synchronized (mLock) {
						values = new ArrayList<User>(originalData);
					}

					final int count = values.size();
					final ArrayList<User> newValues = new ArrayList<User>();

					for (int i = 0; i < count; i++) {
						User value = values.get(i);
						String valueText = value.getCognome().toLowerCase();

						if (valueText.toLowerCase().contains(prefixString)) {
							newValues.add(value);
						}

						// // First match against the whole, non-splitted value
						// if (valueText.startsWith(prefixString)) {
						// newValues.add(value);
						// } else {
						// final String[] words = valueText.split(" ");
						// final int wordCount = words.length;
						//
						// // Start at index 0, in case valueText starts with
						// // space(s)
						// for (int k = 0; k < wordCount; k++) {
						// if (words[k].startsWith(prefixString)) {
						// newValues.add(value);
						// break;
						// }
						// }
						// }
					}

					results.values = newValues;
					results.count = newValues.size();
				}

				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {

				filteredData = (ArrayList<User>) results.values;
				if (results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		}

	}

	@Override
	public void onLoaderReset(Loader<List<User>> loader) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClickPositive() {
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	@Override
	public void onClickNegative() {
		finish();
		System.exit(0);
	}

	// private void sendEmail() {
	// Intent intent = new Intent(Intent.ACTION_SEND);
	// String[] arrayS = new String[newInvited.size()];
	// int i = 0;
	// for (User ut : newInvited) {
	// arrayS[i] = ut.email;
	// i++;
	// }
	// intent.putExtra(Intent.EXTRA_EMAIL, arrayS);
	// intent.putExtra(Intent.EXTRA_SUBJECT, event.getName());
	// intent.putExtra(Intent.EXTRA_TEXT, "I'm email body.");
	// intent.setType("message/rfc822");
	// startActivity(Intent.createChooser(intent, "Send Email"));
	// }
}
