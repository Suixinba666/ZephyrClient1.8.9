package tech.imxianyu.music.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import tech.imxianyu.music.CloudMusic;
import tech.imxianyu.music.IMusic;
import tech.imxianyu.music.Lyric;
import tech.imxianyu.utils.network.HttpClient;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Music implements IMusic {
    public final long id;
    public final String name;
    public final String aliasName;
    public final JsonArray artists;
    public final JsonObject album;
    public final long duration;
    public final String picUrl;

    /**
     * 专辑歌曲没有 picUrl, 通过 cover 传入封面 picUrl
     */
    public Music(JsonObject data, String cover) {
        this.id = data.get("id").getAsLong();
        this.name = data.get("name").getAsString();

        JsonArray alias;
        if (data.has("alia")) {
            alias = data.get("alia").getAsJsonArray();
        } else {
            alias = data.get("alias").getAsJsonArray();
        }

        if (alias.size() > 0) {
            this.aliasName = alias.get(0).getAsString();
        } else {
            this.aliasName = "";
        }

        if (data.has("ar")) {
            this.artists = data.get("ar").getAsJsonArray();
        } else {
            this.artists = data.get("artists").getAsJsonArray();
        }

        if (data.has("al")) {
            this.album = data.get("al").getAsJsonObject();
        } else {
            this.album = data.get("album").getAsJsonObject();
        }

        if (data.has("dt")) {
            this.duration = data.get("dt").getAsLong() / 1000;
        } else {
            this.duration = data.get("duration").getAsLong() / 1000;
        }

        if (this.album.has("picUrl")) {
            this.picUrl = this.album.get("picUrl").getAsString();
        } else {
            this.picUrl = cover;
        }
    }

    private String getArtists(int limit) {
        StringBuilder artistsName = new StringBuilder();

        int count = 0;

        for (JsonElement artistData : artists) {
            JsonElement n = ((JsonObject) artistData).get("name");

            if (count + 1 > limit)
                return artistsName.substring(0, artistsName.length() - 3) + " ...";

            if (!(n instanceof JsonNull)) {
                artistsName.append(n.getAsString()).append(" / ");
            }

            ++count;
        }

        if (artistsName.isEmpty())
            return "";

        return artistsName.substring(0, artistsName.length() - 3);
    }

    public String getArtists() {
        return this.getArtists(4);
    }


    /**
     * 将歌曲扔进垃圾桶 (优化推荐)
     */
//    public void addTrashCan(){
//        this.api.POST_API("/api/radio/trash/add?alg=RT&songId=" + this.id + "&time=25", null);
//    }

    /**
     * 获取歌词
     *
     * @return 滚动歌词对象
     */
    public Lyric lyric() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", this.id);
        data.put("lv", 0);
        data.put("tv", 0);

        return new Lyric(CloudMusic.api.POST("/lyric/new", data).toJson());
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPicUrl() {
        return this.getPicUrl(420);
    }

    @Override
    public String getPicUrl(int size) {
        return this.picUrl + "?param=" + size + "y" + size;
    }

    /**
     * 获得歌曲 url
     *
     * @return 歌曲文件 url
     */
    public String getPlayUrl() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", this.id);
        data.put("level", CloudMusic.quality.getQuality().toLowerCase());
        data.put("encodeType", "mp3");

        JsonObject result = CloudMusic.api.POST("/song/url/v1", data).toJson();
        JsonObject music = result.get("data").getAsJsonArray().get(0).getAsJsonObject();
        if (music.get("code").getAsInt() != 200) {
            throw new RuntimeException(this.name);
        }

        return music.get("url").getAsString();
    }

    @Override
    public long getDuration() {
        return this.duration * 1000;
    }

}
