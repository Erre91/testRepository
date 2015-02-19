package francesco.workspace.homeapp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Event_details_invitation extends Activity implements
		OnMapLongClickListener {

	Event_App event;
	GoogleMap map = null;
	MapFragment myMapFragment = null;
	View rootView;
	private LatLng MY_PLACE;
	private TextView place;
	Context ctx;
	private boolean mapSet;
	SharedPreferences preferences;
	String my_url = "https://usersdatapp.appspot.com/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ctx = this;
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Intent i = getIntent();
		event = (Event_App) i.getSerializableExtra("singleEvent");

		if (i.hasExtra("decision")) {
			if (i.getStringExtra("decision").equals("accept")) {
				acceptClick(this.rootView);
				return;
			} else {
				refuseClick(this.rootView);
				return;
			}
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.my_event_detail);
		place = (TextView) findViewById(R.id.textView2);
		TextView nameView = (TextView) findViewById(R.id.subjectEvent);
		nameView.setText(event.getName());
		TextView dataView = (TextView) findViewById(R.id.dataEvent);
		dataView.setText(event.getData());
		TextView hourView = (TextView) findViewById(R.id.hourEvent);
		hourView.setText(event.getHour());
		TextView creatorText = (TextView) findViewById(R.id.creatorEvent);
		creatorText.setText(event.getIdCreator());

		if (event.getState().equals("accepted")) {
			TextView buttAccepted = (TextView) findViewById(R.id.acceptButton);
			buttAccepted.setTextColor(getResources().getColor(
					R.color.greenRafTotaro));
			buttAccepted.setText("Accepted");
		} else if (event.getState().equals("refused")) {
			TextView buttDeclined = (TextView) findViewById(R.id.declineButton);
			buttDeclined.setTextColor(getResources().getColor(
					R.color.greenRafTotaro));
			buttDeclined.setText("Declined");
		}
		double lat, longit;
		if (event.getGps().equals("none")) {
			lat = 0;
			longit = 0;
			ImageView img = (ImageView) findViewById(R.id.mapNotAvaible);
			img.setVisibility(View.VISIBLE);
			mapSet = false;
		} else {
			String gps_coordinates[] = event.getGps().split(" ");
			lat = Double.parseDouble(gps_coordinates[0]);
			longit = Double.parseDouble(gps_coordinates[1]);

			MY_PLACE = new LatLng(lat, longit);
			MapsInitializer.initialize(this);
			GoogleMapOptions options = new GoogleMapOptions()
					.camera(CameraPosition.fromLatLngZoom(MY_PLACE, 11))
					.compassEnabled(false).mapType(GoogleMap.MAP_TYPE_NORMAL)
					.rotateGesturesEnabled(false).scrollGesturesEnabled(false)
					.tiltGesturesEnabled(false).zoomControlsEnabled(true)
					.zoomGesturesEnabled(false);
			myMapFragment = MapFragment.newInstance(options);
			FragmentManager fm = getFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.fragment_content, myMapFragment);
			ft.commit();
			setUpMapIfNeeded();
			mapSet = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mapSet)
			setUpMapIfNeeded();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.details_event2, menu);
		return true;
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

		case R.id.action_group:
			openPeopleActivity();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void openPeopleActivity() {
		Intent intent = new Intent(Event_details_invitation.this,
				Inviteds_to_Event.class);
		intent.putExtra("singleEvent", event);
		startActivity(intent);
	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		double lat, longit;
		String gps_coordinates[] = event.getGps().split(" ");
		lat = Double.parseDouble(gps_coordinates[0]);
		longit = Double.parseDouble(gps_coordinates[1]);
		MY_PLACE = new LatLng(lat, longit);

		String namePlace = event.getAddress();

		Uri location = Uri.parse("geo:0,0?q=" + lat + "," + longit + "("
				+ namePlace + ")");
		Intent intent = new Intent(Intent.ACTION_VIEW, location);
		startActivity(intent);
	}

	private void setUpMapIfNeeded() {
		if (map == null) {
			// Try to obtain the map from the SupportMapFragment.
			map = myMapFragment.getMap();
			// Check if we were successful in obtaining the map.
			if (map != null) {
				map.setOnMapLongClickListener(this);
				map.addMarker(new MarkerOptions().position(MY_PLACE).title(
						event.getAddress()));
			}
		}
	}

	public void acceptClick(View v) {
		if (!event.getState().equals("accepted"))
			new HttpAsyncTask().execute(my_url + "confirmevent", "accepted");
	}

	public void refuseClick(View v) {
		if (!event.getState().equals("refused")) {
			new HttpAsyncTask().execute(my_url + "confirmevent", "refused");
		}
	}

	public void infoClick(View v) {
		final Dialog dialog = new Dialog(ctx);
		dialog.setContentView(R.layout.info_alert);
		dialog.setTitle("Event details");

		TextView nameCreatorView = (TextView) dialog
				.findViewById(R.id.event_nameCreator);
		nameCreatorView.setText(event.getIdCreator());
		TextView subView = (TextView) dialog.findViewById(R.id.eventSubject);
		subView.setText("Subject: " + event.getName());
		TextView descriView = (TextView) dialog
				.findViewById(R.id.eventDescription);
		descriView.setText(event.getDescription());
		TextView dataT = (TextView) dialog.findViewById(R.id.event_textDate);
		dataT.setText(event.getData());
		TextView hourT = (TextView) dialog.findViewById(R.id.event_textHour);
		hourT.setText(event.getHour());

		dialog.show();
	}

	private void addCalendar() {
		Intent intent = new Intent(Intent.ACTION_INSERT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra(Events.TITLE, event.getName());
		intent.putExtra(Events.EVENT_LOCATION, event.getAddress());
		intent.putExtra(Events.DESCRIPTION, "to do");

		// Setting dates
		String dateCalendar = event.getData() + "-" + event.getHour();
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

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String respons = null;
		private String stateQuery;

		@Override
		protected String doInBackground(String... strings) {

			// 4. convert JSONObject to JSON to String
			return POST(strings[0], strings[1]);
		}

		private String POST(String url, String state) {
			stateQuery = state;
			HttpClient httpclient = SetCookie.setCookie(ctx);
			HttpPost httpPost = new HttpPost(url);
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("keyEvent", event.getKey()));
			pairs.add(new BasicNameValuePair("state", state));
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(pairs));
				HttpResponse resp;
				resp = httpclient.execute(httpPost);
				respons = readAll(new InputStreamReader(resp.getEntity()
						.getContent(), "UTF-8"));
			} catch (Exception e) {
				return null;
			}
			return null;
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				finish();
				return;
			}
			if (respons.contains("success") && stateQuery.equals("accepted"))
				if (!preferences.getBoolean("calendar", false))
					addCalendar();
				else if (respons.equals("fail::sessionFailed")) {
					Intent intent = new Intent(ctx, PrincipalActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			finish();
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

}
