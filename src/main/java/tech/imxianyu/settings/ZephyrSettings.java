package tech.imxianyu.settings;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import tech.imxianyu.gui.clickgui.ZephyrClickGui;
import tech.imxianyu.music.CloudMusic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ImXianyu
 * @since 4/24/2023 8:48 AM
 */
public class ZephyrSettings {

    public static final BooleanSetting renderSelfNameTag = new BooleanSetting("Render Player's Own Name Tag", true);
    public static final BooleanSetting nameTagNoBg = new BooleanSetting("Name Tag No Background", false);
    public static final BooleanSetting reduceShaders = new BooleanSetting("Reduce Shaders", false);
    public static final BooleanSetting mouseDelayFix = new BooleanSetting("Mouse Delay Fix", true);
    public static final BooleanSetting noClickDelay = new BooleanSetting("No Click Delay", true);
    public static final ModeSetting<ShaderQuality> shaderQuality = new ModeSetting<>("Shader Quality", ShaderQuality.Medium);

    public static final NumberSetting<Integer> volume = new NumberSetting<Integer>("Volume", 30, 1, 100, 1, () -> (Minecraft.getMinecraft().currentScreen instanceof ZephyrClickGui) && (ZephyrClickGui.getInstance().currentPanel == ZephyrClickGui.getInstance().getMusicPanel())) {
        @Override
        public void onValueChanged(Integer last, Integer now) {
            if (CloudMusic.player == null) {
                return;
            }

            CloudMusic.player.setVolume(now.floatValue() / 100.0f);
        }
    };
    public enum ShaderQuality {
        High(40, 1),
        Medium(20, 0.5),
        Low(10, 0.25);

        @Getter
        private final int radius;

        @Getter
        private final double factor;

        ShaderQuality(int radius, double factor) {
            this.radius = radius;
            this.factor = factor;
        }

    }

    @Getter
    private static final List<Setting<?>> settings = new ArrayList<>();
    public static JsonObject config;

    @SneakyThrows
    public static void initialize() {
        settings.clear();

        for (Field field : ZephyrSettings.class.getDeclaredFields()) {
            field.setAccessible(true);

            if (Setting.class.isAssignableFrom(field.getType())) {
                Setting<?> setting = (Setting<?>) field.get(null);
                setting.onInit();
                settings.add(setting);
            }
        }

        if (config != null) {
            config.entrySet().forEach(s -> {
                Setting<?> setting = ZephyrSettings.getSettingByName(s.getKey());

                if (setting != null)
                    setting.loadValue(s.getValue().getAsString());
                else
                    System.out.println(s.getKey());
            });
        }
    }

    public static Setting<?> getSettingByName(String name) {
        for (Setting<?> setting : settings) {
            if (name.equalsIgnoreCase(setting.getName()))
                return setting;
        }

        return null;
    }
}
