package tech.imxianyu.music.dto;

import com.google.gson.JsonObject;
import tech.imxianyu.music.CloudMusic;
import tech.imxianyu.utils.network.HttpClient;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户对象
 */
public class User {
    public final long id;
    public final String name;
    public final String signature;
    public final int level;
    public final int vip;
    public final long listenSongs;
    public final int playlistCount;
    public final int createTime;
    public final int createDay;
    protected final HttpClient api;
    private final long likePlayListId = 0;

    public User(JsonObject data) {
        this.api = CloudMusic.api;
        JsonObject profile = data.getAsJsonObject("profile");

        this.id = profile.get("userId").getAsLong();
        this.name = profile.get("nickname").getAsString();
        this.signature = profile.get("signature").getAsString();
        this.vip = profile.get("vipType").getAsInt();
        this.playlistCount = profile.get("playlistCount").getAsInt();
        this.listenSongs = data.get("listenSongs").getAsInt();
        this.level = data.get("level").getAsInt();
        this.createTime = data.get("createTime").getAsInt();
        this.createDay = data.get("createDays").getAsInt();
    }

    /**
     * 用户歌单
     *
     * @return 歌单列表
     */
    public List<PlayList> playLists(int page, int limit) {
        if (limit == 0) {
            limit = 30;
        }

        Map<String, Object> postData = new HashMap<String, Object>();
        postData.put("uid", this.id);
        postData.put("limit", limit);
        postData.put("offset", limit * page);
        postData.put("includeVideo", true);

        JsonObject data = this.api.POST("/user/playlist", postData).toJson();

        List<PlayList> playLists = new ArrayList<>();
        data.get("playlist").getAsJsonArray().forEach(playList -> {
            playLists.add(new PlayList(playList.getAsJsonObject()));
        });

        return playLists;
    }

}
