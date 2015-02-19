package francesco.workspace.homeapp;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

public class NewEventActivity extends Activity implements LocationListener,
		ConnectionCallbacks, OnConnectionFailedListener {

	String gps_coordinates;
	private EditText nameEvent;
	private EditText dateEvent;
	private EditText descriptionEvent;
	private EditText timeEvent;
	private EditText placeEvent;
	Context ctx;

	GoogleApiClient mGoogleApiClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_event_layout);
		nameEvent = (EditText) findViewById(R.id.new_eventName);
		dateEvent = (EditText) findViewById(R.id.new_eventDate);
		timeEvent = (EditText) findViewById(R.id.new_eventTime);
		placeEvent = (EditText) findViewById(R.id.new_eventPlace);
		descriptionEvent = (EditText) findViewById(R.id.new_eventDescription);
		ctx = this;
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();

		mGoogleApiClient.connect();
	}

	private void addCalendar() {
		Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra(Events.TITLE, nameEvent.getText().toString());
		intent.putExtra(Events.EVENT_LOCATION, placeEvent.getText().toString());
		intent.putExtra(Events.DESCRIPTION, "to do");

		// Setting dates
		String dateCalendar = dateEvent.getText().toString() + "-"
				+ timeEvent.getText().toString();
		Date dateX = null;
		try {
			System.out.println(dateCalendar);
			dateX = new SimpleDateFormat("dd/MM/yyyy-HH:mm")
					.parse(dateCalendar);
			System.out.println(dateX);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GregorianCalendar calDate = new GregorianCalendar();
		calDate.setTime(dateX);
		intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
				calDate.getTimeInMillis());
		intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
				calDate.getTimeInMillis());

		// make it a full day event
		intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);

		// make it a recurring Event
		// intent.putExtra(Events.RRULE,
		// "FREQ=WEEKLY;COUNT=11;WKST=SU;BYDAY=TU,TH");

		// Making it private and shown as busy
		intent.putExtra(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
		intent.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_event_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.addEvent) {
			if (verifyFields()) {
				String uri = "https://usersdatapp.appspot.com/event";
				new HttpAsyncTask().execute(uri);
			}
		} else if (id == R.id.searchGps) {
			getPosition();
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean verifyFields() {

		String errore = "";
		if (nameEvent.getText().toString().equals("")
				|| nameEvent.getText().toString() == null) {
			errore += "Field subject missing!\n";
		} else if (descriptionEvent.getText().toString().equals("")
				|| descriptionEvent.getText().toString() == null) {
			errore += "Field description missing. \n";
		} else if (dateEvent.getText().toString().equals("")
				|| dateEvent.getText().toString() == null) {
			errore += "Field date missing. \n";
		} else if (timeEvent.getText().toString().equals("")
				|| timeEvent.getText().toString() == null) {
			errore += "Field time missing. \n";
		} else if (placeEvent.getText().toString().equals("")
				|| placeEvent.getText().toString() == null) {
			errore += "Field location missing. \n";
		}
		if (!errore.equals("")) {
			Toast.makeText(this, errore, Toast.LENGTH_SHORT).show();
			return false;
		}
		try {
			Geocoder geocoder = new Geocoder(this, Locale.getDefault());
			List<Address> addresses = geocoder.getFromLocationName(placeEvent
					.getText().toString(), 1);
			Address address = addresses.get(0);
			double longitude = address.getLongitude();
			double latitude = address.getLatitude();
			Log.i("verify", "long : " + longitude + " lat : " + latitude);
			// LATITUDINE LONGITUDINE
			gps_coordinates = "" + latitude + " " + longitude;
		} catch (Exception e) {
			e.printStackTrace();
			gps_coordinates = "none";
		}
		return true;
	}

	public void saveEvent(View v) {
		Toast.makeText(this, "Example action.", Toast.LENGTH_SHORT).show();
	}

	public void getPosition() {
		Location lastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);

		if (lastLocation == null) {
			startActivity(new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			return;
		}
		// Location location =
		// LocationServices.FusedLocationApi.getLastLocation(new
		// GoogleApiClient.Builder(this).addApi(LocationServices.API)
		// .build());

		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getBaseContext());
		// Showing status
		if (status != ConnectionResult.SUCCESS) { // Google Play Services are
													// not available
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					requestCode);
			dialog.show();
		}

		// locationManager.requestLocationUpdates(provider, 20000, 0, this);

		double lat = (lastLocation.getLatitude());
		double lng = (lastLocation.getLongitude());
		// Creating a LatLng object for the current location
		Geocoder gc = new Geocoder(this);
		try {
			List<Address> addr = gc.getFromLocation(lat, lng, 1);
			EditText et = (EditText) findViewById(R.id.new_eventPlace);
			Address a = addr.get(0);
			String address = "";
			for (int i = 0; i < 3; i++)
				if (a.getAddressLine(i) != null)
					address += (a.getAddressLine(i) + " ");
				else
					break;
			et.setText(address);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void showTimePickerDialog(View v) {
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getFragmentManager(), "timePicker");
	}

	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}

	public void showTextDialog(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Event Description");

		// Set up the input
		final EditText input = new EditText(this);
		// Specify the type of input expected; this, for example, sets the input
		// as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText et = (EditText) findViewById(R.id.new_eventDescription);
				et.setText(input.getText().toString());
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		builder.show();
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		String res = null;

		@Override
		protected String doInBackground(String... urls) {
			// 4. convert JSONObject to JSON to String
			res = POST(urls[0], null);
			return null;
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			/** MODIFY PER FAR SPUNTARE I TOAST CORRETTI **/
			Toast.makeText(getBaseContext(), res, Toast.LENGTH_LONG).show();
			if (res.contains("success"))
				addCalendar();
			else if (res.equals("fail::sessionFailed")) {
				Intent intent = new Intent(ctx, PrincipalActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
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

	public String POST(String url, StringEntity person) {
		String result = "";
		HttpClient httpclient = SetCookie.setCookie(ctx);
		HttpPost httpPost = new HttpPost(url);

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();

		EditText tmp = (EditText) findViewById(R.id.new_eventName);
		pairs.add(new BasicNameValuePair("name", tmp.getText().toString()));

		tmp = (EditText) findViewById(R.id.new_eventDate);
		pairs.add(new BasicNameValuePair("date", tmp.getText().toString()));

		tmp = (EditText) findViewById(R.id.new_eventTime);
		pairs.add(new BasicNameValuePair("hour", tmp.getText().toString()));

		tmp = (EditText) findViewById(R.id.new_eventPlace);
		pairs.add(new BasicNameValuePair("address", tmp.getText().toString()));

		/** MODIFY **/
		pairs.add(new BasicNameValuePair("gps", gps_coordinates));

		tmp = (EditText) findViewById(R.id.new_eventDescription);
		pairs.add(new BasicNameValuePair("descr", tmp.getText().toString()));

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		HttpResponse resp;
		try {
			resp = httpclient.execute(httpPost);
		} catch (IOException e) {
			Log.i("main", "NON Andata a buon fine");
			return "Error: impossible to save your event";
		}

		Log.i("main", "Andata a buon fine");

		return "success";
	}

	private class TimePickerFragment extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(),
					(OnTimeSetListener) this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			EditText et = (EditText) findViewById(R.id.new_eventTime);
			String minuteWithZero;
			if (minute < 10) {
				minuteWithZero = "0" + minute;
			} else {
				minuteWithZero = "" + minute;
			}
			et.setText(hourOfDay + ":" + minuteWithZero);
		}
	}

	private class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			EditText et = (EditText) findViewById(R.id.new_eventDate);
			et.setText(day + "/" + (month + 1) + "/" + year);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// if (mLastLocation != null) {
		// mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
		// mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
		// }

	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub

	}

}