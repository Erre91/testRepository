package francesco.workspace.homeapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class Group_App implements Serializable {

	private String key;
	private String name;
	private String idCreator;
	private List<User> users;
	boolean created;

	public Group_App(String key, String name, String idCreator,
			List<User> users, boolean created) {
		super();
		this.key = key;
		this.name = name;
		this.idCreator = idCreator;
		this.users = users;
		this.created = created;
	}

	public Group_App(JSONObject jsonObject) throws JSONException {
		this.key = jsonObject.getString("group_key");
		this.name = jsonObject.getString("group_name");
		this.idCreator = jsonObject.getString("group_creator");
		if (jsonObject.getString("group_created").equals("true"))
			created = true;
		else
			created = false;
		if (jsonObject.isNull("inv"))
			users = null;
		else {
			users = new ArrayList<User>();
			JSONArray types = jsonObject.getJSONArray("type_inv");
			JSONArray name = jsonObject.getJSONArray("name_inv");
			JSONArray lastname = jsonObject.getJSONArray("lastname_inv");
			JSONArray data = jsonObject.getJSONArray("inv");
			if (data != null && data.length() != 0
					&& data.length() == types.length()) {
				for (int i = 0; i < data.length(); i++) {
					User ux = new User(name.get(i).toString(), null, data
							.get(i).toString(), lastname.get(i).toString(),
							types.get(i).toString(), null);
					ux.setClicked(false);
					users.add(ux);
				}
			}
		}
	}

	public boolean getCreated() {
		return created;
	}

	public void setCreated(boolean created) {
		this.created = created;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdCreator() {
		return idCreator;
	}

	public void setIdCreator(String idCreator) {
		this.idCreator = idCreator;
	}

	public List<User> getUsers() {
		return users;
	}

}
