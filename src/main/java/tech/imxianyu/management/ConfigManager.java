package tech.imxianyu.management;

import com.google.gson.*;
import lombok.Cleanup;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import tech.imxianyu.Zephyr;
import tech.imxianyu.gui.alt.Alt;
import tech.imxianyu.gui.alt.AltManager;
import tech.imxianyu.gui.clickgui.ZephyrClickGui;
import tech.imxianyu.gui.clickgui.panel.impl.MusicPanel;
import tech.imxianyu.interfaces.AbstractManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.music.CloudMusic;
import tech.imxianyu.rendering.multithreading.AsyncGLContentLoader;
import tech.imxianyu.settings.ZephyrSettings;
import tech.imxianyu.utils.multithreading.MultiThreadingUtil;
import tech.imxianyu.utils.network.HttpClient;
import tech.imxianyu.widget.Widget;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author ImXianyu
 * @since 6/16/2023 9:23 AM
 */
public class ConfigManager extends AbstractManager {

    /**
     * 配置文件目录
     */
    public final File configDir = new File(Minecraft.getMinecraft().mcDataDir, "Zephyr");
    private final File ALT = new File(configDir, "Alts.json");
    public String currentConfig = "Default";

    @Override
    @SneakyThrows
    public void onStart() {
        //判断目录是否存在
        if (!configDir.exists()) {
            //创建文件夹
            configDir.mkdir();
        }

        File noteBot = new File(configDir, "NoteBot");

        if (!noteBot.exists()) {
            noteBot.mkdir();
        }

        File autoSay = new File(configDir, "AutoSay");

        if (!autoSay.exists()) {
            autoSay.mkdir();

            File placeHolder = new File(autoSay, "place_your_txt_here");
            placeHolder.createNewFile();
        }

/*        File lyric = new File(musicCache, "Lyrics");

        if (!lyric.exists()) {
            lyric.mkdir();
        }

        MultiThreadingUtil.runAsync(() -> {
            for (File file : lyric.listFiles(pathname -> pathname.getName().endsWith(".lrc"))) {
                CloudMusic.lyricCache.put(Long.parseLong(file.getName().substring(0, file.getName().indexOf("."))), file.getAbsolutePath());
            }
        });*/

        File curConfigFile = new File(configDir, "Config.json");

        if (!curConfigFile.exists()) {
            curConfigFile.createNewFile();

            FileWriter writer = new FileWriter(curConfigFile);

            writer.write("{\n" +
                    "\t\"Config\": \"Default\",\n" +
                    "\t\"CMCookie\": \"\"\n" +
                    "}");

            writer.flush();
            writer.close();
        }

        File configsFile = new File(configDir, "Profiles");

        if (!configsFile.exists()) {
            configsFile.mkdir();
        }

        @Cleanup
        FileReader reader = new FileReader(curConfigFile);
        JsonElement element = new JsonParser().parse(reader);

        currentConfig = element.getAsJsonObject().get("Config").getAsString();

        String cmCookie = element.getAsJsonObject().get("CMCookie").getAsString();

        CloudMusic.initialize(cmCookie);

        if (CloudMusic.api.hasCookie()) {
            MultiThreadingUtil.runAsync(this::refreshNCM);
        } else {
            System.err.println("HAS NO COOKIE?!");
        }

        this.loadConfig();
        AltManager.init();
        this.loadAlts();
        this.loadFriends();
    }

