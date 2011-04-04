package com.onarandombox.Rigby.MultiVerse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class MVUpdateCheck {

	MultiVerse plugin;
	private final Logger log = Logger.getLogger("Minecraft");
	public Timer timer;

	public MVUpdateCheck(MultiVerse instance) {
		this.plugin = instance;

		int delay = 0;
		int period = 7200;

		timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				checkUpdate();
			}
		}, delay * 1000, period * 1000);
	}

	public void checkUpdate() {
		try {
			URL url = new URL(
					"http://bukkit.onarandombox.com/multiverse/version.php?v="
							+ this.plugin.getDescription().getVersion());
			URLConnection conn = url.openConnection();
			conn.setReadTimeout(2000); // Time out of 10 Seconds, 1000 = 1
										// Second.
			HttpURLConnection httpConnection = (HttpURLConnection) conn;
			int code = httpConnection.getResponseCode();
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line;
			String version = null;
			while ((line = rd.readLine()) != null) {
				if (version == null) {
					version = line;
				}
			}
			if ((Double.valueOf(version) > Double.valueOf(this.plugin
					.getDescription().getVersion())) && code == 200) {
				log.info(this.plugin.logPrefix
						+ "There is an update out for MultiVerse (v" + version
						+ ")");
			}
			rd.close();
		} catch (Exception e) {

		}
	}

}
