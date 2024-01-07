/*
 * Decompiled with CFR 0.150.
 */
package tech.imxianyu.gui.alt;

import lombok.Getter;
import lombok.Setter;

public class Alt {
    @Getter
    private String username;
    @Getter
    private String refreshToken;

    @Getter
    private String accessToken;

    @Getter
    private String userUUID;

    @Getter
    @Setter
    private long lastRefreshedTime = 0L;

    public Alt(String crackedName) {
        this.username = crackedName;
    }

    public Alt(String userName, String refreshToken, String accessToken, String userUUID) {
        this.username = userName;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
        this.userUUID = userUUID;
    }

    public boolean isMicrosoft() {
        return this.refreshToken != null && this.accessToken != null && this.userUUID != null;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() / 1000L - this.getLastRefreshedTime() > 86400;
    }

    public long getLeftExpiringTime() {
        return 86400 - (System.currentTimeMillis() / 1000L - this.getLastRefreshedTime());
    }

}

