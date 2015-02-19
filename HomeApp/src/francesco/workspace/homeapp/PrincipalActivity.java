package francesco.workspace.homeapp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

public class PrincipalActivity extends Activity implements DialogClick {

	Context ctx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;

		SharedPreferences.Editor prefsWriter = getSharedPreferences(
				this.getPackageName(), Context.MODE_PRIVATE).edit();
		prefsWriter.clear();
		prefsWriter.commit();

		if (!isLogged()) {
			Intent intent = new Intent(PrincipalActivity.this,
					LoginActivity.class);
			startActivity(intent);
			finish();
		} else {
			new HttpAsyncTask()
					.execute("https://usersdatapp.appspot.com/login");
		}
	}

	private boolean isLogged() {
		PersistentCookieStore pcS = new PersistentCookieStore(this);
		List<Cookie> cookies = pcS.getCookies();
		boolean res = false;
		for (Cookie c : cookies) {
			if (c.getName().equals("JSESSIONID")) {
				res = true;
				break;
			}
		}
		return res;
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String respons = null;

		@Override
		protected String doInBackground(String... strings) {
			return GET(strings[0], null);
		}

		private String GET(String url, StringEntity se) {
			DefaultHttpClient httpclient = SetCookie.setCookie(ctx);
			HttpGet httpGet = new HttpGet(url);
			try {
				HttpResponse resp;
				resp = httpclient.execute(httpGet);
				respons = readAll(new InputStreamReader(resp.getEntity()
						.getContent(), "UTF-8"));
			} catch (Exception e) {
				// e.printStackTrace();
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
			if (respons.contains("success")) { // utente già loggato
				Intent intent = new Intent(ctx, MainActivity.class);
				startActivity(intent);

				finish();
			} else {
				Intent intent = new Intent(PrincipalActivity.this,
						LoginActivity.class);
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
