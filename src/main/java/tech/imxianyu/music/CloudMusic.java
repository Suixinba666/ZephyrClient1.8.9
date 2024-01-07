package tech.imxianyu.music;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.ResourceLocation;
import tech.imxianyu.music.dto.Music;
import tech.imxianyu.music.dto.PlayList;
import tech.imxianyu.music.dto.UserProfile;
import tech.imxianyu.rendering.multithreading.AsyncGLContentLoader;
import tech.imxianyu.rendering.notification.Notification;
import tech.imxianyu.rendering.notification.NotificationManager;
import tech.imxianyu.settings.ZephyrSettings;
import tech.imxianyu.utils.audio.AudioPlayer;
import tech.imxianyu.utils.multithreading.MultiThreadingUtil;
import tech.imxianyu.utils.network.HttpClient;
import tech.imxianyu.widget.impl.MusicLyrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.*;

/**
 * @author ImXianyu
 * @since 6/16/2023 9:34 AM
 */
public class CloudMusic {

    @Getter
    private static final Map<String, String> headers = new HashMap<>();
    public static int averageColor = new Color(27, 32, 45).getRGB();
    public static HttpClient api;
    public static Quality quality = Quality.STANDARD;
    public static AudioPlayer player;
    public static List<IMusic> playList = new ArrayList<>();
    public static int curIdx = 0;
    public static IMusic currentlyPlaying;
    public static Thread playThread;

    public static void back() {

        if (curIdx - 1 < 0)
            return;

        if (player != null && !playList.isEmpty()) {

            curIdx--;

            player.close();

        }
    }

    public static void next() {
        if (curIdx + 1 > playList.size() - 1)
            return;

        if (player != null && !playList.isEmpty()) {

            curIdx++;

            player.close();

        }
    }

    public static void setIdx(int idx) {
        if (idx < 0 || idx > playList.size() - 1)
            return;

        if (player != null) {

            curIdx = idx;

            player.close();

        }
    }

