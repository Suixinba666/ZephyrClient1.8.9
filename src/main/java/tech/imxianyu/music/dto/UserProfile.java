package tech.imxianyu.music.dto;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ImXianyu
 * @since 6/16/2023 4:58 PM
 */
@Data
public class UserProfile {


    private Integer code;
    private AccountDTO account;
    private ProfileDTO profile;

    public UserProfile(JsonObject obj) {
        this.code = obj.get("code").getAsInt();

        this.account = new AccountDTO();

        JsonObject acc = obj.getAsJsonObject("account");
        this.account.id = acc.get("id").getAsInt();
        this.account.userName = acc.get("userName").getAsString();
        this.account.type = acc.get("type").getAsInt();
        this.account.status = acc.get("status").getAsInt();
        this.account.whitelistAuthority = acc.get("whitelistAuthority").getAsInt();
        this.account.createTime = acc.get("createTime").getAsLong();
        this.account.tokenVersion = acc.get("tokenVersion").getAsInt();
        this.account.ban = acc.get("ban").getAsInt();
        this.account.baoyueVersion = acc.get("baoyueVersion").getAsInt();
        this.account.donateVersion = acc.get("donateVersion").getAsInt();
        this.account.vipType = acc.get("vipType").getAsInt();
        this.account.anonimousUser = acc.get("anonimousUser").getAsBoolean();
        this.account.paidFee = acc.get("paidFee").getAsBoolean();

        this.profile = new ProfileDTO();

        JsonObject prof = obj.getAsJsonObject("profile");
        this.profile.userId = prof.get("userId").getAsInt();
        this.profile.userType = prof.get("userType").getAsInt();
        this.profile.nickname = prof.get("nickname").getAsString();
        this.profile.avatarImgId = prof.get("avatarImgId").getAsLong();
        this.profile.avatarUrl = prof.get("avatarUrl").getAsString();
        this.profile.backgroundImgId = prof.get("backgroundImgId").getAsLong();
        this.profile.backgroundUrl = prof.get("backgroundUrl").getAsString();
        if (prof.has("signature") && !(prof.get("signature") instanceof JsonNull))
            this.profile.signature = prof.get("signature").getAsString();

        this.profile.createTime = prof.get("createTime").getAsLong();
        this.profile.userName = prof.get("userName").getAsString();
//        this.profile.accountType = prof.get("accountType").getAsInt();
//        this.profile.shortUserName = prof.get("shortUserName").getAsString();
//        this.profile.birthday = prof.get("birthday").getAsLong();
//        this.profile.authority = prof.get("authority").getAsInt();
//        this.profile.gender = prof.get("gender").getAsInt();
//        this.profile.accountStatus = prof.get("accountStatus").getAsInt();
//        this.profile.province = prof.get("province").getAsInt();
//        this.profile.city = prof.get("city").getAsInt();
//        this.profile.authStatus = prof.get("authStatus").getAsInt();
//        this.profile.description = prof.get("description").getAsString();
//        this.profile.detailDescription = prof.get("detailDescription").getAsString();
//        this.profile.defaultAvatar = prof.get("defaultAvatar").getAsBoolean();
//        this.profile.expertTags = prof.get("expertTags");
//        this.profile.experts = prof.get("experts");
//        this.profile.djStatus = prof.get("djStatus").getAsInt();
//        this.profile.locationStatus = prof.get("locationStatus").getAsInt();
//        this.profile.vipType = prof.get("vipType").getAsInt();
//        this.profile.followed = prof.get("followed").getAsBoolean();
//        this.profile.mutual = prof.get("mutual").getAsBoolean();
//        this.profile.authenticated = prof.get("authenticated").getAsBoolean();
//        this.profile.lastLoginTime = prof.get("lastLoginTime").getAsLong();
//        this.profile.lastLoginIP = prof.get("lastLoginIP").getAsString();
//        this.profile.remarkName = prof.get("remarkName");
//        this.profile.viptypeVersion = prof.get("viptypeVersion").getAsLong();
//        this.profile.authenticationTypes = prof.get("authenticationTypes").getAsInt();
//        this.profile.avatarDetail = prof.get("avatarDetail");
//        this.profile.anchor = prof.get("anchor").getAsBoolean();
    }



    @NoArgsConstructor
    @Data
    public static class AccountDTO {
        private Integer id;
        private String userName;
        private Integer type;
        private Integer status;
        private Integer whitelistAuthority;
        private Long createTime;
        private Integer tokenVersion;
        private Integer ban;
        private Integer baoyueVersion;
        private Integer donateVersion;
        private Integer vipType;
        private Boolean anonimousUser;
        private Boolean paidFee;
    }

    @NoArgsConstructor
    @Data
    public static class ProfileDTO {
        private Integer userId;
        private Integer userType;
        private String nickname;
        private Long avatarImgId;
        private String avatarUrl;
        private Long backgroundImgId;
        private String backgroundUrl;
        private String signature;
        private Long createTime;
        private String userName;
        private Integer accountType;
        private String shortUserName;
        private Long birthday;
        private Integer authority;
        private Integer gender;
        private Integer accountStatus;
        private Integer province;
        private Integer city;
        private Integer authStatus;
        private String description;
        private String detailDescription;
        private Boolean defaultAvatar;
        private Object expertTags;
        private Object experts;
        private Integer djStatus;
        private Integer locationStatus;
        private Integer vipType;
        private Boolean followed;
        private Boolean mutual;
        private Boolean authenticated;
        private Long lastLoginTime;
        private String lastLoginIP;
        private Object remarkName;
        private Long viptypeVersion;
        private Integer authenticationTypes;
        private Object avatarDetail;
        private Boolean anchor;
    }
}
