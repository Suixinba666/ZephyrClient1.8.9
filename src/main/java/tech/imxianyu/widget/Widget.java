package tech.imxianyu.widget;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import tech.imxianyu.eventapi.EventBus;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.settings.Setting;
import tech.imxianyu.widget.direction.HorizontalDirection;
import tech.imxianyu.widget.direction.VerticalDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author ImXianyu
 * @since 6/17/2023 9:52 AM
 */
public abstract class Widget {

    @Getter
    private final String name;
    @Getter
    private final List<Setting<?>> settings = new ArrayList<>();
    public Minecraft mc = Minecraft.getMinecraft();
    public boolean hasToggleSound = false;
    @Getter
    @Setter
    private String description = "No Description.";
    @Getter
    private boolean enabled;
    @Getter
    @Setter
    private Supplier<Boolean> shouldRender = () -> true;

    @Getter
    @Setter
    private double x, y, width, height;

    @Getter
    @Setter
    private double moveX, moveY;

    public double resizeX, resizeY;

    @Getter
    @Setter
    private boolean movable = true;

    @Getter
    private boolean resizable = false;

    public double defaultWidth, defaultHeight;

    public HorizontalDirection horizontalDirection = HorizontalDirection.None;
    public VerticalDirection verticalDirection = VerticalDirection.None;

    public Widget(String name) {
        this.name = name;
    }

    public void setResizable(boolean bl, double defaultWidth, double defaultHeight) {

        this.resizable = bl;

        if (bl) {
            this.defaultWidth = defaultWidth;
            this.defaultHeight = defaultHeight;
            this.width = defaultWidth;
            this.height = defaultHeight;
        }

    }

    public void addSettings(Setting<?>... settings) {
        for (Setting<?> setting : settings) {
            this.settings.add(setting);
            setting.onInit(this);
        }
    }

    public void onEnable() {

    }

    public abstract void onRender(Render2DEvent event, boolean editing);


    public void onDisable() {

    }

    public void onResized(double lastWidth, double lastHeight) {

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

    public void toggle() {
        this.enabled = !this.enabled;

        if (this.isEnabled()) {
            EventBus.register(this);
            this.onEnable();
            if (mc.thePlayer != null && hasToggleSound) {
                mc.thePlayer.playSound("random.click", 1.0f, 0.6f);
            }
        } else {
            EventBus.unregister(this);
            this.onDisable();
            if (mc.thePlayer != null && mc.theWorld != null && hasToggleSound) {
                mc.thePlayer.playSound("random.click", 1.0f, 0.5f);
            }
        }

//        if (hasToggleSound)
//            NotificationManager.show((this.isEnabled ? "Enabled" : "Disabled") + " Module " + this.getName() + ".", Notification.Type.INFO);

    }

    public void loadConfig(JsonObject directory) {
        directory.entrySet().forEach(data -> {
            String key = data.getKey();
            JsonElement value = data.getValue();
            switch (key) {
                case "Enabled" -> {
                    if (!(this.isEnabled() && value.getAsBoolean())
                            && !(!this.isEnabled() && !value.getAsBoolean())) {
                        this.setEnabled(value.getAsBoolean());
                    }
                }
                case "PosX" -> this.setX(value.getAsDouble());
                case "PosY" -> this.setY(value.getAsDouble());
                case "Width" -> this.setWidth(value.getAsDouble());
                case "Height" -> this.setHeight(value.getAsDouble());
                case "HDirection" -> this.horizontalDirection = HorizontalDirection.valueOf(value.getAsString());
                case "VDirection" -> this.verticalDirection = VerticalDirection.valueOf(value.getAsString());
                default -> {
                    Setting<?> val = this.find(key);
                    if (val != null) {
                        val.loadValue(value.getAsString());
                    }
                }
            }
        });
    }

    public JsonObject saveConfig() {
        JsonObject directory = new JsonObject();
        directory.addProperty("Enabled", this.isEnabled());
        directory.addProperty("PosX", this.getX());
        directory.addProperty("PosY", this.getY());
        directory.addProperty("Width", this.getWidth());
        directory.addProperty("Height", this.getHeight());
        directory.addProperty("HDirection", this.horizontalDirection.name());
        directory.addProperty("VDirection", this.verticalDirection.name());
        this.settings.forEach(val -> directory.addProperty(val.getName(), val.getValueForConfig()));

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

}
