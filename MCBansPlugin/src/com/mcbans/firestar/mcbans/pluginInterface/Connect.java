package com.mcbans.firestar.mcbans.pluginInterface;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.log.LogLevels;
import com.mcbans.firestar.mcbans.org.json.JSONException;
import com.mcbans.firestar.mcbans.org.json.JSONObject;
import com.mcbans.firestar.mcbans.request.JsonHandler;
import org.bukkit.ChatColor;

import java.util.HashMap;

public class Connect extends Thread {
	private BukkitInterface MCBans;
	public Connect(BukkitInterface p, String PlayerName, String PlayerIP){
		MCBans = p;
		String s = null;
		JsonHandler webHandle = new JsonHandler( MCBans );
		HashMap<String, String> url_items = new HashMap<String, String>();
		url_items.put("player", PlayerName);
		url_items.put("playerip", PlayerIP);
		url_items.put("exec", "playerConnect");
		JSONObject response = webHandle.hdl_jobj(url_items);
		try {
			if(!response.has("banStatus")){
			}else{
				if(MCBans.Settings.getBoolean("onJoinMCBansMessage")){
					MCBans.broadcastPlayer( PlayerName, ChatColor.DARK_GREEN + "Server secured by MCBans!" );
				}
				switch(ConnectStatus.valueOf(response.get("banStatus").toString().toUpperCase())){
					case N:
						if(response.has("is_mcbans_mod")) {
							if(response.get("is_mcbans_mod").equals("y")){
                                MCBans.log( LogLevels.INFO, PlayerName + " is an MCBans.com Staff member");
								MCBans.broadcastJoinView( ChatColor.AQUA + MCBans.Language.getFormat( "isMCBansMod", PlayerName ));
								MCBans.broadcastPlayer(PlayerName, ChatColor.AQUA + MCBans.Language.getFormat ("youAreMCBansStaff"));
                            }
                        }
						if(response.has("disputeCount")){
							if(!response.get("disputeCount").equals("")){
								MCBans.broadcastPlayer(PlayerName, ChatColor.DARK_RED + response.get("disputeCount").toString() + " open disputes!" );
							}
						}
						if(response.has("connectMessage")){
							if(!response.get("connectMessage").equals("")){
								MCBans.broadcastPlayer(PlayerName, ChatColor.AQUA + response.get("connectMessage").toString() );
							}
						}
						if(response.has("altList") && !MCBans.Permissions.isAllow( PlayerName, "alt.hide")){
							if(!response.get("altList").equals("")){
								if(Float.valueOf(response.get("altCount").toString().trim()) > MCBans.Settings.getFloat("maxAlts") && MCBans.Settings.getBoolean("enableMaxAlts")) {
									s = MCBans.Language.getFormat( "overMaxAlts" );
								} else {
									MCBans.broadcastAltView( ChatColor.DARK_PURPLE + MCBans.Language.getFormatAlts( "altAccounts", PlayerName, response.get("altList").toString() ));
								}
							}
						}
						if (s == null) {
							MCBans.log( PlayerName + " has connected!" );
							s = null;
						}
					break;
					case B:
						//MCBans.broadcastJoinView( ChatColor.DARK_RED + MCBans.Language.getFormat( "previousBans", PlayerName ) );
						MCBans.log( PlayerName + " has connected!" );
						if(response.getJSONArray("globalBans").length()>0 || response.getJSONArray("localBans").length()>0){
							MCBans.broadcastJoinView( "Player " + ChatColor.DARK_AQUA + PlayerName + ChatColor.WHITE + " has " + ChatColor.DARK_RED + response.getString("totalBans") + " ban(s)" + ChatColor.WHITE + " and " + ChatColor.BLUE + response.getString("playerRep") + " REP" + ChatColor.WHITE + "." );
							MCBans.broadcastJoinView( "--------------------------" );
							if (response.getJSONArray("globalBans").length() > 0) {
					        	MCBans.broadcastJoinView( ChatColor.DARK_RED + "Global");
					        	for (int v = 0; v < response.getJSONArray("globalBans").length(); v++) {
					        		MCBans.broadcastJoinView( response.getJSONArray("globalBans").getString(v) );
					        	}
					        }
					        if (response.getJSONArray("localBans").length() > 0) {
					        	MCBans.broadcastJoinView( ChatColor.GOLD + "Local");
					        	for (int v = 0; v < response.getJSONArray("localBans").length(); v++) {
					        		MCBans.broadcastJoinView( response.getJSONArray("localBans").getString(v) );
					        	}
					        }
					        MCBans.broadcastJoinView( "--------------------------" );
						}
						if(response.has("altList") && !MCBans.Permissions.isAllow( PlayerName, "alt.hide")){
							if(!response.get("altList").equals("")){
								MCBans.broadcastAltView( ChatColor.DARK_PURPLE + MCBans.Language.getFormatAlts( "altAccounts", PlayerName, response.get("altList").toString() ) );
							}
						}
						if(response.has("disputeCount")){
							if(!response.get("disputeCount").equals("")){
								MCBans.broadcastPlayer(PlayerName, ChatColor.DARK_RED + response.get("disputeCount").toString() + " open disputes!");
							}
						}
						if(response.has("connectMessage")){
							if(!response.get("connectMessage").equals("")){
								MCBans.broadcastPlayer(PlayerName, ChatColor.AQUA + response.get("connectMessage").toString() );
							}
						}
						MCBans.broadcastPlayer(PlayerName, ChatColor.DARK_RED + "You have bans on record!" );
						if(MCBans.Settings.getBoolean("isDebug")){
							System.out.print("Player Rep: "+Float.parseFloat(response.get("playerRep").toString()));
						}
						if(response.has("is_mcbans_mod")) {
							if(response.get("is_mcbans_mod").equals("y")){
                                MCBans.log( LogLevels.INFO, PlayerName + " is an MCBans.com Staff member");
					            MCBans.broadcastBanView( ChatColor.AQUA + MCBans.Language.getFormat( "isMCBansMod", PlayerName ));
					            MCBans.broadcastPlayer(PlayerName, ChatColor.AQUA + MCBans.Language.getFormat("youAreMCBansStaff"));
                            }
						}
					break;
				}
			}
		} catch (JSONException e) {
            if (response.toString().contains("error")) {
			    if (response.toString().contains("Server Disabled")) {
                    MCBans.broadcastBanView( ChatColor.RED + "Server Disabled by an MCBans Admin");
				    MCBans.broadcastBanView( "MCBans is running in reduced functionality mode. Only local bans can be used at this time.");
				    MCBans.log(LogLevels.SEVERE, "The server API key has been disabled by an MCBans Administrator");
				    MCBans.log(LogLevels.SEVERE, "To appeal this decision, please contact an administrator");
                }
            } else {
        	    MCBans.log(LogLevels.SEVERE, "JSON error while trying to parse join data!");
            }
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
}