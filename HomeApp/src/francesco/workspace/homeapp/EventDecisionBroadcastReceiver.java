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

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.widget.Toast;

public class EventDecisionBroadcastReceiver extends BroadcastReceiver {
	public static final String ACCEPT_INTENT = "francesco.workspace.homeapp.ACCEPT";
	public static final String REFUSE_INTENT = "francesco.workspace.homeapp.REFUSE";
	public static final int NOTIFICATION_ID = 1;

	String my_url = "https://usersdatapp.appspot.com/";

	Event_App event;
	SharedPreferences preferences;
	Context ctx;

	@Override
	public void onReceive(Context context, Intent intent) {
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		ctx = context.getApplicationContext();
		String action = intent.getAction();
		event = (Event_App) intent.getSerializableExtra("singleEvent");

		if (action.equals(ACCEPT_INTENT)) {
			if (!event.getState().equals("accept"))
				new HttpAsyncTask()
						.execute(my_url + "confirmevent", "accepted");
		} else if (action.equals(REFUSE_INTENT)) {
			if (!event.getState().equals("refused"))
				new HttpAsyncTask().execute(my_url + "confirmevent", "refused");
		}

	}

	public void addCalendar() {
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
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		ctx.startActivity(intent);

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
			NotificationManager nm = (NotificationManager) ctx
					.getSystemService(Context.NOTIFICATION_SERVICE);

			if (respons.contains("success") && stateQuery.equals("accepted")) {
				if (!preferences.getBoolean("calendar", false))
					addCalendar();

				Toast.makeText(ctx, "Invitation accepted! :)",
						Toast.LENGTH_LONG).show();
				nm.cancel(NOTIFICATION_ID);
			} else if (respons.contains("success")
					&& stateQuery.equals("refused")) {
				Toast.makeText(ctx, "Invitation refused! :)", Toast.LENGTH_LONG)
						.show();
				nm.cancel(NOTIFICATION_ID);
			} else if (respons.equals("fail::sessionFailed")) {
				Intent intent = new Intent(ctx, PrincipalActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				ctx.startActivity(intent);
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

}
