package francesco.workspace.homeapp;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class RegisterTask extends AsyncTask<User, Integer, boolean[]> {

	private ProgressDialog progressDialog;
	Context context;
	User u;
	String url = "https://usersdatapp.appspot.com/savecontact";

	public RegisterTask(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(context.getResources().getString(
				R.string.caricando));
		progressDialog.show();
	}

	@Override
	protected boolean[] doInBackground(User... utente) {
		boolean[] logged = { false };

		ArrayList<NameValuePair> dataUser = new ArrayList<NameValuePair>();
		dataUser.add(new BasicNameValuePair("email", utente[0].getEmail()));
		dataUser.add(new BasicNameValuePair("name", utente[0].getNome()));
		dataUser.add(new BasicNameValuePair("lastname", utente[0].getCognome()));
		dataUser.add(new BasicNameValuePair("psw", utente[0].getPassword()));
		dataUser.add(new BasicNameValuePair("idgroup", utente[0]
				.getTipoUtente()));

		String resp = "";
		try {
			resp = HTTPConnectionTask.executeHttpPost(url, dataUser);
		} catch (Exception e) {
			e.printStackTrace();
			logged[0] = false;
			return logged;
		}
		Log.i("regTask", resp);
		if (resp.contains("success")) {
			Log.i("main", "Andata a buon fine");
			logged[0] = true;
		} else {
			logged[0] = false;
		}
		return logged;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		progressDialog.setMessage(context.getResources().getString(
				R.string.caricando));
	}

	@Override
	protected void onPostExecute(boolean logged[]) {
		progressDialog.dismiss();
		if (logged[0]) {
			Toast.makeText(context, "User inserted corretly", Toast.LENGTH_LONG)
					.show();
			Intent intent = new Intent(context, LoginActivity.class);
			context.startActivity(intent);
			((Activity) context).finish();
		} else {
			Toast.makeText(context, "Error: impossible to add new user",
					Toast.LENGTH_LONG).show();
		}
		super.onPostExecute(logged);
	}
}
