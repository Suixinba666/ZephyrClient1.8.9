package tech.imxianyu.gui.multiplayerdark;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;
import org.lwjglx.input.Mouse;
import tech.imxianyu.rendering.animation.animations.MultiEndpointAnimation;
import tech.imxianyu.rendering.entities.RenderableEntity;
import tech.imxianyu.rendering.entities.impl.Image;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.utils.timing.Timer;

public class TexturedButtonDark extends RenderableEntity {

    @Getter
    private final ResourceLocation img;

    private final MultiEndpointAnimation animation;
    private final Timer timer = new Timer();
    private final long delay;
    private final Runnable action;

    boolean previousMouse = false;
    @Getter
    private final String name;
    public TexturedButtonDark(ResourceLocation img, double x, double y, double width, double height, long delay, Runnable action) {
        super(x, y, width, height);

        this.img = img;
        this.delay = delay;
        this.action = action;
        this.name = img.getResourcePath().substring(img.getResourcePath().lastIndexOf("/") + 1, img.getResourcePath().lastIndexOf("."));
        this.animation = new MultiEndpointAnimation().withStartValue(RenderSystem.getHeight() + 10 + height)
                .withEndPoints(RenderSystem.getHeight() + 10 + height, RenderSystem.getHeight() - height - 10, RenderSystem.getHeight() - height);
    }

    boolean previousState = false;

    public void draw(double mouseX, double mouseY, ZephyrMultiPlayerUIDark inst) {

        boolean hovered = RenderSystem.isHovered(mouseX, mouseY, 0, RenderSystem.getHeight() - 60, RenderSystem.getWidth(), 60) && inst.dialog == null;

        if (hovered && !previousState) {
            previousState = true;
            this.timer.reset();
        }

        if (!hovered && previousState) {
            previousState = false;
            this.timer.reset();
        }

        double posY = this.animation.value;

        if (inst.deleteMode && (this.getName().equals("add") || this.getName().equals("refresh"))) {
            this.animation.interpolate(true, 0.15f);
        } else {

            if (this.timer.isDelayed(delay)) {

                if (inst.deleteMode && (this.getName().equals("remove") || this.getName().equals("back"))) {
                    this.animation.interpolate(false, 0.15f);

                } else {
                    if (this.getName().equals("back")) {
                        this.animation.interpolate(true, 0.15f);
                    } else {
                        this.animation.interpolate(!hovered, 0.15f);
                    }
                }

            }

        }

        Image.draw(img, this.getX(), posY, getWidth(), getHeight(), Image.Type.Normal);

        boolean isHoveredImg = RenderSystem.isHovered(mouseX, mouseY, this.getX(), posY, getWidth(), getHeight());

        if (isHoveredImg && Mouse.isButtonDown(0) && !previousMouse && inst.dialog == null) {
            previousMouse = true;
            this.action.run();
        }

        if (!Mouse.isButtonDown(0) && previousMouse)
            previousMouse = false;
    }
}
