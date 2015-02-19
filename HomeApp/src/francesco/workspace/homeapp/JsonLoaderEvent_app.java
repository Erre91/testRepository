package francesco.workspace.homeapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.DialogFragment;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class JsonLoaderEvent_app extends AsyncTaskLoader<List<Event_App>> {

	String url;
	private static Context ctx;

	public JsonLoaderEvent_app(Context context, String url) {
		super(context);
		this.url = url;
		ctx = context;
	}

	@Override
	public List<Event_App> loadInBackground() {
		DefaultHttpClient client = SetCookie.setCookie(ctx);
		HttpGet get = new HttpGet(url);
		HttpResponse resp = null;
		int retry = 500;
		while (retry > 0) {
			try {
				resp = client.execute(get);
				break;
			} catch (IOException e) {
				Log.e("json_err", "network exception");
				retry--;
				if (retry == 0) {
					DialogFragment dialog = ConnectionDialogFragment
							.newInstance();
					dialog.show(((MainActivity) ctx).getFragmentManager(),
							"dialog");
					return null;
				}
			}
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
			List<Event_App> result;
			try {
				JSONArray listOfContacts = new JSONArray(jsonString);
				int len = listOfContacts.length();
				result = new ArrayList<Event_App>(len);
				for (int i = 0; i < len; i++) {
					result.add(new Event_App(listOfContacts.getJSONObject(i)));
				}
			} catch (JSONException e) {
				// TODO
				e.printStackTrace();
				return null;
			}
			return result;
		} else
			return null;
	}

	private class ScreenTask2 extends AsyncTask<Context, Void, Void> {
		Context c;

		@Override
		protected Void doInBackground(Context... params) {
			c = params[0];
			return null;
		}

		@Override
		protected void onPostExecute(Void a) {
			Toast.makeText(c, "Error while reading, try again!",
					Toast.LENGTH_LONG).show();
		}
	}

	private class ScreenTask3 extends AsyncTask<Context, Void, Void> {
		Context c;

		@Override
		protected Void doInBackground(Context... params) {
			c = params[0];
			return null;
		}

		@Override
		protected void onPostExecute(Void a) {
			Toast.makeText(c, "Error in json code!", Toast.LENGTH_LONG).show();
		}
	}

	// private class AlertTask extends AsyncTask<Context, Void, Void> {
	// Context c;
	//
	// @Override
	// protected Void doInBackground(Context... params) {
	// c = params[0];
	// return null;
	// }
	//
	// @Override
	// protected void onPostExecute(Void a) {
	// AlertDialog alertDialog;
	// alertDialog = new AlertDialog.Builder(c).create();
	// alertDialog.setTitle("Connection error");
	// alertDialog.setMessage("Unable to contact the server");
	// alertDialog.show();
	// }
	// }

	private String readAll(Reader reader) throws IOException {
		StringBuilder builder = new StringBuilder(4096);
		for (CharBuffer buf = CharBuffer.allocate(512); (reader.read(buf)) > -1; buf
				.clear()) {
			builder.append(buf.flip());
		}
		return builder.toString();
	}
}