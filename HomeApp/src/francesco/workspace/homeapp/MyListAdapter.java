package francesco.workspace.homeapp;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyListAdapter extends ArrayAdapter<Event_App> {

	private Context ctx;
	private List<Integer> mEtichette;
	private int resource;
	private boolean invitations;

	/**
	 * Ho previsto un costruttore a cui passare gli elementi da visualizzare
	 * nella lista
	 * 
	 * @param context
	 * @param resource
	 */
	public MyListAdapter(Context context, int resource, boolean value) {
		super(context, resource);
		mEtichette = new ArrayList<Integer>();
		ctx = context;
		this.resource = resource;
		invitations = value;
	}

	/**
	 * Aggiungo un'etichetta nella lista dopo l'ultima voce inserita
	 * 
	 * @param etichetta
	 *            : descrizione dell'etichetta da visualizzare
	 */
	public void addEtichetta(Event_App etichetta) {
		super.add(etichetta);

		mEtichette.add(this.getCount() - 1);
	}

	/**
	 * Tutti gli elementi della lista dovranno essere selezionabili tranne le
	 * etichette. Quindi verifico se l'utente ha selezionato un elemento oppure
	 * un'etichetta e restituisco TRUE o FALSE a seconda del caso.
	 */
	public boolean isEnabled(int position) {
		return mEtichette.indexOf(position) < 0;
	}

	/**
	 * In questa funzione mi preoccupo di visualizzare ogni elemento della lista
	 * come "elemento semplice" oppure come etichetta in base alla posizione che
	 * devo visualizzare. Mi occupo anche di nascondere la ImageView se
	 * l'elemento successivo è un'etichetta oppure se è l'ultimo elemento della
	 * lista.
	 * 
	 */
	@SuppressLint({ "ResourceAsColor", "NewApi" })
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Devo visualizzare un'etichetta?
		final boolean etichetta = mEtichette.indexOf(position) >= 0;
		Log.i("ciccio", "" + etichetta);
		View item = convertView;
		/**
		 * Scelgo il layout da aggiungere alla lista a seconda se devo
		 * visualizzare un'etichetta o meno
		 */
		if (!etichetta) {
			item = LayoutInflater.from(ctx).inflate(R.layout.item_view, parent,
					false);

			Event_App ea = this.getItem(position);

			ImageView imageView = (ImageView) item
					.findViewById(R.id.event_CreatorImage);
			if (invitations)
				imageView.setImageResource(R.drawable.calendar_2);
			else
				imageView.setImageResource(R.drawable.event_black);

			ImageView stateView = (ImageView) item
					.findViewById(R.id.event_StateImg);
			stateView.setVisibility(View.INVISIBLE);

			TextView creatorText = (TextView) item
					.findViewById(R.id.event_nameCreator);
			if (invitations)
				creatorText.setText(ea.getIdCreator());
			else
				creatorText.setVisibility(View.INVISIBLE);

			TextView nameEventText = (TextView) item
					.findViewById(R.id.eventSubject);
			nameEventText.setText("Subject: " + ea.getName());

			TextView descriptionText = (TextView) item
					.findViewById(R.id.eventDescription);
			descriptionText.setText(ea.getDescription());

			TextView oraText = (TextView) item
					.findViewById(R.id.event_textHour);
			oraText.setText(ea.getHour());

			if (invitations) {
				if (ea.getState().equals("accepted")) {
					stateView.setImageResource(R.drawable.accepted_64_green);
					stateView.setVisibility(View.VISIBLE);
				} else if (ea.getState().equals("refused")) {
					stateView.setImageResource(R.drawable.delete_64_red);
					stateView.setVisibility(View.VISIBLE);
				}
			}

			final int prossimo = position + 1;

			if ((position < this.getCount() && mEtichette.indexOf(prossimo) >= 0)
					|| position == this.getCount() - 1) {
				View divider = item.findViewById(R.id.item_separator2);
				divider.setVisibility(View.INVISIBLE);
			}
		} else {

			item = LayoutInflater.from(ctx).inflate(R.layout.lv_header_layout,
					parent, false);
			// Assegno il testo all'etichetta
			Event_App tag = this.getItem(position);
			TextView dateText = (TextView) item.findViewById(R.id.lv_list_hdr);
			dateText.setText(tag.getData());
		}
		return item;
	}

	public Event_App getEvent(int position) {
		return this.getItem(position);
	}
}