/*
 * Decompiled with CFR 0.150.
 *
 * Could not load the following classes:
 *  com.mojang.authlib.Agent
 *  com.mojang.authlib.exceptions.AuthenticationException
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication
 */
package tech.imxianyu.gui.alt;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Formatting;
import net.minecraft.util.Session;
import tech.imxianyu.utils.oauth.OAuth;

public class AltLoginThread
        extends Thread {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Alt alt;
    private String status;

    @Getter
    private boolean finished = false;

    public AltLoginThread(Alt alt) {
        super("Alt Login Thread");
        this.alt = alt;
        this.status = "§eWaiting...";
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void run() {
        this.status = "§eLogging in...";
        if (alt.isMicrosoft() && !alt.isExpired()) {
            var session = new Session(alt.getUsername(), alt.getUserUUID(), alt.getAccessToken(), "mojang");
            mc.setSession(session);
        } else if (alt.isMicrosoft()) {
            OAuth oAuth = new OAuth();
            new Thread(() -> oAuth.refresh(
                    alt.getRefreshToken(),
                    new OAuth.LoginCallback() {
                        @Override
                        public void onSucceed(String uuid, String userName, String token, String refreshToken) {
                            Alt at = new Alt(userName, refreshToken, token, uuid);
                            at.setLastRefreshedTime(System.currentTimeMillis() / 1000L);
                            AltManager.getAlts().set(AltManager.getAlts().indexOf(alt), at);
                            var session = new Session(userName, uuid, token, "mojang");
                            mc.setSession(session);
                            finished = true;
                            AltLoginThread.this.status = Formatting.GREEN + "Logged in. (" + userName + ")";
                        }

                        @Override
                        public void onFailed(Exception e) {

                        }

                        @Override
                        public void setStatus(String status) {
                            AltLoginThread.this.status = "§eRefreshing (" + status + ")...";
                        }
                    }
            )).start();
        } else {
            mc.setSession(new Session(alt.getUsername(), "", "", "mojang"));
        }
        this.status = "§aLogged in. (" + alt.getUsername() + ")";
    }
}

