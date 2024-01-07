package tech.imxianyu.management;

import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjglx.input.Mouse;
import tech.imxianyu.eventapi.EventBus;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.rendering.DisplayResizedEvent;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.interfaces.AbstractManager;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.settings.Setting;
import tech.imxianyu.utils.timing.Timer;
import tech.imxianyu.widget.Widget;
import tech.imxianyu.widget.direction.HorizontalDirection;
import tech.imxianyu.widget.direction.VerticalDirection;
import tech.imxianyu.widget.impl.*;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author ImXianyu
 * @since 6/17/2023 9:55 AM
 */
public class WidgetsManager extends AbstractManager {

    public static final KeyStrokes keyStrokes = new KeyStrokes();
    public static final tech.imxianyu.widget.impl.ArrayList arrayList = new tech.imxianyu.widget.impl.ArrayList();
    public static final Music music = new Music();
    public static final MusicLyrics musicLyrics = new MusicLyrics();
    public static final MusicSpectrum musicSpectrum = new MusicSpectrum();
    public static final PaperDoll paperDoll = new PaperDoll();
    public static final Armor armor = new Armor();
    public static final SpeedGraph speedGraph = new SpeedGraph();
    public static final Potion potion = new Potion();
    public static final TargetHud targetHud = new TargetHud();
    public static final Inventory inventory = new Inventory();
    public static final ScoreBoard scoreBoard = new ScoreBoard();
    public static final ChatFilter chatFilter = new ChatFilter();

    @Getter
    private static final List<Widget> widgets = new ArrayList<>();

    private static Widget positioningWidget = null, draggingWidget = null;
    private Timer frameLimitTimer = new Timer();
    @Handler
    public void onRender2D(Render2DEvent event) {
//        if (!frameLimitTimer.isDelayed(1000 / 80, true)) return;
        boolean editing = (mc.currentScreen instanceof GuiChat);

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        double mouseX = Mouse.getX() * scaledResolution.getScaledWidth() / (mc.displayWidth * 1.0);
        double mouseY = scaledResolution.getScaledHeight() - Mouse.getY() * scaledResolution.getScaledHeight() / (mc.displayHeight * 1.0) - 1;
        mouseX = mouseX * RenderSystem.getScaleFactor();
        mouseY = mouseY * RenderSystem.getScaleFactor();

        GlStateManager.pushMatrix();

        for (Widget widget : WidgetsManager.getWidgets()) {

            if (!widget.isEnabled())
                continue;

            if (!editing && widget.isMovable()) {
                this.doAdsorption(widget);
            }

            widget.onRender(event, editing);

            if (mc.currentScreen instanceof GuiChat) {

                if (widget.isResizable()) {
                    this.resizeWidget(widget, mouseX, mouseY);
                }

                if (widget.isMovable()) {
                    this.renderWidgetInfo(widget);
                    this.doCollisions(widget, mouseX, mouseY);
                }

            }
        }

        GlStateManager.popMatrix();

    };
    
    private void resizeWidget(Widget widget, double mouseX, double mouseY) {
        double posX = widget.getX(), posY = widget.getY(), width = widget.getWidth(), height = widget.getHeight();

        double resizeRange = 8;

        if (RenderSystem.isHovered(mouseX, mouseY, posX + width - resizeRange, posY + height - resizeRange, resizeRange * 1.5, resizeRange * 1.5)) {
            if (Mouse.isButtonDown(0) && draggingWidget == null) {
                draggingWidget = widget;
            }
        }

        if (!Mouse.isButtonDown(0) && draggingWidget == widget)
            draggingWidget = null;

        if (draggingWidget == widget) {
            if (widget.resizeX == 0 && widget.resizeY == 0) {
                widget.resizeX = mouseX - width;
                widget.resizeY = mouseY - height;
            } else {

                double lastWidth = widget.getWidth(), lastHeight = widget.getHeight();

                if (mouseX - widget.resizeX >= widget.defaultWidth) {
                    widget.setWidth(mouseX - widget.resizeX);
                }

                if (mouseY - widget.resizeY >= widget.defaultHeight) {
                    widget.setHeight(mouseY - widget.resizeY);
                }

                widget.onResized(lastWidth, lastHeight);
            }
        } else if (widget.resizeX != 0 || widget.resizeY != 0) {
            widget.resizeX = 0;
            widget.resizeY = 0;
        }
        
    }

    private void renderWidgetInfo(Widget widget) {
        RenderSystem.drawOutLine(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), 1, Color.BLACK.getRGB());
        Rect.draw(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), RenderSystem.hexColor(64, 64, 64, 100), Rect.RectType.EXPAND);

        String stringToRender = widget.getName();

        if (widget.isResizable())
            stringToRender += " [Resizable]";

        if (widget.horizontalDirection != HorizontalDirection.None)
            stringToRender += " H:" + widget.horizontalDirection.name();

        if (widget.verticalDirection != VerticalDirection.None)
            stringToRender += " V:" + widget.verticalDirection.name();

