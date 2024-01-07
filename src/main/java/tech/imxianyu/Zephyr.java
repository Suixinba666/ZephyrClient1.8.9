package tech.imxianyu;


import com.sun.javafx.application.PlatformImpl;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.src.Config;
import org.lwjglx.opengl.Display;
import tech.imxianyu.eventapi.EventBus;
import tech.imxianyu.eventapi.State;
import tech.imxianyu.events.other.ClientShutdownEvent;
import tech.imxianyu.events.other.ClientStartupEvent;
import tech.imxianyu.interfaces.AbstractManager;
import tech.imxianyu.management.*;
import tech.imxianyu.music.CloudMusic;
import tech.imxianyu.music.IMusic;
import tech.imxianyu.rendering.Blur;
import tech.imxianyu.rendering.Bloom;
import tech.imxianyu.rendering.loadingscreen.ZephyrLoadingScreen;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.ZephyrSettings;
import tech.imxianyu.utils.dev.DevUtils;
import tech.imxianyu.utils.information.Version;
import tech.imxianyu.utils.logging.ZLog;
import tech.imxianyu.utils.platform.PlatformUtils;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * @author ImXianyu
 * @since 3/25/2023 11:03 PM
 */
public class Zephyr {

    /*
    * !!! PLEASE KEEP THE ORIGINAL AUTHOR'S SIGNATURE !!!
    * !!! 请保留原作者署名 !!!
    * */

    /**
     * The instance of the client.
     */
    @Getter
    private static final Zephyr instance = new Zephyr();

    /**
     * Managers list.
     */
    @Getter
    private final List<AbstractManager> managers = new ArrayList<>();

    /**
     * Client version.
     */
    @Getter
    private final Version version = new Version(Version.VersionType.Dev, "230730 (WIP)");

    /**
     * Client version hash.
     */
    @Getter
    private final String versionHash = this.generateVersionHash(this.getVersion().getType() + this.getVersion().getBuildDate());

    /**
     * Module Manager
     */
    @Getter
    private ModuleManager moduleManager;

    /**
     * Widgets Manager
     */
    @Getter
    private WidgetsManager widgetsManager;

    /**
     * Font Manager
     */
    @Getter
    private FontManager fontManager;

    /**
     * Command Manager
     */
    @Getter
    private CommandManager commandManager;

    /**
     * Friend Manager
     */
    @Getter
    private FriendManager friendManager;

    /**
     * Config Manager
     */
    @Getter
    private ConfigManager configManager;

    /**
     * true if client is fully loaded
     */
    public boolean clientLoadFinished = false;

    /**
     * Useless constructor
     */
    public Zephyr() {

    }

    /**
     * Starts the client, called from Minecraft.startGame()
     */
    public void startClient() {
        Display.setTitle(String.format("Zephyr%s %s (%s/master)", this.getVersion().getType() == Version.VersionType.Release ? "" : " " + this.getVersion().getType(), this.getVersion().getBuildDate(), this.getVersionHash()));
        ZephyrLoadingScreen.setProgress(70, "Zephyr - Start");
        ZLog.d("Pre Client Start");

        // call the startup event (currently useless lmfao)
        ClientStartupEvent clientStartUpEvent = new ClientStartupEvent();
        clientStartUpEvent.setState(State.PRE);
        EventBus.call(clientStartUpEvent);

        ZephyrLoadingScreen.setProgress(90, "Zephyr - Managers");

        // initialize managers
        this.moduleManager = new ModuleManager();
        this.widgetsManager = new WidgetsManager();
        this.fontManager = new FontManager();
        this.commandManager = new CommandManager();
        this.friendManager = new FriendManager();
        this.configManager = new ConfigManager();

        // start the managers and register them on the event bus
        for (AbstractManager manager : this.managers) {
            manager.onStart();
            EventBus.register(manager);
        }

        // initialize client settings
        ZephyrSettings.initialize();


        ZLog.d("Post Client Start");
        clientStartUpEvent.setState(State.POST);
        EventBus.call(clientStartUpEvent);

        // config auto-save thread
        this.initConfigAutoSavingThread();

        // shutdown hook
        this.addShutdownHook();

        // JFX Initialization
        this.initJavaFX();

        // init 2d display list (skidded)
        RenderSystem.initDisplayList();

        // update thread priorities
        Config.updateThreadPriorities();

        ZephyrLoadingScreen.setProgress(100, "Zephyr - Finish");

        // now the client is fully loaded
        clientLoadFinished = true;

        PlatformUtils.displayMessage("Zephyr Client", "Zephyr Client is loaded!", TrayIcon.MessageType.INFO);

        RenderSystem.refreshSkinCache();

        // big-god rendering framebuffers
        Blur.blurBuffer = RenderSystem.createFrameBuffer(Blur.blurBuffer);
        Bloom.bloomBuffer = RenderSystem.createFrameBuffer(Bloom.bloomBuffer);

        // play the genshin impact main theme song

        ZLog.d("Finish.");
    }

    private void initConfigAutoSavingThread() {
        new Thread(() -> {
            while (true) {
                this.configManager.saveConfig();
                try {
                    Thread.sleep(1000 * 60 * 5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void initJavaFX() {
        PlatformImpl.startup(() -> {});
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(
                new Runnable() {
                    @Override
                    @SneakyThrows
                    public void run() {
                        Zephyr.getInstance().stopClient();

                        if (Zephyr.getInstance().getVersion().getType() == Version.VersionType.Dev) {
                            if (!DevUtils.missingCharacters.isEmpty()) {
                                for (Character missingCharacter : DevUtils.missingCharacters) {
                                    System.out.print(missingCharacter);
                                }
                                System.out.println();

                                File f = new File("H:\\Projects\\ferrum-client\\src\\main\\java\\tech\\imxianyu\\rendering\\font\\CharUtils.java");

                                if (f.exists()) {
                                    Scanner s = new Scanner(f, StandardCharsets.UTF_8);
                                    s.useDelimiter("\n");

                                    StringBuilder sb = new StringBuilder();
                                    while (s.hasNext()) {
                                        sb.append(s.next());
                                    }

                                    try (FileWriter wr = new FileWriter(f, false)) {
                                        String string = sb.toString();

                                        StringBuilder t = new StringBuilder();

                                        for (Character missingCharacter : DevUtils.missingCharacters) {
                                            t.append(missingCharacter);
                                        }

                                        string = string.replaceAll("public static final String unicodes = \"", "public static final String unicodes = \"" + t);
                                        wr.write(string);
                                    }
                                }

                            }
                        }

                    }
                }
        ));
    }

    /**
     * shuts down the client, called from jvm shutdown hook
     */
    public void stopClient() {
        ZLog.d("Client Shutdown");

        ClientShutdownEvent clientShutdownEvent = new ClientShutdownEvent();
        EventBus.call(clientShutdownEvent);

        for (AbstractManager manager : this.managers) {
            manager.onStop();
            EventBus.unregister(manager);
        }

    }

    /**
     * Generates an 8-digit hash from the input using md5.
     * Used to simulate as a git commit
     * @param input input
     * @return an 8-digit hash from input
     */
    @SneakyThrows
    public String generateVersionHash(String input) {
        input = "Zephyr" + input;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(input.getBytes());
        byte[] byteArray = md5.digest();

        BigInteger bigInt = new BigInteger(1, byteArray);
        // radix 16
        StringBuilder result = new StringBuilder(bigInt.toString(16));
        // insert 0 if the length is less than 32
        while (result.length() < 32) {
            result.insert(0, "0");
        }
        return result.substring(8, 16);
    }

}
