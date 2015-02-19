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

public class JsonLoaderUsers extends AsyncTaskLoader<List<User>> {

	String url;
	Context ctx;
	int flag;

	public JsonLoaderUsers(Context context, String url, int flag) {
		super(context);
		ctx = context;
		this.url = url;
		this.flag = flag;
	}

	@Override
	public List<User> loadInBackground() {
		DefaultHttpClient client = SetCookie.setCookie(ctx);
		HttpGet get = new HttpGet(url);
		HttpResponse resp;
		try {
			resp = client.execute(get);
		} catch (Exception e) {
			e.printStackTrace();
			if (flag == 0) {
				DialogFragment dialog = ConnectionDialogFragment.newInstance();
				dialog.show(((InvitePeople_tabbed) ctx).getFragmentManager(),
						"dialog");
			} else {
				DialogFragment dialog = ConnectionDialogFragment.newInstance();
				dialog.show(((Inviteds_to_Event) ctx).getFragmentManager(),
						"dialog");
			}

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
			List<User> result;
			try {
				JSONArray listOfContacts = new JSONArray(jsonString);
				int len = listOfContacts.length();
				result = new ArrayList<User>(len);
				for (int i = 0; i < len; i++) {
					result.add(new User(listOfContacts.getJSONObject(i)));
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