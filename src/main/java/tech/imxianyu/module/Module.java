package tech.imxianyu.module;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Formatting;
import org.lwjglx.input.Keyboard;
import tech.imxianyu.eventapi.EventBus;
import tech.imxianyu.module.submodule.SubModule;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.Setting;
import tech.imxianyu.settings.StringModeSetting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author ImXianyu
 * @since 4/8/2023 4:14 PM
 */
public class Module {

    @Getter
    private final String name;
    @Getter
    private final Category category;
    @Getter
    private final List<Setting<?>> settings = new ArrayList<>();
    @Getter
    private final List<SubModule<?>> subModules = new ArrayList<>();
    public Minecraft mc = Minecraft.getMinecraft();
    public boolean hasToggleSound = false;
    @Getter
    @Setter
    private String description = "No Description.";
    @Getter
    @Setter
    private int keyBind = Keyboard.KEY_NONE;
    @Getter
    @Setter
    private String suffix = "";
    @Getter
    private boolean enabled;
    @Getter
    @Setter
    private Supplier<Boolean> shouldRender = () -> true;

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;

//        ModuleManager.getModules().add(this);
    }

    public void addSettings(Setting<?>... settings) {
        for (Setting<?> setting : settings) {
            this.settings.add(setting);
            setting.onInit(this);
        }
    }

    @SneakyThrows
    public void addSubModules(SubModule<?>... subModules) {
        this.subModules.addAll(Arrays.asList(subModules));

        List<String> names = new ArrayList<>();

        for (SubModule subModule : subModules) {
            subModule.setModule(this);
            names.add(subModule.getName());
        }

        StringModeSetting subModes = new StringModeSetting("Mode", names.get(0), names) {
            @Override
            public void onModeChanged(String before, String now) {

                SubModule<?> bef = getSubModuleByName(before);
                EventBus.unregister(bef);
                bef.onDisable();

                if (isEnabled()) {
                    SubModule<?> aft = getSubModuleByName(now);
                    EventBus.register(aft);
                    aft.onEnable();
                }
            }
        };

        for (SubModule<?> subModule : subModules) {
            for (Field declaredField : subModule.getClass().getDeclaredFields()) {
                declaredField.setAccessible(true);

                if (Setting.class.isAssignableFrom(declaredField.getType())) {
                    Setting<?> setting = (Setting<?>) declaredField.get(subModule);
                    final Supplier<Boolean> beforeShow = setting.getShouldRender();
                    setting.setShouldRender(() -> subModes.getValue().equals(subModule.getName()) && beforeShow.get());
                    this.addSettings(setting);
                }
            }
        }

        this.settings.add(0, subModes);
    }

