package tech.imxianyu.rendering.titlebar;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.lwjglx.input.Mouse;
import org.lwjglx.opengl.Display;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.Blur;
import tech.imxianyu.rendering.TexturedShadow;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.ShaderBlur;
import tech.imxianyu.utils.math.MathUtils;
import tech.imxianyu.utils.timing.Timer;

import java.util.Optional;

public class TitleBar {

    @Getter
    private static final TitleBar instance = new TitleBar();

    private final Minecraft mc = Minecraft.getMinecraft();

    public static final boolean ENABLED = false;

    Framebuffer blurBuffer;

    public TitleBar() {

    }

    public static double getHeight() {
        return ENABLED ? 14 : 0;
    }

    public void render(double mouseX, double mouseY) {

        if (!ENABLED)
            return;

        mouseX *= RenderSystem.getScaleFactor();
        mouseY *= RenderSystem.getScaleFactor();

        Optional.ofNullable(blurBuffer).ifPresent(
                buffer -> ShaderBlur.render(buffer.framebufferTexture, 1.3f)
        );

        this.initBuffers();

        double titleBarHeight = 14;

        this.doRectBlur(0, 0, RenderSystem.getWidth(), titleBarHeight);

        TexturedShadow.drawBottomShadow(0, titleBarHeight, RenderSystem.getWidth(), 1, 8);

        this.moveWindow(mouseX, mouseY);

        Image.draw(new ResourceLocation("icons/icon_16x16.png"), 4, 3, 8, 8, Image.Type.Normal);

        ZFontRenderer fontRenderer = FontManager.pf12;

        fontRenderer.drawString(Display.getTitle(), 16, titleBarHeight * 0.5 - fontRenderer.getHeight() * 0.5, RenderSystem.hexColor(255, 255, 255, 200));

        this.renderButtons(mouseX, mouseY);
    }

    float closeButtonAlpha;
    float hideButtonAlpha;
    float windowFullButtonAlpha;
    boolean prevMouse = false;

