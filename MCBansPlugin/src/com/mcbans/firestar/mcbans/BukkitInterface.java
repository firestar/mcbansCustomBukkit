package com.mcbans.firestar.mcbans;

import com.mcbans.firestar.mcbans.bukkitListeners.PlayerListener;
import com.mcbans.firestar.mcbans.callBacks.MainCallBack;
import com.mcbans.firestar.mcbans.commands.CommandHandler;
import com.mcbans.firestar.mcbans.log.ActionLog;
import com.mcbans.firestar.mcbans.log.LogLevels;
import com.mcbans.firestar.mcbans.log.Logger;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;

public class BukkitInterface extends JavaPlugin {
	
	private CommandHandler commandHandle;
	private PlayerListener bukkitPlayer = new PlayerListener(this);
	public int taskID = 0;
	public HashMap<String, Integer> connectionData = new HashMap<String, Integer>();
	public HashMap<String, Long> resetTime = new HashMap<String, Long>();
	public Core Core = new Core();
	public Settings Settings;
	public Language Language = null;
	public MainCallBack callbackThread = null;
	public ActionLog actionLog = null;
	public Consumer lbconsumer = null;
	public String apiServer = "72.10.39.172";
	private String apiKey = "";
	private boolean mode = false;
	public BukkitPermissions Permissions = null;
    public Logger logger = new Logger(this);
	
	public void onDisable() {
		System.out.print("MCBans: Disabled");
		if(callbackThread!=null){
			if(callbackThread.isAlive()){
				callbackThread.interrupt();
			}
		}
	}
	
	public void onEnable() {

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(bukkitPlayer, this);
		
        if( !this.getServer().getOnlineMode() ){
        	logger.log(LogLevels.FATAL, "MCBans: Your server is not in online mode!");
        	pm.disablePlugin(pluginInterface("mcbans"));
        	return;
        }
        
        Settings = new Settings(this);
        if (Settings.doTerminate) {
			log(LogLevels.FATAL, "Please download the latest settings.yml from MCBans.com!");
        	return;
		}
        
        // API KEY verification!
        if(Settings.getString("apiKey") != null){
        	this.apiKey = Settings.getString("apiKey");
        }else{
        	log(LogLevels.FATAL, "Invalid MCBans.jar! Please re-download at http://myserver.mcbans.com.");
        }
        
        
        
        String language;
        
        if (Core.lang != null) {
        	language = Core.lang;
        } else {
        	log(LogLevels.FATAL, "Invalid MCBans.jar! Please re-download at http://myserver.mcbans.com.");
        	return;
        }
        log(LogLevels.INFO, "Loading language file: "+language);
        
        File languageFile = new File("plugins/mcbans/language/"+language+".yml");
        if(!languageFile.exists()){
        	if (Core.lang != null) {
        		log(LogLevels.INFO, "Contacting Master server for language file " + Core.lang + ".yml");
        		Downloader getLanguage = new Downloader();
        		getLanguage.Download("http://"+this.apiServer+"/getLanguage/" + Core.lang + ".yml", "plugins/mcbans/language/" + Core.lang + ".yml");
        		languageFile = new File("plugins/mcbans/language/" + Core.lang + ".yml");
        		if (!languageFile.exists()) {
        			log(LogLevels.FATAL, Core.lang + " does not exist on Master server.");
                    return;
        		}
        	} else {
        		log(LogLevels.FATAL, "No language file found!");
        		return;
        	}
        }
        
        if(Settings.getBoolean("logEnable")){
        	log(LogLevels.INFO, "Starting to save to log file!");
        	actionLog = new ActionLog( this, Settings.getString("logFile") );
        	actionLog.write("MCBans Log File Started");
        }else{
        	log(LogLevels.INFO, "Log file disabled!");
        }
        
        Permissions = new BukkitPermissions( Settings, this );
        commandHandle = new CommandHandler( Settings, this );
        
        
        callbackThread = new MainCallBack( this );
        callbackThread.start();
        if (Core.lang != null) {
        	Language = new Language(Core.lang);
        } else {
        	Language = new Language(Settings.getString("language"));
        }
        
        Plugin logBlock = pm.getPlugin("LogBlock");
        if (logBlock != null) {
        	lbconsumer = ((LogBlock)logBlock).getConsumer();
        	log(LogLevels.INFO, "Enabling LogBlock integration");
        }

        log(LogLevels.INFO, "Started up successfully!");
        
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		return commandHandle.execCommand( command.getName(), args, sender );
	}

    public void log(String message) {
        log(LogLevels.NONE, message);
    }