//    @SneakyThrows
//    public void reInitSubModules() {
//
//        if (this.subModules.isEmpty())
//            return;
//
//        DynamicEnumUtil.clearEnum(Mode.class);
//        for (SubModule subModule : subModules) {
//            subModule.setModule(this);
//            DynamicEnumUtil.addEnum(Mode.class, subModule.getName(), new Class[]{}, new Object[]{});
//        }
//
//        ModeSetting<Mode> subModes = new ModeSetting<Mode>("Mode", Mode.values()[0]) {
//            @Override
//            public void onModeChanged(Mode before, Mode now) {
//
//                SubModule<?> bef = getSubModuleByName(before.name());
//                EventBus.unregister(bef);
//                bef.onDisable();
//
//                if (isEnabled()) {
//                    SubModule<?> aft = getSubModuleByName(now.name());
//                    EventBus.register(aft);
//                    aft.onEnable();
//                }
//            }
//        };
//
//        for (SubModule<?> subModule : subModules) {
//            for (Field declaredField : subModule.getClass().getDeclaredFields()) {
//                declaredField.setAccessible(true);
//
//                if (Setting.class.isAssignableFrom(declaredField.getType())) {
//                    Setting<?> setting = (Setting<?>) declaredField.get(subModule);
//                    final Supplier<Boolean> beforeShow = setting.getShouldRender();
//                    setting.setShouldRender(() -> subModes.getCurMode().equals(subModule.getName()) && beforeShow.get());
//                    this.addSettings(setting);
//                }
//            }
//        }
//
//        this.settings.add(0, subModes);
//    }

    public SubModule<?> getSubModuleByName(String name) {
        if (subModules.isEmpty())
            return null;

        for (SubModule<?> subModule : subModules) {
            if (name.equals(subModule.getName()))
                return subModule;
        }

        return null;
    }

    public SubModule<?> getCurrentSubModule() {
        if (subModules.isEmpty())
            return null;

        for (SubModule<?> subModule : subModules) {
            if (this.getSubModes().getValue().equals(subModule.getName()))
                return subModule;
        }

        return null;
    }

    public StringModeSetting getSubModes() {
        return (StringModeSetting) this.settings.get(0);
    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (this.isEnabled()) {

            EventBus.register(this);
            this.onEnable();
            if (mc.thePlayer != null && hasToggleSound) {
                mc.thePlayer.playSound("random.click", 1.0f, 0.6f);
            }
        } else {

            EventBus.unregister(this);
            this.onDisable();
            if (mc.thePlayer != null && hasToggleSound) {
                mc.thePlayer.playSound("random.click", 1.0f, 0.5f);
            }
        }
    }

    public void print(String format, Object... args) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;
        mc.thePlayer.addChatMessage(Formatting.AQUA + "[" + this.getName() + "] " + Formatting.RESET + String.format(format, args));
    }

    public void toggle() {
        this.enabled = !this.enabled;

        SubModule<?> subModule = this.getCurrentSubModule();
        if (this.isEnabled()) {

            if (subModule != null) {
                EventBus.register(subModule);
                subModule.onEnable();
            }
            EventBus.register(this);
            this.onEnable();
            if (mc.thePlayer != null && hasToggleSound) {
                mc.thePlayer.playSound("random.click", 1.0f, 0.6f);
            }
        } else {
            if (subModule != null) {
                EventBus.unregister(subModule);
                subModule.onDisable();
            }
            EventBus.unregister(this);
            this.onDisable();
            if (mc.thePlayer != null && mc.theWorld != null && hasToggleSound) {
                mc.thePlayer.playSound("random.click", 1.0f, 0.5f);
            }
        }
    }

    public void loadConfig(JsonObject directory) {
        directory.entrySet().forEach(data -> {
            switch (data.getKey()) {
                case "Key":
                    this.setKeyBind(data.getValue().getAsInt());
                    break;
                case "Enabled":
                    if (!(this.isEnabled() && data.getValue().getAsBoolean())
                            && !(!this.isEnabled() && !data.getValue().getAsBoolean())) {
                        this.setEnabled(data.getValue().getAsBoolean());
                    }
                    break;
            }
            Setting<?> val = this.find(data.getKey());
            if (val != null) {
                val.loadValue(data.getValue().getAsString());
            }
        });
    }

    public JsonObject saveConfig() {
        JsonObject directory = new JsonObject();
        directory.addProperty("Key", this.getKeyBind());
        directory.addProperty("Enabled", this.isEnabled());
        this.settings.forEach(val -> {
            directory.addProperty(val.getName(), val.getValueForConfig());
        });

        return directory;
    }

    public Setting<?> find(final String term) {
        for (Setting<?> setting : this.settings) {
            if (setting.getName().equalsIgnoreCase(term)) {
                return setting;
            }
        }
        return null;
    }

    public enum Category {
        COMBAT("Combat", "a"),
        MOVEMENT("Movement", "b"),
        RENDER("Render", "c"),
        PLAYER("Player", "d"),
        WORLD("World", "e"),
        EXPLOIT("Exploit", "f"),
        OTHER("Other", "g");


        @Getter
        private final String name;

        @Getter
        private final String icon;

        Category(String name, String icon) {
            this.name = name;
            this.icon = icon;
        }

        public static int getInIndex(Category cat) {
            List<Category> categories = Arrays.asList(Category.values());

            return categories.indexOf(cat);
        }
    }
}
