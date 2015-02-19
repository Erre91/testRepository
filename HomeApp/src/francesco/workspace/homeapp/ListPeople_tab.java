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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import android.app.ActionBar;
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
import francesco.workspace.homeapp.InvitePeople_tabbed.Callback;

public class ListPeople_tab extends Fragment implements
		LoaderCallbacks<List<User>>, DialogClick, Callback {

	private View view;
	private Event_App event;
	private ListView listView;
	private List<User> users = null;
	// public Activity act = getActivity();
	LayoutInflater inflater;
	List<User> cancInvited = new ArrayList<User>();
	List<User> newInvited = new ArrayList<User>();
	private LoaderCallbacks<List<User>> call = this;
	String uri = null;
	Context ctx;
	SharedPreferences preferences;
	private String my_url = "https://usersdatapp.appspot.com/";
	private MyListAdapter_user my_adapter;
	private boolean setCategory;
	private AlertDialog levelDialog_category;
	private CharSequence[] items_category = { " Secretary ", " Professor ",
			" Director ", " Student ", " Guest " };
	private AlertDialog levelDialog_filter;
	private CharSequence[] items_filter = { " All people ",
			" Already invited ", " Not yet invited ", " Accepted ", " Refused " };

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
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		view.findViewById(R.id.no_event).setVisibility(View.INVISIBLE);
		ImageButton button = (ImageButton) view.findViewById(R.id.save);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				view.findViewById(R.id.no_event).setVisibility(View.INVISIBLE);
				view.findViewById(R.id.listViewPeople).setVisibility(
						View.INVISIBLE);
				view.findViewById(R.id.loadingPanel)
						.setVisibility(View.VISIBLE);
				new HttpTask().execute(my_url);
			}
		});

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.people_to_event, menu);
		getActivity().getActionBar().setDisplayOptions(
				ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE
						| ActionBar.DISPLAY_SHOW_CUSTOM);
		getActivity().getActionBar().setTitle("   Invite your friends!");
	}

	@Override
	public void onResume() {
		super.onResume();
		view.findViewById(R.id.no_event).setVisibility(View.INVISIBLE);
		getActivity().getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<List<User>> onCreateLoader(int id, Bundle args) {
		if (uri == null)
			uri = my_url + "invitation?keyEvent=" + event.getKey();
		JsonLoaderUsers loader = new JsonLoaderUsers(getActivity(), uri, 0);
		loader.forceLoad();
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<List<User>> loader, List<User> data) {
		if (data != null) {
			users = data;
			populateListView();
			if (data.size() == 0) {
				changeStateView();
				view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
				view.findViewById(R.id.listViewPeople).setVisibility(
						View.INVISIBLE);
				return;
			}
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// deve variare il check mark
					User ut = users.get(position);
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
					if (ut.isInvited() && !ut.isClicked()) {
						cancInvited.add(ut);
						ut.setClicked(true);
						imageView.setImageResource(R.drawable.cancel_red);
						imageView.setVisibility(View.VISIBLE);
					} else if (ut.isInvited() && ut.isClicked()) {
						cancInvited.remove(ut);
						ut.setClicked(false);
						imageView.setImageResource(R.drawable.blue_c_mark);
						imageView.setVisibility(View.VISIBLE);
					} else if (ut.isClicked()) {
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

		view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
		view.findViewById(R.id.listViewPeople).setVisibility(View.VISIBLE);
	}

	private void populateListView() {
		final ArrayAdapter<User> adapter = new MyListAdapter_user();
		my_adapter = (MyListAdapter_user) adapter;
		EditText inputSearch = (EditText) view.findViewById(R.id.inputSearch);
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
		ListView list = (ListView) view.findViewById(R.id.listViewPeople);
		list.setAdapter(adapter);
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

		protected void onPostExecute(String result) {
			if (result == null) {
				Toast.makeText(ctx, "Impossible send email", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(ctx, "Email successfully sent",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void changeStateView() {
		TextView text = (TextView) view.findViewById(R.id.no_event);
		text.setText("NO PEOPLE AVAIBLE");
		text.setVisibility(View.VISIBLE);
	}

	private class MyListAdapter_user extends ArrayAdapter<User> implements
			Filterable {

		private ArrayFilter mFilter;
		private ArrayFilter_category cFilter;
		private ArrayList<User> filteredData;
		private ArrayList<User> originalData;
		private Object mLock = new Object();

		public MyListAdapter_user() {
			super(getActivity(), R.layout.item_people);
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
			if (ut.isInvited() && !ut.isClicked()) {
				imageView.setImageResource(R.drawable.blue_c_mark);
				imageView.setVisibility(View.VISIBLE);
			} else if (ut.isInvited() && ut.isClicked()) {
				imageView.setImageResource(R.drawable.cancel_red);
				imageView.setVisibility(View.VISIBLE);
			} else if (ut.isClicked()) {
				imageView.setImageResource(R.drawable.red_c_mark);
				imageView.setVisibility(View.VISIBLE);
			} else {
				imageView.setVisibility(View.INVISIBLE);
			}
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

		public int getItemPosition(User user_t) {
			int sizeFiltered = filteredData.size();
			for (int i = 0; i < sizeFiltered; i++) {
				if (filteredData.get(i).getEmail().equals(user_t.getEmail()))
					return i;
			}
			return -1;
		}

		public View getViewByPosition(int pos, ListView listView) {
			final int firstListItemPosition = listView
					.getFirstVisiblePosition();
			final int lastListItemPosition = firstListItemPosition
					+ listView.getChildCount() - 1;

			if (pos < firstListItemPosition || pos > lastListItemPosition) {
				return listView.getAdapter().getView(pos, null, listView);
			} else {
				final int childIndex = pos - firstListItemPosition;
				return listView.getChildAt(childIndex);
			}
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
	public void onLoaderReset(Loader<List<User>> loader) {
		// TODO Auto-generated method stub

	}

	public void selectFilter() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select the filter for the people");
		builder.setSingleChoiceItems(items_filter, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						switch (item) {
						// all
						case 0:
							peopleViewChange(my_url + "invitation?keyEvent="
									+ event.getKey());
							break;
						// invited
						case 1:
							peopleViewChange(my_url + "invitation?keyEvent="
									+ event.getKey() + "&state=invited");

							break;
						// not invited
						case 2:
							peopleViewChange(my_url + "invitation?keyEvent="
									+ event.getKey() + "&state=notinvited");
							break;
						// accepted
						case 3:
							peopleViewChange(my_url + "invitation?keyEvent="
									+ event.getKey() + "&state=accepted");
							break;
						// refused
						case 4:
							peopleViewChange(my_url + "invitation?keyEvent="
									+ event.getKey() + "&state=refused");
							break;
						}
						levelDialog_filter.dismiss();
					}
				});
		levelDialog_filter = builder.create();
		levelDialog_filter.show();
	}

	public void selectCategory() {
		if (my_adapter == null || users.size() == 0)
			return;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

		case R.id.category_people:
			selectCategory();
			return true;

		case R.id.selectAll_people:
			selectAll_people();
			return true;

		case R.id.deselectAll_people:
			deselectAll_people();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// public void clickSave(View viewX) {
	// viewX.findViewById(R.id.listViewPeople).setVisibility(View.INVISIBLE);
	// viewX.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
	// new HttpTask().execute(my_url);
	// }

	public void peopleViewChange(String url) {
		view.findViewById(R.id.listViewPeople).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.no_event).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
		uri = url;
		getActivity().getLoaderManager().restartLoader(0, null, call);
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
						"Contact saved!", Toast.LENGTH_LONG).show();
			else if (res.equals("fail::sessionFailed")) {
				Intent intent = new Intent(ctx, PrincipalActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				getActivity().finish();
				return;
			}
			if (preferences.getBoolean("email", false)) {
				newInvited.clear();
				getActivity().getLoaderManager().restartLoader(0, null, call);
				getActivity().findViewById(R.id.loadingPanel).setVisibility(
						View.GONE);
				return;
			}
			if (newInvited.size() > 0) {
				String[] arrayS = new String[newInvited.size()];
				int i = 0;
				for (User ut : newInvited) {
					arrayS[i] = ut.email;
					i++;
				}
				new SendEmailBroadcast().execute(arrayS);
				newInvited.clear();
				my_adapter.getFilter().filter("");
			}
			Intent intent = getActivity().getIntent();
			getActivity().finish();
			getActivity().startActivity(intent);
		}
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

	public String SendData(String urls, Object object) {
		String s1 = POST(urls, null);
		String s2 = GET(urls, null);
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

	public String POST(String url, StringEntity person) {
		HttpClient httpclient = SetCookie.setCookie(ctx);
		HttpPost httpPost = new HttpPost(url + "invitation");
		String respons = "";

		List<NameValuePair> pairs = null;
		for (User ut : newInvited) {
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

	public String GET(String url, StringEntity person) {
		HttpClient httpclient = SetCookie.setCookie(ctx);

		for (User ut : cancInvited) {
			String urlFinish = url + "delete?keyEvent=" + event.getKey()
					+ "&invited=" + ut.getEmail();

			HttpGet httpPost = new HttpGet(urlFinish);

			HttpResponse resp;
			try {
				resp = httpclient.execute(httpPost);
			} catch (IOException e) {
				Log.i("main", "NON Andata a buon fine");
				return null;
			}
			ut.setClicked(false);
			ut.setInvited(false);
			Log.i("main", "Andata a buon fine");
		}
		cancInvited.clear();
		return "success";
	}

	@Override
	public void onClickPositive() {
		Intent intent = getActivity().getIntent();
		getActivity().finish();
		startActivity(intent);
	}

	@Override
	public void onClickNegative() {
		getActivity().finish();
		System.exit(0);
	}

	@Override
	public void doSomething(Object object) {
		// TODO: aggiungi un attributo che dica quali sono i filtri da settare
		// in fase di onResume() !!!
		// Toast.makeText(getActivity(), object.toString(),
		// Toast.LENGTH_LONG).show();
	}
}