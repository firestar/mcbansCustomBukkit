package com.mcbans.firestar.mcbans.backup;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.log.LogLevels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class BackupCheck extends Thread {
	private BukkitInterface MCBans;
	private String apiKey = null;
	private boolean debug = false;
	public BackupCheck(BukkitInterface p){
		MCBans = p;
		apiKey = MCBans.getApiKey();
		debug = MCBans.Settings.getBoolean("isDebug");
	}
	@Override
	public void run(){
		while(true){
			String result = "";
			try {
				URL url;
				url = new URL( "http://"+MCBans.apiServer+"/v2/" + this.apiKey );
	    	    URLConnection conn = url.openConnection();
	    	    conn.setConnectTimeout(5000);
	    	    conn.setReadTimeout(5000);
	    	    conn.setDoOutput(true);
	    	    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	    	    wr.write("exec=check");
	    	    wr.flush();
	    	    StringBuilder buf = new StringBuilder();
	    	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    	    String line;
	    	    while ((line = rd.readLine()) != null) {
	    	    	buf.append(line);
	    	    }
	    	    result = buf.toString();
	    	    if(debug){
	    	    	MCBans.log(result);
	    	    }
	    	    wr.close();
	    	    rd.close();
			} catch (MalformedURLException e) {
				if(debug){
					e.printStackTrace();
				}
			} catch (IOException e) {
				if(debug){
					e.printStackTrace();
				}
			}
			if(!result.equalsIgnoreCase("up")){
				if (MCBans.hasErrored(result)) {
					return;
				} else {
					MCBans.setMode(true);
					MCBans.log(LogLevels.SEVERE, "MCBans Master Server is offline!");
				}
			}else{
				MCBans.setMode(false);
			}
			try {
				Thread.sleep(300000);
			} catch (InterruptedException e) {
				if(debug){
					e.printStackTrace();
				}
			}
		}
	}
}