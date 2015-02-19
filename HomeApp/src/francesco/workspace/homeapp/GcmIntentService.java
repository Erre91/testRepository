/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package francesco.workspace.homeapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	Bundle extras;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	public static final String TAG = "GCM Demo";
	public static final String ACCEPT_INTENT = "francesco.workspace.homeapp.ACCEPT";
	public static final String REFUSE_INTENT = "francesco.workspace.homeapp.REFUSE";

	@Override
	protected void onHandleIntent(Intent intent) {
		extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				// This loop represents the service doing some work.
				Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
				// Post notification of received message.
				sendNotification(extras.getString("message"));
				Log.i(TAG, "Received: " + extras.toString());
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if (extras.containsKey("notification_type")) {
			NotificationCompat.Builder build = new NotificationCompat.Builder(
					this)
					.setSmallIcon(R.drawable.ic_iconphone)
					.setContentTitle("TapEvent - New Group!")
					.setStyle(
							new NotificationCompat.BigTextStyle().bigText(msg))
					.setAutoCancel(true).setContentText(msg)
					.setLights(Color.BLUE, 300, 100)
					.setDefaults(Notification.DEFAULT_VIBRATE);

			mNotificationManager.notify(NOTIFICATION_ID, build.build());

			return;
		}

		Intent intent = new Intent(this, Event_details_invitation.class);
		Event_App ev = new Event_App(extras.getString("event_key"),
				extras.getString("event_name"), extras.getString("event_date"),
				extras.getString("event_hour"), extras.getString("event_user"),
				extras.getString("event_address"),
				extras.getString("event_location"),
				extras.getString("event_description"));

		ev.setState("false");
		intent.putExtra("singleEvent", ev);

		Intent intent_accept = new Intent();
		Intent intent_refuse = new Intent();

		intent_accept.putExtra("singleEvent", ev);
		intent_refuse.putExtra("singleEvent", ev);
		intent_accept.setAction(ACCEPT_INTENT);
		intent_refuse.setAction(REFUSE_INTENT);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.ic_iconphone)
				.setContentTitle("TapEvent - New Invitation!")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setAutoCancel(true)
				.setContentText(msg)
				.addAction(
						R.drawable.ok_48_blue,
						"Accept",
						PendingIntent.getBroadcast(this, 1234, intent_accept,
								PendingIntent.FLAG_CANCEL_CURRENT))
				.addAction(
						R.drawable.delete_64_red,
						"Refuse",
						PendingIntent.getBroadcast(this, 1234, intent_refuse,
								PendingIntent.FLAG_CANCEL_CURRENT))
				.setDefaults(
						Notification.DEFAULT_LIGHTS
								| Notification.DEFAULT_VIBRATE);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}