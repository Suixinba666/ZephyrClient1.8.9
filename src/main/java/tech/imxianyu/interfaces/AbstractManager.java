package tech.imxianyu.interfaces;

import net.minecraft.client.Minecraft;
import tech.imxianyu.Zephyr;

/**
 * the abstract layer of the manager
 * @author ImXianyu
 * @since 3/26/2023 1:07 PM
 */
public abstract class AbstractManager {

    public Minecraft mc = Minecraft.getMinecraft();

    public AbstractManager() {
        Zephyr.getInstance().getManagers().add(this);
    }

    //管理器初始化
    public abstract void onStart();

    //管理器停止
    public abstract void onStop();
}