    @SneakyThrows
    public static void play(List<IMusic> songs) {
        if (playThread != null) {
            playThread.stop();
        }

        playThread = new Thread(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                playList = songs;

                int idx = 0;
                curIdx = 0;

                for (int i = 0; i < playList.size(); i++) {
//                    System.out.println("NEXT");
                    IMusic song = playList.get(i);

                    if (playList != songs) {
                        break;
                    }

                    if (idx != curIdx) {
                        song = playList.get(curIdx);
                        idx = curIdx;
                        i = curIdx;
                    }

                    if (player != null && !player.isFinished()) {
                        player.close();

                        Thread.sleep(250);
                    }
                    currentlyPlaying = song;

                    String playUrl = song.getPlayUrl();

                    if (player == null) {
                        player = new AudioPlayer(playUrl);
                        player.setVolume(ZephyrSettings.volume.getValue() / 100.0f);
                    } else {
                        player.setAudio(playUrl);
                    }

                    IMusic finalSong = song;

                    MultiThreadingUtil.runAsync(() -> {

                        ResourceLocation musicCover = tech.imxianyu.widget.impl.Music.getMusicCover(finalSong);
                        ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(musicCover);

                        if (texture == null || texture == TextureUtil.missingTexture) {
                            InputStream inputStream = HttpClient.downloadStream(finalSong.getPicUrl(160), 5);

                            BufferedImage img;

                            try {
                                img = ImageIO.read(inputStream);
                            } catch (IOException e) {
                                return;
                            }

                            BufferedImage finalImg = img;
                            AsyncGLContentLoader.loadGLContentAsync(() -> Minecraft.getMinecraft().getTextureManager().loadTexture(musicCover, new DynamicTexture(finalImg)));
                        }

                        ResourceLocation musicCoverLarge = tech.imxianyu.widget.impl.Music.getMusicCoverLarge(finalSong);
                        ITextureObject textureLarge = Minecraft.getMinecraft().getTextureManager().getTexture(musicCoverLarge);

                        if (textureLarge == null || textureLarge == TextureUtil.missingTexture) {
                            InputStream inputStream = HttpClient.downloadStream(finalSong.getPicUrl(), 5);

                            BufferedImage img;
                            try {
                                img = ImageIO.read(inputStream);
                            } catch (IOException e) {
                                return;
                            }

                            BufferedImage finalImg = img;
                            AsyncGLContentLoader.loadGLContentAsync(() -> Minecraft.getMinecraft().getTextureManager().loadTexture(musicCoverLarge, new DynamicTexture(finalImg)));
                        }

                    });

                    player.unpause();
//                    player.setAfterPlayed(() -> {
//                        currentlyPlaying = null;
//                    });
                    loadLyric(song);
                    System.out.println("Now Playing " + song.getName() + " - " + song.getArtists());
                    NotificationManager.show("Now Playing", Formatting.GOLD + song.getName() + " - " + song.getArtists(), Notification.Type.INFO, 8000);
                    player.play();
                    while (!player.isFinished()) {
                        Thread.yield();
                    }
//                    System.out.println("STOPPED");
                    if (idx == curIdx) {
                        idx++;
                        curIdx++;
                    }

                    //Wait
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) {

                    }
                }
            }
        }, "Play Thread");
        playThread.start();
    }

    public static void loadLyric(IMusic song) {
        MultiThreadingUtil.runAsync(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {

                Map<String, Object> postData = new HashMap<>();
                postData.put("id", song.getId());
                postData.put("timestamp", System.currentTimeMillis());

                JsonObject json = api.POST("/lyric/new", postData).toJson();
//                String lyrics = json.getAsJsonObject("lrc").get("lyric").getAsString();

/*                if (lyrics.isEmpty()) {
                    MusicLyrics.initLyric("[00:00.000] No Lyric");
                    return;
                }*/

                MusicLyrics.initLyric(json);

            }
        });
    }

    public static void initialize(String cookie) {
        api = new HttpClient("http://localhost:3000");
        api.setCookie(cookie);
    }

    public static String qrCodeLogin() {
        String key = CloudMusic.qrKey();

        QRCodeGenerator.generateAndLoadTexture("https://music.163.com/login?codekey=" + key);

        while (true) {

            HttpClient.HttpResult result = qrCheck(key);
            JsonObject json = result.toJson();

            int code = json.get("code").getAsInt();
            if (code == 800) {
                key = CloudMusic.qrKey();

                QRCodeGenerator.generateAndLoadTexture("https://music.163.com/login?codekey=" + key);
            }

            if (code == 803) {
//                String cookie = json.get("cookie").getAsString();
//
//                Map<String, String> cookiesMap = new HashMap<>();
//                String[] cookiekeys = cookie.split("; ")[0].split("=");
//                if (cookiekeys.length == 1) {
//                    cookiesMap.put(cookiekeys[0], "");
//                    continue;
//                }
//                cookiesMap.put(cookiekeys[0], cookiekeys[1]);
//
//                StringBuilder cookieData = new StringBuilder();
//                for (Map.Entry<String, String> cookieKeys2 : cookiesMap.entrySet()) {
//                    cookieData.append(cookieKeys2.getKey()).append("=").append(cookieKeys2.getValue()).append("; ");
//                }
//                return cookieData.toString();

                String cookie = json.get("cookie").getAsString();

                String[] split = cookie.split(";");
                StringBuilder sb = new StringBuilder();
                for (String s : split) {
                    if (s.contains("MUSIC_U") || s.contains("__csrf")) {
                        sb.append(s).append("; ");
                    }
                }

                return sb.substring(0, sb.length() - 2);
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        getUserProfile();
        List<IMusic> search = search("Neverland");

        for (IMusic iMusic : search) {
            System.out.println(iMusic.getName() + ": " + iMusic.getId());
            System.out.println(iMusic.getPlayUrl());
        }

    }

    public static UserProfile getUserProfile() {

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", System.currentTimeMillis());

        JsonObject jsonObject = api.POST("/login/status", data).toJson();
//        System.out.println(jsonObject.getAsJsonObject("data"));
//        UserProfile userProfile = gson.fromJson(jsonObject.getAsJsonObject("data"), UserProfile.class);
        UserProfile userProfile = new UserProfile(jsonObject.getAsJsonObject("data"));
//        System.out.println(jsonObject);
        return userProfile;
    }

    public static List<PlayList> playLists(int userId) {

        Map<String, Object> postData = new HashMap<String, Object>();
        postData.put("uid", userId);
        postData.put("limit", 30);
        postData.put("offset", 0);
        postData.put("includeVideo", "true");
        postData.put("timestamp", System.currentTimeMillis());

        JsonObject data = api.POST("/user/playlist", postData).toJson();

        List<PlayList> playLists = new ArrayList<>();
        data.get("playlist").getAsJsonArray().forEach(playList -> {
            playLists.add(new PlayList(playList.getAsJsonObject()));
//            System.out.println(playList.getAsJsonObject());
        });

        return playLists;
    }

    @SneakyThrows
    private static Map<String, Object> searchData(String key, int type) {
        Map<String, Object> data = new HashMap<>();
        data.put("keywords", URLEncoder.encode(key, "UTF-8"));
        data.put("type", type);
        data.put("limit", 30);
        data.put("offset", 0);
        data.put("total", true);
        return data;
    }

    public static List<IMusic> search(String keyWord) {
        List<IMusic> list = new ArrayList<>();

        Map<String, Object> data = searchData(keyWord, 1);
        data.put("timestamp", System.currentTimeMillis());
        JsonObject json = api.POST("/cloudsearch", data).toJson();

        JsonArray songs = null;
        try {
            JsonObject result = json.getAsJsonObject("result");
            if (result != null) {
                songs = result.getAsJsonArray("songs");

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (songs != null) {
            for (JsonElement song : songs) {
                IMusic m = new Music(song.getAsJsonObject(), null);
                list.add(m);
            }
        }
//        JsonObject data = post.toJson();


        return list;
    }


    public static String qrKey() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("type", 1);
        data.put("timestamp", System.currentTimeMillis());

        JsonObject json = api.POST("/login/qr/key", data).toJson();
        return json.getAsJsonObject("data").get("unikey").getAsString();
    }

    private static HttpClient.HttpResult qrCheck(String qrKey) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("key", qrKey);
        data.put("type", 1);
        data.put("timestamp", System.currentTimeMillis());

        return api.POST("/login/qr/check", data);
    }

    /**
     * 获取歌曲
     *
     * @param id 歌曲 id
     * @return 歌曲对象
     */
    public static Music music(long id) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ids", id);

        JsonArray json = api.POST("/song/detail", data).toJson().getAsJsonArray("songs");
        if (json.size() == 0) {
            throw new RuntimeException("NOT FOUND");
        }
        return new Music(json.get(0).getAsJsonObject(), null);
    }

}
