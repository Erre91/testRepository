package francesco.workspace.homeapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListGroup_tab extends Fragment implements
		LoaderCallbacks<List<Group_App>>, DialogClick {

	private View view;
	private Event_App event;
	private ListView listView;
	private List<Group_App> groups = null;
	private Group_App group;
	private LayoutInflater inflater;
	private LoaderCallbacks<List<Group_App>> call = this;
	private String uri = null;
	private Context ctx;
	private SharedPreferences preferences;
	private String my_url = "https://usersdatapp.appspot.com/";
	private MyListAdapter_group my_adapter;
	private boolean setCategory;
	private AlertDialog levelDialog_filter;
	private CharSequence[] items_filter = { " Created by you ", " Invited ",
			" All " };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		view = inflater.inflate(R.layout.activity_people_to_event, container,
				false);
		listView = (ListView) view.findViewById(R.id.listViewPeople);
		Intent i = getActivity().getIntent();
		event = (Event_App) i.getSerializableExtra("singleEvent");
		ctx = getActivity();
		view.findViewById(R.id.no_event).setVisibility(View.INVISIBLE);
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.groups_to_event, menu);
	}

	@Override
	public void onResume() {
		super.onResume();
		view.findViewById(R.id.no_event).setVisibility(View.INVISIBLE);
		getActivity().getLoaderManager().restartLoader(1, null, this);
	}

	@Override
	public Loader<List<Group_App>> onCreateLoader(int id, Bundle args) {
		if (uri == null)
			uri = my_url + "groups?type=all";
		JsonLoaderGroup loader = new JsonLoaderGroup(getActivity(), uri);
		loader.forceLoad();
		return loader;
	}

	public void changeStateView() {
		TextView text = (TextView) view.findViewById(R.id.no_event);
		text.setText("NO GROUP AVAIBLE");
		text.setVisibility(View.VISIBLE);
	}

	@Override
	public void onLoadFinished(Loader<List<Group_App>> loader,
			List<Group_App> data) {
		if (data != null) {
			if (data.size() == 0) {
				changeStateView();
				view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
				view.findViewById(R.id.listViewPeople).setVisibility(
						View.INVISIBLE);
				return;
			}
			groups = data;
			populateListView();
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// deve variare il check mark
					Group_App ut = groups.get(position);
					int wantedPosition = position;
					int firstPosition = listView.getFirstVisiblePosition()
							- listView.getHeaderViewsCount();
					int wantedChild = wantedPosition - firstPosition;
					if (wantedChild < 0
							|| wantedChild >= listView.getChildCount()) {
						Log.w("etichetta", "Unable-.........");
						return;
					}
					// View wantedView = listView.getChildAt(wantedChild);
					// ((InvitePeople_tabbed) getActivity()).switchFragment(0,
					// ut.getKey());
					group = ut;
					addNewGroup();
				}
			});
		} else {
			return;
		}
		view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		view.findViewById(R.id.listViewPeople).setVisibility(View.VISIBLE);
	}

	private void populateListView() {
		final ArrayAdapter<Group_App> adapter_2 = new MyListAdapter_group();
		my_adapter = (MyListAdapter_group) adapter_2;
		EditText inputSearch = (EditText) view.findViewById(R.id.inputSearch);
		inputSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				// When user changed the Text
				setCategory = false;
				adapter_2.getFilter().filter(cs);
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
		Collections.sort(groups, new Comparator<Group_App>() {
			@Override
			public int compare(Group_App group1, Group_App group2) {
				return group1.getName().compareToIgnoreCase(group2.getName());
			}
		});
		ListView list = (ListView) view.findViewById(R.id.listViewPeople);
		list.setAdapter(adapter_2);
	}

	private class MyListAdapter_group extends ArrayAdapter<Group_App> implements
			Filterable {

		private ArrayFilter mFilter;
		private ArrayFilter_category cFilter;
		private ArrayList<Group_App> filteredData;
		private ArrayList<Group_App> originalData;
		private Object mLock = new Object();

		public MyListAdapter_group() {
			super(getActivity(), R.layout.item_group);
			originalData = (ArrayList<Group_App>) groups;
			filteredData = (ArrayList<Group_App>) groups;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View itemView = convertView;
			if (itemView == null) {
				itemView = inflater.inflate(R.layout.item_group, parent, false);
			}
			Group_App gr = filteredData.get(position);

			TextView nameText = (TextView) itemView
					.findViewById(R.id.item_groupName);
			nameText.setText(gr.getName());

			ImageView imageView = (ImageView) itemView
					.findViewById(R.id.group_image);

			TextView creator = (TextView) itemView
					.findViewById(R.id.item_groupCreator);
			if (gr.getCreated()) {
				creator.setText("YOURS");
				imageView.setImageResource(R.drawable.group_lavanda);
			} else {
				creator.setText(gr.getIdCreator());
				imageView.setImageResource(R.drawable.group_sakura);
			}
			return itemView;
		}

		public int getCount() {
			return filteredData.size();
		}

		public Group_App getItem(int position) {
			return filteredData.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		/**
		 * {@inheritDoc}
		 */
		public Filter getFilter() {
			if (!setCategory) {
				if (mFilter == null) {
					mFilter = new ArrayFilter();
				}
				return mFilter;
			}
			if (cFilter == null) {
				cFilter = new ArrayFilter_category();
			}
			return cFilter;
		}

		private class ArrayFilter extends Filter {
			@Override
			protected FilterResults performFiltering(CharSequence prefix) {
				FilterResults results = new FilterResults();

				if (prefix == null || prefix.length() == 0) {
					ArrayList<Group_App> list;
					synchronized (mLock) {
						list = new ArrayList<Group_App>(originalData);
					}
					results.values = list;
					results.count = list.size();
				} else {
					String prefixString = prefix.toString().toLowerCase();

					ArrayList<Group_App> values;
					synchronized (mLock) {
						values = new ArrayList<Group_App>(originalData);
					}

					final int count = values.size();
					final ArrayList<Group_App> newValues = new ArrayList<Group_App>();

					for (int i = 0; i < count; i++) {
						Group_App value = values.get(i);
						String valueText = value.getName().toLowerCase();

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

				filteredData = (ArrayList<Group_App>) results.values;
				if (results.count > 0) {
					view.findViewById(R.id.no_event).setVisibility(
							View.INVISIBLE);
					notifyDataSetChanged();
				} else {
					changeStateView();
					notifyDataSetInvalidated();
				}
			}
		}

		private class ArrayFilter_category extends Filter {
			@Override
			protected FilterResults performFiltering(CharSequence prefix) {
				FilterResults results = new FilterResults();

				if (prefix == null || prefix.length() == 0) {
					ArrayList<Group_App> list;
					synchronized (mLock) {
						list = new ArrayList<Group_App>(originalData);
					}
					results.values = list;
					results.count = list.size();
				} else {
					String prefixString = prefix.toString().toLowerCase();

					ArrayList<Group_App> values;
					synchronized (mLock) {
						values = new ArrayList<Group_App>(originalData);
					}

					final int count = values.size();
					final ArrayList<Group_App> newValues = new ArrayList<Group_App>();

					for (int i = 0; i < count; i++) {
						Group_App value = values.get(i);
						String valueText = value.getIdCreator().toLowerCase();

						if (valueText.toLowerCase().equalsIgnoreCase(
								prefixString)) {
							newValues.add(value);
						}
					}

					results.values = newValues;
					results.count = newValues.size();
				}

				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {

				filteredData = (ArrayList<Group_App>) results.values;
				if (results.count > 0) {
					view.findViewById(R.id.no_event).setVisibility(
							View.INVISIBLE);
					notifyDataSetChanged();
				} else {
					changeStateView();
					notifyDataSetInvalidated();
				}
			}
		}

	}

	@Override
	public void onLoaderReset(Loader<List<Group_App>> loader) {
		// TODO Auto-generated method stub

	}

	public void selectFilter() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select the filter for the group");
		builder.setSingleChoiceItems(items_filter, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						switch (item) {
						// created
						case 0:
							listViewChange(my_url + "groups?type=created");
							break;
						// invited
						case 1:
							listViewChange(my_url + "groups?type=invited");
							break;
						// all
						case 2:
							listViewChange(my_url + "groups?type=all");
							break;
						}
						levelDialog_filter.dismiss();
					}
				});
		levelDialog_filter = builder.create();
		levelDialog_filter.show();
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

		case R.id.filter_people:
			selectFilter();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void listViewChange(String url) {
		view.findViewById(R.id.listViewPeople).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.no_event).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
		uri = url;
		getActivity().getLoaderManager().restartLoader(1, null, call);
	}

	@Override
	public void onClickPositive() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClickNegative() {
		// TODO Auto-generated method stub

	}

	public void addNewGroup() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Invite all people in\n" + group.getName());

		// Set up the buttons
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String uri = "https://usersdatapp.appspot.com/invitation";
				new HttpTask().execute(uri);
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	private class HttpTask extends AsyncTask<String, Void, String> {
		String res = null;

		@Override
		protected String doInBackground(String... urls) {
			res = SendData(urls[0], null);
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (res == null) {
				Toast.makeText(getActivity().getBaseContext(),
						"Error: unable to execute request", Toast.LENGTH_LONG)
						.show();
				return;
			}
			if (res == null || res.equals("success"))
				Toast.makeText(getActivity().getBaseContext(),
						"Group added to your event!", Toast.LENGTH_LONG).show();
			else if (res.equals("fail::sessionFailed")) {
				Intent intent = new Intent(ctx, PrincipalActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				getActivity().finish();
				return;
			}
			if (preferences.getBoolean("email", false)) {
				getActivity().getLoaderManager().restartLoader(0, null, call);
				getActivity().findViewById(R.id.loadingPanel).setVisibility(
						View.GONE);
				return;
			}
			if (group.getUsers().size() > 0) {
				String[] arrayS = new String[group.getUsers().size()];
				int i = 0;
				for (User ut : group.getUsers()) {
					arrayS[i] = ut.email;
					i++;
				}
				new SendEmailBroadcast().execute(arrayS);
				my_adapter.getFilter().filter("");
			}
			Intent intent = getActivity().getIntent();
			getActivity().finish();
			getActivity().startActivity(intent);
		}
	}

	public String SendData(String urls, Object object) {
		String s2 = GET(urls, null);
		if (s2 == null)
			return null;
		String s1 = POST(urls, null);
		if (s1 == null || s2 == null)
			return null;
		return s1 + " " + s2;
	}

	private String readAll(Reader reader) throws IOException {
		StringBuilder builder = new StringBuilder(4096);
		for (CharBuffer buf = CharBuffer.allocate(512); (reader.read(buf)) > -1; buf
				.clear()) {
			builder.append(buf.flip());
		}
		return builder.toString();
	}

	public String GET(String url, StringEntity person) {
		DefaultHttpClient client = SetCookie.setCookie(ctx);
		String x = my_url + "participants?keyGroup=" + group.getKey()
				+ "&type=yetinvited";
		HttpGet get = new HttpGet(my_url + "participants?keyGroup="
				+ group.getKey() + "&type=yetinvited");
		HttpResponse resp;
		try {
			resp = client.execute(get);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		HttpEntity entity = resp.getEntity();
		String jsonString;
		try {
			InputStream stream = entity.getContent();
			Reader reader = new InputStreamReader(stream, "UTF-8");
			jsonString = readAll(reader);
		} catch (IOException e) {
			// TODO handle
			e.printStackTrace();
			return null;
		}
		if (jsonString != null) {
			List<Group_App> result;
			try {
				JSONArray listOfContacts = new JSONArray(jsonString);
				int len = listOfContacts.length();
				result = new ArrayList<Group_App>(len);
				for (int i = 0; i < len; i++) {
					result.add(new Group_App(listOfContacts.getJSONObject(i)));
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
			group = result.get(0);
			return "success";
		} else
			return null;
	}

	public String POST(String url, StringEntity person) {
		HttpClient httpclient = SetCookie.setCookie(ctx);
		HttpPost httpPost = new HttpPost(url);
		String respons = "";

		List<NameValuePair> pairs = null;
		for (User ut : group.getUsers()) {
			pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("keyEvent", event.getKey()));
			pairs.add(new BasicNameValuePair("idUser", ut.getEmail()));

			try {
				httpPost.setEntity(new UrlEncodedFormEntity(pairs));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}

			HttpResponse resp = null;

			try {
				resp = httpclient.execute(httpPost);
				respons = readAll(new InputStreamReader(resp.getEntity()
						.getContent(), "UTF-8"));

			} catch (IOException e) {
				Log.i("main", "NON Andata a buon fine");
				// new MessageTask().execute(this);
				return null;
				/** GESTIRE **/
			}
			ut.setClicked(false);
			ut.setInvited(true);
		}
		// newInvited.clear();
		Log.i("main", respons);
		return "success";
	}

	private class SendEmailBroadcast extends AsyncTask<String[], Void, String> {

		@Override
		protected String doInBackground(String[]... params) {
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(ctx);
			String userName = pref.getString("username", null);
			String password = pref.getString("password", null);
			if (userName == null || password == null) {
				System.out.println("Errore nelle preferences");
				System.exit(9);
			}
			EmailClient emailClient = EmailClient.getInstance(userName,
					password);
			try {
				emailClient.sendEmail(params[0], event);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return "ok";
		}
	}
}