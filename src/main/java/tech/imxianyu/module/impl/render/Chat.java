package tech.imxianyu.module.impl.render;

import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.settings.ZephyrSettings;

public class Chat extends Module {

    public BooleanSetting fastChat = new BooleanSetting("Fast Chat", false);
    public BooleanSetting blur = new BooleanSetting("Blur", false, () -> !fastChat.getValue());
    public BooleanSetting animation = new BooleanSetting("Chat Animation", false);
    public BooleanSetting shadow = new BooleanSetting("Shadow", false);
    public NumberSetting<Double> shadowRadius = new NumberSetting<>("Shadow Radius", 4.0, 0.0, 10.0, 0.5, () -> shadow.getValue() && ZephyrSettings.reduceShaders.getValue());
    public BooleanSetting clientChat = new BooleanSetting("Use client font renderer", false);

    public Chat() {
        super("Chat", Category.RENDER);
    }
}
