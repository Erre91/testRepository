package francesco.workspace.homeapp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks, DialogClick {
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	Fragment frag;
	Context ctx;

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "510914454106";

	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "GCM Demo";

	TextView mDisplay;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;
	String regid;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ctx = this;
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		gcmServiceRegistration();
	}

	private void gcmServiceRegistration() {
		context = getApplicationContext();

		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);
			String rgi = regid;
			// sendRegistrationIdToBackend();

			if (regid.isEmpty()) {
				registerInBackground();
			}
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGcmPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * Gets the current registration ID for application on GCM service, if there
	 * is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGcmPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					// You should send the registration ID to your server over
					// HTTP, so it
					// can use GCM/HTTP or CCS to send messages to your app.
					sendRegistrationIdToBackend();

					// For this demo: we don't need to send it because the
					// device will send
					// upstream messages to a server that echo back the message
					// using the
					// 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

		}.execute(null, null, null);
	}

	// Send an upstream message.
	// public void onClick(final View view) {
	//
	// if (view == findViewById(R.id.send)) {
	// new AsyncTask<Void, Void, String>() {
	// @Override
	// protected String doInBackground(Void... params) {
	// String msg = "";
	// try {
	// Bundle data = new Bundle();
	// data.putString("my_message", "Hello World");
	// data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
	// String id = Integer.toString(msgId.incrementAndGet());
	// gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
	// msg = "Sent message";
	// } catch (IOException ex) {
	// msg = "Error :" + ex.getMessage();
	// }
	// return msg;
	// }
	//
	// @Override
	// protected void onPostExecute(String msg) {
	// mDisplay.append(msg + "\n");
	// }
	// }.execute(null, null, null);
	// } else if (view == findViewById(R.id.clear)) {
	// mDisplay.setText("");
	// }
	// }

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGcmPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(context.getPackageName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				HttpClient httpclient = SetCookie.setCookie(ctx);
				HttpPost request = new HttpPost(
						"https://usersdatapp.appspot.com/addnotificationinfo");
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("registrationID", regid));

				try {
					request.setEntity(new UrlEncodedFormEntity(pairs));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				StatusLine sl = null;

				try {
					HttpResponse resp = httpclient.execute(request);
					sl = resp.getStatusLine();
					// Toast.makeText(ctx, "Status Code:" + sl.getStatusCode() +
					// " and other info: " + sl.getReasonPhrase(),
					// Toast.LENGTH_LONG).show();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return sl.getStatusCode() + sl.getReasonPhrase();

			}

			@Override
			protected void onPostExecute(String result) {
				Toast.makeText(ctx, result, Toast.LENGTH_LONG);
			};
		}.execute(null, null, null);
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		Fragment frag = null;
		switch (position) {
		case 0:
			frag = new List_Invitations();
			break;
		case 1:
			frag = new ManageEventFrag();
			break;

		case 2:
			frag = new ManageGroups();
			break;

		case 3:
			startActivity(new Intent(Intent.ACTION_VIEW,
					android.net.Uri
							.parse("content://com.android.calendar/time/")));
			break;

		case 4:
			frag = new SettingsFragment();
			break;

		case 5:
			new HttpAsyncTask()
					.execute("https://usersdatapp.appspot.com/logout");
			Editor editor = preferences.edit();
			editor.remove("password");
			editor.remove("username");
			editor.commit();
			break;

		}
		if (frag == null)
			return;
		FragmentManager fragmentManager;
		fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container, frag)
				.commit();
	}

	public void setTitleActionBar(String title) {
		getActionBar().setTitle(title);
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_myEvents);
			break;
		case 2:
			mTitle = getString(R.string.title_manageEvents);
			break;
		case 3:
			mTitle = getString(R.string.title_manageGroups);
			break;
		case 4:
			mTitle = getString(R.string.title_calendar);
			break;
		case 5:
			mTitle = getString(R.string.title_settings);
			break;
		case 6:
			mTitle = getString(R.string.title_logout);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.eventlist_frag,
					container, false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String respons = null;

		@Override
		protected String doInBackground(String... strings) {
			return GET(strings[0], null);
		}

		private String GET(String url, StringEntity se) {
			DefaultHttpClient httpclient = SetCookie.setCookie(ctx);
			HttpGet httpGet = new HttpGet(url + "?regid=" + regid);
			try {
				HttpResponse resp;
				resp = httpclient.execute(httpGet);
				respons = readAll(new InputStreamReader(resp.getEntity()
						.getContent(), "UTF-8"));
			} catch (Exception e) {
				DialogFragment dialog = ConnectionDialogFragment.newInstance();
				dialog.show(getFragmentManager(), "dialog");
			}
			return null;
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			if (respons == null)
				return;
			if (respons.contains("success")) {
				Intent intent = new Intent(ctx, PrincipalActivity.class);
				startActivity(intent);
				finish();
			}
		}

		private String readAll(Reader reader) throws IOException {
			StringBuilder builder = new StringBuilder(4096);
			for (CharBuffer buf = CharBuffer.allocate(512); (reader.read(buf)) > -1; buf
					.clear()) {
				builder.append(buf.flip());
			}
			return builder.toString();
		}
	}

	@Override
	public void onClickPositive() {
		if (frag != null) {
			FragmentManager fragmentManager;
			fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.container, frag)
					.commit();
			return;
		}
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
