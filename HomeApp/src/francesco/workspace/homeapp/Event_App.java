package francesco.workspace.homeapp;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class Event_App implements Serializable {

	private String key;
	private String name;
	private String data;
	private String hour;
	private String idCreator;
	private String address;
	private String gps;
	private String state;
	private String description;

	public Event_App(String key, String name, String data, String hour,
			String idCreator, String address, String gps, String description) {
		super();
		this.key = key;
		this.name = name;
		this.data = data;
		this.hour = hour;
		this.idCreator = idCreator;
		this.description = description;
		this.setAddress(address);
		this.setGps(gps);
		setState(null);
	}

	public Event_App(JSONObject jsonObject) throws JSONException {
		this.key = jsonObject.getString("keyEvent");
		this.name = jsonObject.getString("name");
		this.data = jsonObject.getString("date");
		this.hour = jsonObject.getString("hour");
		this.idCreator = jsonObject.getString("idUser");
		this.setAddress(jsonObject.getString("address"));
		this.setGps(jsonObject.getString("gps"));
		if (jsonObject.isNull("state"))
			state = null;
		else
			this.setState(jsonObject.getString("state"));
		this.description = jsonObject.getString("descr");
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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getIdCreator() {
		return idCreator;
	}

	public void setIdCreator(String idCreator) {
		this.idCreator = idCreator;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getGps() {
		return gps;
	}

	public void setGps(String gps) {
		this.gps = gps;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
