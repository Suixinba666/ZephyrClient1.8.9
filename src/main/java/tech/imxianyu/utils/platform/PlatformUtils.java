package tech.imxianyu.utils.platform;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import tech.imxianyu.Zephyr;
import tech.imxianyu.utils.information.Version;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * @author ImXianyu
 * @since 7/4/2023 3:15 PM
 */
public class PlatformUtils {

    public static boolean supported = SystemTray.isSupported();

    public static TrayIcon trayIcon;
    @SneakyThrows
    public static void initialize() {
        if (!supported) {
            System.err.println("System Tray Isn't Supported!");
            return;
        }

        URL resource = PlatformUtils.class.getClassLoader().getResource("assets/minecraft/Zephyr/textures/logo_128x.png");

        if (resource == null) {
            supported = false;
            return;
        }

        ImageIcon icon = new ImageIcon(resource);

        trayIcon = new TrayIcon(icon.getImage(), String.format("Zephyr%s %s (%s/master)", Zephyr.getInstance().getVersion().getType() == Version.VersionType.Release ? "" : " " + Zephyr.getInstance().getVersion().getType(), Zephyr.getInstance().getVersion().getBuildDate(), Zephyr.getInstance().getVersionHash()), buildMenu());
        trayIcon.setImageAutoSize(true);
        SystemTray.getSystemTray().add(trayIcon);
    }

    public static void displayMessage(String title, String content, TrayIcon.MessageType type) {
        if (!supported)
            return;

        trayIcon.displayMessage(title, content, type);
    }

    private static PopupMenu buildMenu() {
        PopupMenu menu = new PopupMenu();

        menu.addSeparator();

        MenuItem exitButton = PlatformUtils.buildItem("Exit", (e) -> {
            Minecraft.getMinecraft().shutdown();
        });
        menu.add(exitButton);

        return menu;
    }

    private static MenuItem buildItem(String label, ActionListener listener) {
        MenuItem item = new MenuItem(label);
        item.addActionListener(listener);
        return item;
    }

}
