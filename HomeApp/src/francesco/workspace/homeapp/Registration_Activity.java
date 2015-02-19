package francesco.workspace.homeapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class Registration_Activity extends Activity {

	Context ctx;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.register_user);

		final EditText psw_inputText = (EditText) findViewById(R.id.Password_inputReg);
		final EditText psw2_inputText = (EditText) findViewById(R.id.RepeatPassword_inputReg);
		final EditText mail_inputText = (EditText) findViewById(R.id.Email_inputReg);
		final EditText mail2_inputText = (EditText) findViewById(R.id.RepeatEmail_inputReg);
		final EditText nome_inputText = (EditText) findViewById(R.id.Name_inputReg);
		final EditText cognome_inputText = (EditText) findViewById(R.id.Lastname_inputReg);
		final Spinner spinner = (Spinner) findViewById(R.id.Spinner_inputReg);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.spinner_value,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		Button regButton = (Button) findViewById(R.id.button_inputReg);
		regButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String errore = "";

				if (nome_inputText.getText().toString().equals("")
						|| nome_inputText.getText().toString() == null) {
					errore += "Insert a name please";
				} else if (cognome_inputText.getText().toString().equals("")
						|| cognome_inputText.getText().toString() == null) {
					errore += "Insert a lastname please";
				} else if (mail_inputText.getText().toString().equals("")
						|| mail_inputText.getText().toString() == null) {
					errore += "Insert an email please";
				} else if (!mail_inputText.getText().toString().contains("@")
						|| (!(mail_inputText.getText().toString()
								.replaceAll("\\s+$", "").endsWith(".com")
								|| mail_inputText.getText().toString()
										.replaceAll("\\s+$", "")
										.endsWith(".it")
								|| mail_inputText.getText().toString()
										.replaceAll("\\s+$", "")
										.endsWith(".org")
								|| mail_inputText.getText().toString()
										.replaceAll("\\s+$", "")
										.endsWith(".eu") || mail_inputText
								.getText().toString().replaceAll("\\s+$", "")
								.endsWith(".fr")))) {

					errore += "Insert a corret email please";
				} else if (!mail_inputText
						.getText()
						.toString()
						.replaceAll("\\s+$", "")
						.equals(mail2_inputText.getText().toString()
								.replaceAll("\\s+$", ""))) {
					errore += "The two emails must be the same";
				} else if (psw_inputText.getText().toString().equals("")
						|| psw_inputText.getText().toString() == null) {
					errore += "Insert a password";
				} else if (!psw_inputText.getText().toString()
						.equals(psw2_inputText.getText().toString())) {
					errore += "The two passwords must be the same ";
				}
				if (!errore.equals("")) {
					Toast.makeText(getBaseContext(), errore, Toast.LENGTH_LONG)
							.show();
					return;
				}
				User u = new User(nome_inputText.getText().toString()
						.replaceAll("\\s+$", ""), psw_inputText.getText()
						.toString(), mail_inputText.getText().toString()
						.replaceAll("\\s+$", ""), cognome_inputText.getText()
						.toString().replaceAll("\\s+$", ""), spinner
						.getSelectedItem().toString(), "none");
				// Log.i("Registration", spinner.getSelectedItem().toString());
				new RegisterTask(Registration_Activity.this).execute(u);
			}
		});

	}

	public void returnToLoginClick(View v) {
		Intent intent = new Intent(Registration_Activity.this,
				LoginActivity.class);
		startActivity(intent);
		finish();
	}

}