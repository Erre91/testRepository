package francesco.workspace.homeapp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements DialogClick {

	private Context ctx;
	private EditText username_inputText;
	private EditText psw_inputText;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// to have a full screen application
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.login);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String value = preferences.getString("password", null);
		if (value != null) {
			Editor editor = preferences.edit();
			editor.remove("password");
			editor.remove("username");
			editor.commit();
		}
		username_inputText = (EditText) findViewById(R.id.username_inputText);
		psw_inputText = (EditText) findViewById(R.id.psw_inputText);
		ctx = this;
	}

	public void loginClick(View v) {

		String errore = "";
		if (username_inputText.getText().toString().equals("")
				|| username_inputText.getText().toString() == null) {
			errore += "Insert a username please";
		} else if (!username_inputText.getText().toString().contains("@")
				|| (!(username_inputText.getText().toString()
						.replaceAll("\\s+$", "").endsWith(".com")
						|| username_inputText.getText().toString()
								.replaceAll("\\s+$", "").endsWith(".it")
						|| username_inputText.getText().toString()
								.replaceAll("\\s+$", "").endsWith(".org")
						|| username_inputText.getText().toString()
								.replaceAll("\\s+$", "").endsWith(".eu") || username_inputText
						.getText().toString().replaceAll("\\s+$", "")
						.endsWith(".fr")))) {
			errore += "Insert a valid email";
		} else if (psw_inputText.getText().toString().equals("")
				|| psw_inputText.getText().toString() == null) {
			errore += "Insert a password please";
		}
		if (!errore.equals("")) {
			Toast.makeText(getBaseContext(), errore, Toast.LENGTH_LONG).show();
			return;
		}
		new HttpAsyncTask().execute("https://usersdatapp.appspot.com/login");
	}

	public void onRegisterClick(View v) {
		Intent intent = new Intent(ctx, Registration_Activity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String respons = null;

		@Override
		protected String doInBackground(String... strings) {
			return POST(strings[0], null);
		}

		private String POST(String url, StringEntity se) {
			PersistentCookieStore psC = new PersistentCookieStore(ctx);
			DefaultHttpClient httpclient = SetCookie.setCookie(ctx);
			HttpPost httpPost = new HttpPost(url);
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			final EditText username_inputText = (EditText) findViewById(R.id.username_inputText);
			final EditText psw_inputText = (EditText) findViewById(R.id.psw_inputText);
			String usr = username_inputText.getText().toString()
					.replaceAll("\\s+$", "");
			String psw = psw_inputText.getText().toString();
			pairs.add(new BasicNameValuePair("email", usr));
			pairs.add(new BasicNameValuePair("psw", generateMD5(psw)));
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(pairs));
				HttpResponse resp;
				resp = httpclient.execute(httpPost);
				List<Cookie> cookies = httpclient.getCookieStore().getCookies();
				if (cookies != null && !cookies.isEmpty()) {
					for (Cookie c : cookies) {
						if (c.getName().equals("JSESSIONID")) {
							psC.addCookie(c);
							System.out.println("cookie ottenuto");
						}
					}
				}
				Editor editor = preferences.edit();
				editor.putString("password", psw);
				editor.putString("username", usr);
				editor.commit();
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
			if (respons.contains("success")) {
				Intent intent = new Intent(ctx, MainActivity.class);
				startActivity(intent);
				finish();
			} else {
				Toast.makeText(getBaseContext(), "Log failed",
						Toast.LENGTH_LONG).show();
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

	public static String generateMD5(String str) {
		try {
			String original = str;
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e1) {
				return null;
			}
			md.update(original.getBytes());
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (Exception e) {
			return null;
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
