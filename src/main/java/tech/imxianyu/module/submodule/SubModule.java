package tech.imxianyu.module.submodule;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import tech.imxianyu.module.Module;

public class SubModule<T extends Module> {

    @Getter
    private final String name;
    public Minecraft mc = Minecraft.getMinecraft();
    @Getter
    @Setter
    private T module;

    public SubModule(String name) {
        this.name = name;
    }

    public void onEnable() {

    }

    public void onDisable() {

    }


}
