package tech.imxianyu.music.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import tech.imxianyu.music.CloudMusic;
import tech.imxianyu.music.IMusic;
import tech.imxianyu.music.IMusicList;
import tech.imxianyu.utils.multithreading.MultiThreadingUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 歌单对象
 */
public class PlayList implements IMusicList {
    public final long id;
    public final String name;
    @Getter
    private final RenderValues renderValues = new RenderValues();
    public String cover;
    public int count;
    public long playCount;
    public JsonObject creator;
    public String[] description;
    public JsonArray tags;
    public List<IMusic> musics;

    public boolean texturesLoaded = false;
    public double scrollSmooth = 0;
    public double scrollOffset = 0;
    public boolean searchMode = false;
    private JsonArray songs;

    public PlayList(JsonObject data) {

        JsonObject playlist;
        if (data.has("playlist")) {
            playlist = data.get("playlist").getAsJsonObject();
        } else {
            playlist = data;
        }

        this.id = playlist.get("id").getAsLong();
        this.name = playlist.get("name").getAsString();
        this.cover = playlist.get("coverImgUrl").getAsString();
        this.count = playlist.get("trackCount").getAsInt();
        this.playCount = playlist.get("playCount").getAsLong();
        this.creator = playlist.get("creator").getAsJsonObject();
        if (!playlist.get("description").isJsonNull()) {
            this.description = playlist.get("description").getAsString().split("\n");
        } else {
            this.description = null;
        }
        this.tags = playlist.get("tags").getAsJsonArray();

        MultiThreadingUtil.runAsync(() -> {
            Map<String, Object> postData = new HashMap<>();
            postData.put("id", this.id);
            postData.put("n", 100000);
            postData.put("s", 8);

            this.songs = CloudMusic.api.POST("/playlist/track/all", postData).toJson().getAsJsonArray("songs");
        });

    }

    //Search
    public PlayList() {
        searchMode = true;
        this.id = 0;
        this.name = "Search";
    }

    public final List<IMusic> emptyList = Lists.newArrayList();

    @Override
    public List<IMusic> getMusics() {
        if (this.musics != null && (this.songs != null || searchMode)) {
            return this.musics;
        }

        if (this.songs != null) {
            this.musics = new ArrayList<>();
            if (this.songs.size() != 0) {
                this.songs.forEach(element -> {
                    this.musics.add(new Music(element.getAsJsonObject(), null));
                });
            }

            return this.musics;

        } else {
            return emptyList;
        }

    }

    public class RenderValues {

        public float hoveredAlpha = 0;

    }

}
