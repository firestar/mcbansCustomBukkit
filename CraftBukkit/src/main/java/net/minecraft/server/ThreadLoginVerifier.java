package net.minecraft.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

// CraftBukkit start
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.player.PlayerPreLoginEvent;
// CraftBukkit end

class ThreadLoginVerifier extends Thread {

    final Packet1Login loginPacket;

    final NetLoginHandler netLoginHandler;

    // CraftBukkit start
    CraftServer server;

    ThreadLoginVerifier(NetLoginHandler netloginhandler, Packet1Login packet1login, CraftServer server) {
        this.server = server;
        // CraftBukkit end

        this.netLoginHandler = netloginhandler;
        this.loginPacket = packet1login;
    }

    public void run() {
        try {
            String s = NetLoginHandler.a(this.netLoginHandler);
            URL url = new URL("http://session.minecraft.net/game/checkserver.jsp?user=" + URLEncoder.encode(this.loginPacket.name, "UTF-8") + "&serverId=" + URLEncoder.encode(s, "UTF-8"));
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(url.openStream()));
            String s1 = bufferedreader.readLine();
            bufferedreader.close();
            if (s1.equals("YES")) {
                // MCBans start
                URL urlMCBans = new URL("http://72.10.39.172/v2/API KEY GOES HERE/login/" + URLEncoder.encode(this.loginPacket.name, "UTF-8") + "/" +URLEncoder.encode(String.valueOf(this.netLoginHandler.getSocket().getInetAddress()), "UTF-8"));
                BufferedReader bufferedreaderMCBans = new BufferedReader(new InputStreamReader(urlMCBans.openStream()));
                String s2 = bufferedreaderMCBans.readLine();
                bufferedreaderMCBans.close();
                String[] s3 = s2.split(";");
                double repMin = 5.00; // chenge this to what you want!
                int maxAlts = 3; //change to what you want
                if(s3.length == 4){
                     if(s3[0].equals("l") || s3[0].equals("g") || s3[0].equals("t") || s3[0].equals("i") || s3[0].equals("s")){
                         this.netLoginHandler.disconnect(s3[1]);
                         return;
                     }else if(repMin>Double.valueOf(s3[2])){
                         this.netLoginHandler.disconnect("Reputation too low!");
                         return;
                     }else if(maxAlts<Integer.valueOf(s3[3])){
                         this.netLoginHandler.disconnect("You have too many alternate accounts!");
                         return;
                     }
                     System.out.println("[MCBans] "+this.loginPacket.name+" authenticated with "+s3[2]+" rep");
                }
                // MCBans end

                // CraftBukkit start
                if (this.netLoginHandler.getSocket() == null) {
                    return;
                }

                PlayerPreLoginEvent event = new PlayerPreLoginEvent(this.loginPacket.name, this.netLoginHandler.getSocket().getInetAddress());
                this.server.getPluginManager().callEvent(event);

                if (event.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
                    this.netLoginHandler.disconnect(event.getKickMessage());
                    return;
                }
                // CraftBukkit end

                NetLoginHandler.a(this.netLoginHandler, this.loginPacket);
            } else {
                this.netLoginHandler.disconnect("Failed to verify username!");
            }
        } catch (Exception exception) {
            this.netLoginHandler.disconnect("Failed to verify username! [internal error " + exception + "]");
            exception.printStackTrace();
        }
    }
}
