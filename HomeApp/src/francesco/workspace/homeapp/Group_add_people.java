package francesco.workspace.homeapp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Group_add_people extends Activity implements
		LoaderCallbacks<List<Group_App>>, DialogClick {

	private ListView listView;
	private LayoutInflater inflater;
	private List<User> users;
	private List<User> newInvited = new ArrayList<User>();
	private LoaderCallbacks<List<Group_App>> call = this;
	private String uri = null;
	private Context ctx;
	private SharedPreferences preferences;
	private String my_url = "https://usersdatapp.appspot.com/";
	private MyListAdapter_user my_adapter;
	private boolean setCategory;
	private AlertDialog levelDialog_category;
	private CharSequence[] items_category = { " Secretary ", " Professor ",
			" Director ", " Student ", " Guest " };

	private Group_App my_group;
	private List<Group_App> group;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_people_to_event);
		listView = (ListView) findViewById(R.id.listViewPeople);
		inflater = LayoutInflater.from(this);
		Intent i = getIntent();
		my_group = (Group_App) i.getSerializableExtra("singleGroup");
		ctx = this;
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		ImageButton button = (ImageButton) findViewById(R.id.save);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.listViewPeople).setVisibility(View.INVISIBLE);
				findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
				new HttpTask().execute(my_url);
			}
		});
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_people_to_group, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<List<Group_App>> onCreateLoader(int id, Bundle args) {
		if (uri == null)
			uri = my_url + "participants?keyGroup=" + my_group.getKey()
					+ "&type=notinvited";
		JsonLoaderGroup loader = new JsonLoaderGroup(this, uri);
		loader.forceLoad();
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<List<Group_App>> loader,
			List<Group_App> data) {
		if (data != null) {
			group = data;
			users = group.get(0).getUsers();
			populateListView();
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// deve variare il check mark
					User ut = my_adapter.getItem(position);
					int wantedPosition = position;
					int firstPosition = listView.getFirstVisiblePosition()
							- listView.getHeaderViewsCount();
					int wantedChild = wantedPosition - firstPosition;
					if (wantedChild < 0
							|| wantedChild >= listView.getChildCount()) {
						Log.w("etichetta", "Unable-.........");
						return;
					}
					View wantedView = listView.getChildAt(wantedChild);
					ImageView imageView = (ImageView) wantedView
							.findViewById(R.id.item_status);
					if (ut.isClicked()) {
						newInvited.remove(ut);
						ut.setClicked(false);
						imageView.setVisibility(View.INVISIBLE);
					} else {
						newInvited.add(ut);
						ut.setClicked(true);
						imageView.setImageResource(R.drawable.red_c_mark);
						imageView.setVisibility(View.VISIBLE);
					}
				}
			});
		} else {
			return;
		}

		findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		findViewById(R.id.listViewPeople).setVisibility(View.VISIBLE);
	}

	private void populateListView() {
		final ArrayAdapter<User> adapter = new MyListAdapter_user();
		my_adapter = (MyListAdapter_user) adapter;
		EditText inputSearch = (EditText) findViewById(R.id.inputSearch);
		inputSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				// When user changed the Text
				setCategory = false;
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
		ListView list = (ListView) findViewById(R.id.listViewPeople);
		list.setAdapter(adapter);
	}

	private class MyListAdapter_user extends ArrayAdapter<User> implements
			Filterable {

		private ArrayFilter mFilter;
		private ArrayFilter_category cFilter;
		private ArrayList<User> filteredData;
		private ArrayList<User> originalData;
		private Object mLock = new Object();

		public MyListAdapter_user() {
			super(ctx, R.layout.item_people);
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
			if (ut.isClicked()) {
				imageView.setImageResource(R.drawable.red_c_mark);
				imageView.setVisibility(View.VISIBLE);
			} else
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

		private class ArrayFilter_category extends Filter {
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
						String valueText = value.getTipoUtente().toLowerCase();

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
	public void onLoaderReset(Loader<List<Group_App>> loader) {
		// TODO Auto-generated method stub

	}

	public void selectCategory() {
		if (my_adapter == null)
			return;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select the category for the people");
		builder.setSingleChoiceItems(items_category, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						switch (item) {
						case 0:
							setCategory = true;
							my_adapter.getFilter().filter("secretary");
							break;
						case 1:
							setCategory = true;
							my_adapter.getFilter().filter("professor");

							break;
						case 2:
							setCategory = true;
							my_adapter.getFilter().filter("director");
							break;
						case 3:
							setCategory = true;
							my_adapter.getFilter().filter("student");
							break;
						case 4:
							setCategory = true;
							my_adapter.getFilter().filter("guest");
							break;
						}
						levelDialog_category.dismiss();
					}
				});
		levelDialog_category = builder.create();
		levelDialog_category.show();

	}

	private void deselectAll_people() {
		if (users == null || my_adapter == null)
			return;
		if (newInvited != null) {
			for (User u : newInvited) {
				u.setClicked(false);
			}
			newInvited.clear();
		}
		my_adapter.notifyDataSetChanged();
	}

	private void selectAll_people() {
		if (users == null || my_adapter == null)
			return;
		if (newInvited != null) {
			for (User u : newInvited) {
				u.setClicked(false);
			}
			newInvited.clear();
		}
		for (User u : users) {
			if (!u.isInvited()) {
				u.setClicked(true);
				newInvited.add(u);
			}
		}
		my_adapter.notifyDataSetChanged();
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

		case R.id.category_people:
			selectCategory();
			return true;

		case R.id.selectAll_people:
			selectAll_people();
			return true;

		case R.id.deselectAll_people:
			deselectAll_people();
			return true;

		case R.id.all_people:
			setCategory = true;
			my_adapter.getFilter().filter("");
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void peopleViewChange(String url) {
		findViewById(R.id.listViewPeople).setVisibility(View.INVISIBLE);
		findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
		uri = url;
		getLoaderManager().restartLoader(0, null, call);
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
				Toast.makeText(getBaseContext(),
						"Error: unable to execute request", Toast.LENGTH_LONG)
						.show();
				return;
			}
			if (res.equals("success"))
				Toast.makeText(getBaseContext(), "People added!",
						Toast.LENGTH_LONG).show();
			else if (res.equals("fail::sessionFailed")) {
				Intent intent = new Intent(ctx, PrincipalActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				return;
			}
			if (newInvited.size() > 0) {
				newInvited.clear();
				my_adapter.getFilter().filter("");
			}
			getLoaderManager().restartLoader(0, null, call);
			findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		}
	}

	public String SendData(String urls, Object object) {
		String s1 = POST(urls, null);
		if (s1 == null)
			return null;
		return s1;
	}

	private String readAll(Reader reader) throws IOException {
		StringBuilder builder = new StringBuilder(4096);
		for (CharBuffer buf = CharBuffer.allocate(512); (reader.read(buf)) > -1; buf
				.clear()) {
			builder.append(buf.flip());
		}
		return builder.toString();
	}

	public String POST(String url, StringEntity person) {
		HttpClient httpclient = SetCookie.setCookie(ctx);
		HttpPost httpPost = new HttpPost(url + "participants");
		String respons = "";

		List<NameValuePair> pairs = null;
		for (User ut : newInvited) {
			pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("group_name", my_group.getName()));
			pairs.add(new BasicNameValuePair("new_user", ut.getEmail()));
			pairs.add(new BasicNameValuePair("type", "add"));
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
}
