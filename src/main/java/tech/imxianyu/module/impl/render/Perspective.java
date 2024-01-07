package tech.imxianyu.module.impl.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import org.lwjglx.input.Keyboard;
import org.lwjglx.opengl.Display;
import tech.imxianyu.eventapi.EventBus;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.input.MouseXYChangeEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.ModeSetting;

public class Perspective extends Module {
    public static boolean perspectiveToggled;
    public static Minecraft mc = Minecraft.getMinecraft();
    private static float cameraYaw;
    private static float cameraPitch;
    private static int previousPerspective;

    static {
        perspectiveToggled = false;
        cameraYaw = 0.0f;
        cameraPitch = 0.0f;
        previousPerspective = 0;
    }

    ModeSetting<Mode> click = new ModeSetting<>("Mode", Mode.Hold);
    @Handler
    public void onTick(TickEvent event) {
        if (mc.thePlayer != null && mc.theWorld != null) {
            if (click.getValue() == Mode.Hold)
                if (!Keyboard.isKeyDown(super.getKeyBind())) {
                    super.setEnabled(false);
                }
        }
    };

    public Perspective() {
        super("Perspective", Category.RENDER);
        super.setKeyBind(Keyboard.KEY_LMENU);
    }

    public static float getCameraYaw() {
        return perspectiveToggled ? cameraYaw : mc.getRenderViewEntity().rotationYaw;
    }

    public static float getCameraPitch() {
        return perspectiveToggled ? cameraPitch : mc.getRenderViewEntity().rotationPitch;
    }

    public static float getCameraPrevYaw() {
        return perspectiveToggled ? cameraYaw : mc.getRenderViewEntity().prevRotationYaw;
    }

    public static float getCameraPrevPitch() {
        return perspectiveToggled ? cameraPitch : mc.getRenderViewEntity().prevRotationPitch;
    }

    public static boolean overrideMouse() {
        if ((mc.inGameHasFocus && Display.isActive()) /*|| ((mc.currentScreen == null || mc.currentScreen instanceof GuiIngameMenu) && EventBus.canReceive(MouseXYChangeEvent.class))*/) {
            if (!perspectiveToggled) {
                return true;
            }
            mc.mouseHelper.mouseXYChange();
            float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            float f2 = f1 * f1 * f1 * 8.0f;
            float f3 = mc.mouseHelper.deltaX * f2;
            float f4 = mc.mouseHelper.deltaY * f2;
            cameraYaw += f3 * 0.15f;
            cameraPitch += f4 * 0.15f;
            if (cameraPitch > 90.0f) {
                cameraPitch = 90.0f;
            }
            if (cameraPitch < -90.0f) {
                cameraPitch = -90.0f;
            }
        }
        return false;
    }

    @Override
    public void onEnable() {

        if (mc.thePlayer != null) {
            perspectiveToggled = true;
            cameraYaw = mc.thePlayer.rotationYaw;
            cameraPitch = mc.thePlayer.rotationPitch;
            previousPerspective = mc.gameSettings.thirdPersonView;
            mc.gameSettings.thirdPersonView = 1;
        }

    }

    @Override
    public void onDisable() {
        perspectiveToggled = false;
        mc.gameSettings.thirdPersonView = previousPerspective;
    }

    enum Mode {
        Click, Hold
    }
}