    public void refreshNCM() {
        CloudMusic.initialize(CloudMusic.api.getCookie());
        MusicPanel.profile = CloudMusic.getUserProfile();
        MusicPanel.playLists = CloudMusic.playLists(MusicPanel.profile.getProfile().getUserId());
        if (!MusicPanel.playLists.isEmpty()) {
            MusicPanel.selectedList = MusicPanel.playLists.get(0);
            ResourceLocation coverForList = MusicPanel.getCoverForList(MusicPanel.selectedList);

            ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(coverForList);

            if (texture == null || texture == TextureUtil.missingTexture) {
                AsyncGLContentLoader.loadGLContentAsync(new Runnable() {
                    @Override
                    @SneakyThrows
                    public void run() {

                        BufferedImage img = ImageIO.read(HttpClient.downloadStream(MusicPanel.selectedList.cover));

                        Minecraft.getMinecraft().getTextureManager().loadTexture(coverForList, new DynamicTexture(img));
                    }
                });
            }
        }

        AsyncGLContentLoader.loadGLContentAsync(new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                InputStream inputStream = HttpClient.downloadStream(MusicPanel.profile.getProfile().getAvatarUrl());
                BufferedImage img = ImageIO.read(inputStream);

                TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

                if (textureManager.getTexture(MusicPanel.avatar) != null)
                    textureManager.deleteTexture(MusicPanel.avatar);

                textureManager.loadTexture(MusicPanel.avatar, new DynamicTexture(img));
            }
        });
    }

    @SneakyThrows
    public void loadFriends() {
        File friendsFile = new File(configDir, "Friends.json");

        if (!friendsFile.exists()) {
            friendsFile.createNewFile();
            return;
        }

        FileReader reader = new FileReader(friendsFile);

        JsonElement json = new JsonParser().parse(reader);

        if (!json.isJsonObject()) {
            return;
        }

        JsonArray array = json.getAsJsonObject().get("friends").getAsJsonArray();

        FriendManager.getFriends().clear();

        for (JsonElement jsonElement : array) {
            FriendManager.getFriends().add(jsonElement.getAsString());
        }
    }

    @SneakyThrows
    public void saveFriends() {
        File friendsFile = new File(configDir, "Friends.json");

        if (!friendsFile.exists()) {
            friendsFile.createNewFile();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject jsonObject = new JsonObject();

        JsonElement array = gson.toJsonTree(FriendManager.getFriends());

        jsonObject.add("friends", array);

        try {
            Writer writer = new FileWriter(friendsFile);
            writer.write(gson.toJson(jsonObject));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            friendsFile.delete();
        }
    }

    public void loadAlts() {
        try {
            if (!ALT.exists()) {
                PrintWriter printWriter = new PrintWriter(new FileWriter(ALT));
                printWriter.println();
                printWriter.close();
            } else {
                AltManager.getAlts().clear();
                Gson gson = (new GsonBuilder()).create();
                Reader reader = new FileReader(ALT);
                JsonArray array = gson.fromJson(reader, JsonArray.class);
                AltManager.getAlts().clear();
                for (JsonElement jsonElement : array) {
                    Alt alt = gson.fromJson(jsonElement, Alt.class);
                    AltManager.getAlts().add(alt);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void saveAlts() {
        try {
            PrintWriter printWriter = new PrintWriter(ALT);
            Gson gson = (new GsonBuilder()).create();
            printWriter.println(gson.toJson(AltManager.getAlts()));
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void loadConfig() {

        //加载模块设置
        //模块配置目录

        File configsFile = new File(configDir, "Profiles");
        File configFile = new File(configsFile, currentConfig + ".json");

        if (!configFile.exists()) {
            configFile.createNewFile();
            return;
        }

        @Cleanup
        FileReader fileReader = new FileReader(configFile);
        JsonElement config = new JsonParser().parse(fileReader);

        JsonObject modules = config.getAsJsonObject().get("Modules").getAsJsonObject();

        modules.entrySet().forEach(m -> {
            Module module = Zephyr.getInstance().getModuleManager().getModuleByName(m.getKey());

            if (module != null) {
                module.loadConfig(m.getValue().getAsJsonObject());
            } else {
                System.out.println(m.getKey());
            }
        });

        JsonObject widgets = config.getAsJsonObject().get("Widgets").getAsJsonObject();

        widgets.entrySet().forEach(w -> {
            Widget widget = Zephyr.getInstance().getWidgetsManager().getWidgetByName(w.getKey());

            if (widget != null) {
                widget.loadConfig(w.getValue().getAsJsonObject());
            } else {
                System.out.println(w.getKey());
            }
        });

//        JsonObject settings = new JsonObject();
//        ZephyrSettings.getSettings().forEach(val -> {
//            settings.addProperty(val.getInternalName(), val.getValueForConfig());
//        });
//        jsonObject.add("Settings", settings);

        JsonObject settings = config.getAsJsonObject().get("Settings").getAsJsonObject();
        ZephyrSettings.config = settings;

    }

    @SneakyThrows
    public void saveConfig() {
        //判断目录是否存在
        if (!configDir.exists()) {
            //创建文件夹
            configDir.mkdir();
        }

        File curConfigFile = new File(configDir, "Config.json");
        curConfigFile.createNewFile();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        FileWriter writer = new FileWriter(curConfigFile);

        JsonObject obj = new JsonObject();

        obj.addProperty("Config", currentConfig);


        /*for (Frame frame : MusicGui.getInstance().frameList.stream().filter(f -> f instanceof SheetFrame).collect(Collectors.toList())) {
            sheetIds.add(((SheetFrame) frame).id);
        }*/

        obj.addProperty("CMCookie", CloudMusic.api.getCookie());
        writer.write(gson.toJson(obj));

        /*writer.write("{\n" +
                "\t\"Config\": \"" + currentConfig + "\"\n" +
                "}");*/

        writer.flush();
        writer.close();

        //模块配置目录
        File configsFile = new File(configDir, "Profiles");
        File configFile = new File(configsFile, currentConfig + ".json");
        JsonObject jsonObject = new JsonObject();

        JsonObject modules = new JsonObject();
        ModuleManager.getModules().forEach(module -> {
            modules.add(module.getName(), module.saveConfig());
        });
        jsonObject.add("Modules", modules);

        JsonObject widgets = new JsonObject();
        WidgetsManager.getWidgets().forEach(widget -> {
            widgets.add(widget.getName(), widget.saveConfig());
        });
        jsonObject.add("Widgets", widgets);

        JsonObject settings = new JsonObject();
        ZephyrSettings.getSettings().forEach(val -> {
            settings.addProperty(val.getName(), val.getValueForConfig());
        });
        jsonObject.add("Settings", settings);

        configFile.createNewFile();
        Writer wt = new FileWriter(configFile);
        wt.write(new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject));
        wt.flush();
        wt.close();

        this.saveAlts();
        this.saveFriends();
    }


    @Override
    public void onStop() {
        this.saveConfig();
    }
}
