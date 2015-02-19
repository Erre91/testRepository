package francesco.workspace.homeapp;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

	String name;
	String lastname;
	String password;
	String email;
	String tipoUtente;
	String state;

	boolean invited;
	boolean clicked;

	public User(String nome, String password, String email, String cognome,
			String tipoUtente, String state) {
		super();
		this.name = nome;
		this.lastname = cognome;
		this.email = email;
		this.password = password;
		this.tipoUtente = tipoUtente;
		this.state = state;
		this.invited = false;
		this.clicked = false;
	}

	public User(JSONObject jsonObject) throws JSONException {
		this.name = jsonObject.getString("nameUser");
		this.lastname = jsonObject.getString("lastnameUser");
		this.email = jsonObject.getString("idUser");
		this.password = null;
		this.tipoUtente = jsonObject.getString("groupUser");
		this.state = jsonObject.getString("state");
		if (jsonObject.getString("invited").equals("true"))
			this.invited = true;
		else
			this.invited = false;
		this.clicked = false;
	}

	// getters and setters

	public String getNome() {
		return name;
	}

	public void setNome(String nome) {
		this.name = nome;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCognome() {
		return lastname;
	}

	public void setCognome(String cognome) {
		this.lastname = cognome;
	}

	public String getTipoUtente() {
		return tipoUtente;
	}

	public void setTipo(String tipoUtente) {
		this.tipoUtente = tipoUtente;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public boolean isInvited() {
		return invited;
	}

	public void setInvited(boolean invited) {
		this.invited = invited;
	}

	public boolean isClicked() {
		return clicked;
	}

	public void setClicked(boolean clicked) {
		this.clicked = clicked;
	}

}