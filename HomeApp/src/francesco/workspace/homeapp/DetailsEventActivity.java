package francesco.workspace.homeapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class DetailsEventActivity extends Activity implements
		OnMapLongClickListener {

	private Event_App event;
	private GoogleMap map = null;
	private MapFragment myMapFragment = null;
	private View rootView;
	private LatLng MY_PLACE;
	private boolean mapSet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details_event);

		Intent i = getIntent();
		event = (Event_App) i.getSerializableExtra("singleEvent");

		TextView subjectView = (TextView) findViewById(R.id.second_subjectEvent);
		subjectView.setText(event.getName());
		TextView dataView = (TextView) findViewById(R.id.second_dataEvent);
		dataView.setText(event.getData());
		TextView hourView = (TextView) findViewById(R.id.second_hourEvent);
		hourView.setText(event.getHour());

		double lat, longit;
		if (event.getGps().equals("none")) {
			lat = 0;
			longit = 0;
			ImageView img = (ImageView) findViewById(R.id.second_mapNotAvaible);
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
			ft.replace(R.id.second_fragment_content, myMapFragment);
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
		getMenuInflater().inflate(R.menu.details_event, menu);
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
		Intent intent = new Intent(DetailsEventActivity.this,
				InvitePeople_tabbed.class);
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

		Uri location = Uri.parse("geo:0,0?q=" + lat + "," + longit + "("
				+ event.getName() + ")");
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

	public void infoClick(View v) {
		final Dialog dialog = new Dialog(this);
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

		ImageView img = (ImageView) dialog
				.findViewById(R.id.event_CreatorImage);
		img.setImageResource(R.drawable.nerd_black_best);

		dialog.show();
	}

}
