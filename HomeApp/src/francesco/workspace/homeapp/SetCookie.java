package francesco.workspace.homeapp;

import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;

public class SetCookie {
	public static DefaultHttpClient setCookie(Context ctx) {
		DefaultHttpClient client = new DefaultHttpClient();
		PersistentCookieStore psC = new PersistentCookieStore(ctx);
		CookieStore store = new BasicCookieStore();
		List<Cookie> cookies = psC.getCookies();
		for (Cookie c : cookies) {
			boolean result = c.getName().equals("JSESSIONID");
			if (result) {
				store.addCookie(c);
				break;
			}
		}
		client.setCookieStore(store);
		return client;
	}

}
