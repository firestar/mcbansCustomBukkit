package com.mcbans.firestar.mcbans.bukkitListeners;

import com.mcbans.firestar.mcbans.BukkitInterface;
import com.mcbans.firestar.mcbans.pluginInterface.Connect;
import com.mcbans.firestar.mcbans.pluginInterface.Disconnect;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
	private BukkitInterface MCBans;
	public PlayerListener(BukkitInterface plugin) {
        MCBans = plugin;
    }
	@EventHandler
	public void onPlayerPreLogin(PlayerPreLoginEvent event) {
		
		if (event.getResult() != Result.ALLOWED) {
			return;
		}
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		String playerIP = event.getPlayer().getAddress().getAddress().getHostAddress();
        String playerName = event.getPlayer().getName();
		Connect playerConnect = new Connect( MCBans, playerName, playerIP );
		playerConnect.start();
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        Disconnect disconnectHandler = new Disconnect( MCBans, playerName );
        disconnectHandler.start();
    }
}