    private void renderButtons(double mouseX, double mouseY) {
        closeButtonAlpha = MathUtils.clamp(closeButtonAlpha, 0.1f, 0.5f);
        windowFullButtonAlpha = MathUtils.clamp(windowFullButtonAlpha, 0.1f, 0.5f);
        hideButtonAlpha = MathUtils.clamp(hideButtonAlpha, 0.1f, 0.5f);

        ResourceLocation circle = new ResourceLocation("Zephyr/textures/titlebar/circle.png");

        RenderSystem.color(RenderSystem.reAlpha(-1, closeButtonAlpha));
        Image.draw(circle, RenderSystem.getWidth() - 10, 4.5, 4.5, 4.5, Image.Type.NoColor);

        RenderSystem.color(RenderSystem.reAlpha(-1, windowFullButtonAlpha));
        Image.draw(circle, RenderSystem.getWidth() - 10 - 7.5, 4.5, 4.5, 4.5, Image.Type.NoColor);

        RenderSystem.color(RenderSystem.reAlpha(-1, hideButtonAlpha));
        Image.draw(circle, RenderSystem.getWidth() - 10 - 15, 4.5, 4.5, 4.5, Image.Type.NoColor);

        if (RenderSystem.isHovered(mouseX, mouseY, RenderSystem.getWidth() - 10, 4.5, 4.5, 4.5)) {//Close button
            closeButtonAlpha = AnimationSystem.getAnimationState(closeButtonAlpha, 0.5f, 0.2f);
            if (Mouse.isButtonDown(0)) {
                prevMouse = true;
            }
            if (!Mouse.isButtonDown(0) && prevMouse) {
                prevMouse = false;
                mc.shutdown();
            }
        } else {
            closeButtonAlpha = AnimationSystem.getAnimationState(closeButtonAlpha, 0.1f, 0.2f);
        }
        if (RenderSystem.isHovered(mouseX, mouseY, RenderSystem.getWidth() - 10 - 7.5, 4.5, 4.5, 4.5)) {//Window full button
            windowFullButtonAlpha = AnimationSystem.getAnimationState(windowFullButtonAlpha, 0.5f, 0.2f);
            if (Mouse.isButtonDown(0)) {
                prevMouse = true;
            }
            if (!Mouse.isButtonDown(0) && prevMouse) {
                prevMouse = false;
//                GLFW.glfwSetWindowMonitor(Display.getWindow(), 0L, 0, 0, GLFWVidMode.WIDTH, GLFWVidMode.HEIGHT, -1);
//                Display.getWindow().toggleFullscreen();
                if (GLFW.glfwGetWindowAttrib(Display.getWindow(), GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_FALSE) {
                    GLFW.glfwMaximizeWindow(Display.getWindow());
                } else {
                    GLFW.glfwRestoreWindow(Display.getWindow());
                }
            }
        } else {
            windowFullButtonAlpha = AnimationSystem.getAnimationState(windowFullButtonAlpha, 0.1f, 0.2f);
        }
        if (RenderSystem.isHovered(mouseX, mouseY, RenderSystem.getWidth() - 10 - 15, 4.5, 4.5, 4.5)) {//Hide button
            hideButtonAlpha = AnimationSystem.getAnimationState(hideButtonAlpha, 0.5f, 0.2f);
            if (Mouse.isButtonDown(0)) {
                prevMouse = true;
            }
            if (!Mouse.isButtonDown(0) && prevMouse) {
                prevMouse = false;
                GLFW.glfwIconifyWindow(Display.getWindow());
            }
        } else {
            hideButtonAlpha = AnimationSystem.getAnimationState(hideButtonAlpha, 0.1f, 0.2f);
        }
    }

    boolean doubleClicked = false, doubleClickCheck = false;
    Timer doubleClickTimer = new Timer();
    double moveX = 0, moveY = 0;

    private void moveWindow(double mouseX, double mouseY) {
        if (!Mouse.isButtonDown(0))
            doubleClicked = false;

        if (RenderSystem.isHovered(mouseX, mouseY, 0, 0, RenderSystem.getWidth(), 14) && Mouse.isButtonDown(0) && !Mouse.isGrabbed()) {

            if (!doubleClicked) {
                doubleClicked = true;
                if (doubleClickTimer.isDelayed(500)) {
                    doubleClickTimer.reset();
                    doubleClickCheck = true;
                } else {
                    if (doubleClickCheck) {
                        if (GLFW.glfwGetWindowAttrib(Display.getWindow(), GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_FALSE) {
                            GLFW.glfwMaximizeWindow(Display.getWindow());
                        } else {
                            GLFW.glfwRestoreWindow(Display.getWindow());
                        }
                        doubleClickCheck = false;
                    }
                }
            }

            if (moveX == 0 && moveY == 0) {
                moveX = mouseX;
                moveY = mouseY;
            } else {
                double posX = Display.getX() + mouseX - moveX;
                double posY = Display.getY() + mouseY - moveY;
                GLFW.glfwSetWindowPos(Display.getWindow(), (int) posX, (int) posY);
            }
//            this.previousMouse = true;
        } else if (moveX != 0 || moveY != 0) {
            moveX = 0;
            moveY = 0;
        }
    }

    private void initBuffers() {

        this.blurBuffer = RenderSystem.createFrameBuffer(this.blurBuffer);
        this.blurBuffer.framebufferClear();

    }

    public void doRectBlur(double x, double y, double width, double height) {
        doRectBlur(x, y, width, height, 255);
    }

    public void doRectBlur(double x, double y, double width, double height, float alpha) {
        doRectBlur(x, y, width, height, (int) (alpha * 255));
    }

    public void doRectBlur(double x, double y, double width, double height, int alpha) {
        this.blurBuffer.bindFramebuffer(true);
        Rect.draw(x, y, width, height, RenderSystem.hexColor(255, 255, 255, alpha), Rect.RectType.EXPAND);

        mc.getFramebuffer().bindFramebuffer(true);
    }

}