//        stringToRender += " W: " + widget.getWidth() + " H: " + widget.getHeight();

        RenderSystem.drawRect(widget.getX() + (widget.getWidth() < 0 ? widget.getWidth() : 0), widget.getY() + widget.getHeight(), widget.getX() + 4 + FontManager.pf18.getStringWidth(stringToRender) + (widget.getWidth() < 0 ? widget.getWidth() : 0), widget.getY() + widget.getHeight() + FontManager.pf18.getHeight() + 4, Color.BLACK.getRGB());
        FontManager.pf18.drawString(stringToRender, widget.getX() + 2 + (widget.getWidth() < 0 ? widget.getWidth() : 0), widget.getY() + widget.getHeight() + 2, -1);
    }

    @Handler
    public void onResize(DisplayResizedEvent event) {

        if (true)
            return;

        double horizontal = (double) event.getNowWidth() / (double) event.getBeforeWidth();
        double vertical = (double) event.getNowHeight() / (double) event.getBeforeHeight();


        for (Widget widget : widgets) {

            if (widget.horizontalDirection == HorizontalDirection.None) {
                widget.setX(widget.getX() * horizontal);
            }

            if (widget.verticalDirection == VerticalDirection.None) {
                widget.setY(widget.getY() * vertical);
            }

        }
    }

    private void doAdsorption(Widget widget) {

        if (widget.horizontalDirection == HorizontalDirection.Left) {
            widget.setX(0);
        } else if (widget.horizontalDirection == HorizontalDirection.Center) {
            widget.setX(RenderSystem.getWidth() * 0.5 - widget.getWidth() * 0.5);
        } else if (widget.horizontalDirection == HorizontalDirection.Right) {
            widget.setX(RenderSystem.getWidth() - widget.getWidth());
        }

        if (widget.verticalDirection == VerticalDirection.Top) {
            widget.setY(0);
        } else if (widget.verticalDirection == VerticalDirection.Center) {
            widget.setY(RenderSystem.getHeight() * 0.5 - widget.getHeight() * 0.5);
        } else if (widget.verticalDirection == VerticalDirection.Bottom) {
            widget.setY(RenderSystem.getHeight() - widget.getHeight());
        }

    }

    private void doCollisions(Widget widget, double mouseX, double mouseY) {

        this.doAdsorption(widget);

//        Rect.draw(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), 0xff0090ff, Rect.RectType.EXPAND);

        if (RenderSystem.isHovered(mouseX, mouseY, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight()) && Mouse.isButtonDown(0) && (positioningWidget == null || positioningWidget == widget) && draggingWidget == null) {
            positioningWidget = widget;
            if (widget.getMoveX() == 0 && widget.getMoveY() == 0) {
                widget.setMoveX(mouseX - widget.getX());
                widget.setMoveY(mouseY - widget.getY());
            } else {
                double x = mouseX - widget.getMoveX();
                double y = mouseY - widget.getMoveY();

                double widgetX = widget.getX();
                double widgetY = widget.getY();
                double widgetCenterX = widget.getX() + widget.getWidth() * 0.5;
                double widgetCenterY = widget.getY() + widget.getHeight() * 0.5;

                double range = 4;

                if (widgetX <= range) {
                    widget.horizontalDirection = HorizontalDirection.Left;
                } else if (this.distanceTo(widgetCenterX, RenderSystem.getWidth() * 0.5) <= range) {
                    widget.horizontalDirection = HorizontalDirection.Center;
                } else if (RenderSystem.getWidth() - (widgetX + widget.getWidth()) <= range) {
                    widget.horizontalDirection = HorizontalDirection.Right;
                } else {
                    widget.horizontalDirection = HorizontalDirection.None;
                }

                if (widgetY <= range) {
                    widget.verticalDirection = VerticalDirection.Top;
                } else if (this.distanceTo(widgetCenterY, RenderSystem.getHeight() * 0.5) <= range) {
                    widget.verticalDirection = VerticalDirection.Center;
                } else if (RenderSystem.getHeight() - (widgetY + widget.getHeight()) <= range) {
                    widget.verticalDirection = VerticalDirection.Bottom;
                } else {
                    widget.verticalDirection = VerticalDirection.None;
                }

                double threshold = 10;

                if (this.distanceTo(widget.getX(), x) > threshold || widget.horizontalDirection == HorizontalDirection.None) {
                    widget.setX(x);
                    widget.horizontalDirection = HorizontalDirection.None;
                }

                if (this.distanceTo(widget.getY(), y) > threshold || widget.verticalDirection == VerticalDirection.None) {
                    widget.setY(y);
                    widget.verticalDirection = VerticalDirection.None;
                }

            }
        } else if ((widget.getMoveX() != 0 || widget.getMoveY() != 0)) {
            if (positioningWidget == widget)
                positioningWidget = null;
            widget.setMoveX(0);
            widget.setMoveY(0);
        }
    }

    private double distanceTo(double a, double b) {
        return Math.abs(a - b);
    }


    @Override
    @SneakyThrows
    public void onStart() {
        widgets.forEach(EventBus::unregister);

        widgets.clear();

        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (Widget.class.isAssignableFrom(field.getType())) {
                Widget widget = (Widget) field.get(null);

                if (widget == null)
                    continue;

                for (Field f : widget.getClass().getDeclaredFields()) {
                    f.setAccessible(true);

                    if (Setting.class.isAssignableFrom(f.getType())) {
                        widget.addSettings((Setting<?>) f.get(widget));
                    }
                }

                widgets.add(widget);
            }
        }

        widgets.sort(Comparator.comparing(Widget::getName));
    }

    @Override
    public void onStop() {

    }

    public Widget getWidgetByName(String name) {

        for (Widget widget : widgets) {
            if (widget.getName().equalsIgnoreCase(name))
                return widget;
        }

        return null;
    }

}
