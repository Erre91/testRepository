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

public class JsonLoaderGroup extends AsyncTaskLoader<List<Group_App>> {

	private String url;
	private Context ctx;

	public JsonLoaderGroup(Context context, String url) {
		super(context);
		ctx = context;
		this.url = url;
	}

	@Override
	public List<Group_App> loadInBackground() {
		DefaultHttpClient client = SetCookie.setCookie(ctx);
		HttpGet get = new HttpGet(url);
		HttpResponse resp;
		try {
			resp = client.execute(get);
		} catch (Exception e) {
			e.printStackTrace();
			DialogFragment dialog = ConnectionDialogFragment.newInstance();
			dialog.show(((InvitePeople_tabbed) ctx).getFragmentManager(),
					"dialog");
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
			return result;
		} else
			return null;
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