    public void log(LogLevels type, String message) {
        if (actionLog != null) {
            actionLog.write(message);
        }
        logger.log(type, message);
    }
	
	public void broadcastBanView(String msg){
		for( Player player: this.getServer().getOnlinePlayers() ){
			if( Permissions.isAllow( player.getName(), "ban.view" ) ){
				player.sendMessage( Settings.getPrefix() + " " + msg );
			}
		}
	}
	public void broadcastJoinView(String msg){
		for( Player player: this.getServer().getOnlinePlayers() ){
			if( Permissions.isAllow( player.getName(), "join.view" ) ){
				player.sendMessage( Settings.getPrefix() + " " + msg );
			}
		}
	}
	public void broadcastAltView(String msg){
		for( Player player: this.getServer().getOnlinePlayers() ){
			if( Permissions.isAllow( player.getName(), "alts.view" ) ){
				player.sendMessage( Settings.getPrefix() + " " + msg );
			}
		}
	}
	
	public void broadcastKickView(String msg){
		for( Player player: this.getServer().getOnlinePlayers() ){
			if( Permissions.isAllow( player.getName(), "kick.view" ) ){
				player.sendMessage( Settings.getPrefix() + " " + msg );
			}
		}
	}

	public void broadcastAll(String msg){
		for( Player player: this.getServer().getOnlinePlayers() ){
			player.sendMessage( Settings.getPrefix() + " " + msg );
		}
	}
	
	public void broadcastPlayer( String Player, String msg ){
		Player target = this.getServer().getPlayer(Player);
		if(target!=null){
			target.sendMessage( Settings.getPrefix() + " " + msg );
		}else{
			System.out.print( Settings.getPrefix() + " " + msg );
		}
	}
	public boolean getMode(){
		return mode;
	}
	public void setMode( boolean newMode ){
		mode = newMode;
	}
	public String getApiKey(){
		return this.apiKey;
	}
	public void broadcastPlayer( Player target, String msg ){
		target.sendMessage( Settings.getPrefix() + " " + msg );
	}
	
	public boolean hasErrored (HashMap<String, String> response) {
		if (response.containsKey("error")) {
			String error = response.get("error");
			if (error.contains("Server Disabled")) {
				if (getMode()) {
					return true;
				}
				broadcastBanView( ChatColor.RED + "Server Disabled by an MCBans Admin");
				broadcastBanView( "MCBans is running in reduced functionality mode. Only local bans can be used at this time.");
				log(LogLevels.SEVERE, "The server API key has been disabled by an MCBans Administrator");
				log(LogLevels.SEVERE, "To appeal this decision, please contact an administrator");
				setMode(true);
			} else if (error.contains("api key not found.")) {
				broadcastBanView( ChatColor.RED + "Invalid MCBans.jar!");
				broadcastBanView("The API key inside the current MCBans.jar is invalid. Please re-download the plugin from myserver.mcbans.com");
				log(LogLevels.FATAL, "Invalid MCBans.jar - Please re-download from myserver.mcbans.com!");
			} else {
				broadcastBanView( ChatColor.RED + "Unexpected reply from MCBans API!");
				log(LogLevels.SEVERE, "API returned an invalid error:");
				log(LogLevels.SEVERE, "MCBans said: " + error);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public boolean hasErrored (String response) {
		if (response.contains("error")) {
			if (response.contains("Server Disabled")) {
				if (getMode()) {
					return true;
				}
				broadcastBanView( ChatColor.RED + "Server Disabled by an MCBans Admin");
				broadcastBanView( "MCBans is running in reduced functionality mode. Only local bans can be used at this time.");
				log(LogLevels.SEVERE, "The server API key has been disabled by an MCBans Administrator");
				log(LogLevels.SEVERE, "To appeal this decision, please contact an administrator");
				setMode(true);
			} else if (response.contains("api key not found.")) {
				broadcastBanView( ChatColor.RED + "Invalid MCBans.jar!");
				broadcastBanView( "The API key inside the current MCBans.jar is invalid. Please re-download the plugin from myserver.mcbans.com");
				log(LogLevels.FATAL, "Invalid MCBans.jar - Please re-download from myserver.mcbans.com!");
				getServer().getPluginManager().disablePlugin(pluginInterface("mcbans"));
			} else {
				broadcastBanView( ChatColor.RED + "Unexpected reply from MCBans API!");
				log(LogLevels.SEVERE, "API returned an invalid error:");
				log(LogLevels.SEVERE, "MCBans said: " + response);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public Plugin pluginInterface( String pluginName ){
		return this.getServer().getPluginManager().getPlugin(pluginName);
	}
	
}