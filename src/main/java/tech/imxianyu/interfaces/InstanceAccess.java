package tech.imxianyu.interfaces;

import net.minecraft.client.Minecraft;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public interface InstanceAccess {
    Minecraft mc = Minecraft.getMinecraft();
    Executor threadPool = Executors.newFixedThreadPool(2);

}
