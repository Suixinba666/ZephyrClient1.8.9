package tech.imxianyu.gui;

import lombok.RequiredArgsConstructor;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.color.ColorUtils;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.utils.timing.Timer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RetroBackground {

    List<RetroRect> rects = new ArrayList<>();
    double width, height, startY;
    double scanLineHeight = 240;
    double scanLineY = startY - scanLineHeight;
    double scanLineSmoothY = scanLineY;
    double scanLineSmoothY2 = scanLineY;
    double scanLineSmoothY3 = scanLineY;
    boolean isDirectionToBottom = true;
    Timer waitTimer = new Timer();
    long waitTime = 500;

    public RetroBackground() {

    }

    public void init(double posX, double posY, double width, double height) {
        this.width = width;
        this.height = height;
        this.startY = posY;
        scanLineY = posY;
        waitTimer.lastNs = System.currentTimeMillis() - waitTime;
        rects.clear();
        //(numbers of)
        int nRectsWidth = 26, nRectsHeight = 14;

        double spacing = 1;
        double rectWidth = (width - (nRectsWidth - 1) * spacing) / nRectsWidth;
        double rectHeight = (height - (nRectsHeight - 1) * spacing) / nRectsHeight;

        double offsetX = posX, offsetY = posY;
        for (int y = 0; y < nRectsHeight; y++) {
            offsetX = posX;
            for (int x = 0; x < nRectsWidth; x++) {
//                System.out.println("(" + x + ", " + y + ") " + offsetX + ", " + offsetY + " " + rectWidth + ", " + rectHeight);
                rects.add(new RetroRect(offsetX, offsetY, rectWidth, rectHeight));

                offsetX += spacing + rectWidth;
            }

            offsetY += spacing + rectHeight;
        }
    }

    public void draw(double posX, double posY, double mouseX, double mouseY) {
        Rect.draw(posX, posY, width, height, Color.BLACK.getRGB(), Rect.RectType.EXPAND);

        double upest = posY - scanLineHeight;
        double downest = posY + height + scanLineHeight;

        if (isDirectionToBottom && scanLineY == downest) {
            waitTimer.reset();
            isDirectionToBottom = !isDirectionToBottom;
        } else if (!isDirectionToBottom && scanLineY == upest) {
            waitTimer.reset();
            isDirectionToBottom = !isDirectionToBottom;
        }

        if (waitTimer.isDelayed(waitTime)) {
            /*scanLineSmoothY = AnimationSystem.interpolate(scanLineSmoothY, isDirectionToBottom ? downest : upest, 0.1f);
            scanLineSmoothY2 = AnimationSystem.interpolate(scanLineSmoothY2, scanLineSmoothY, 0.1f);
            scanLineSmoothY3 = AnimationSystem.interpolate(scanLineSmoothY3, scanLineSmoothY2, 0.1f);*/
            scanLineY = AnimationSystem.interpolate(scanLineY, isDirectionToBottom ? downest : upest, 0.07f);

            if (Math.abs((isDirectionToBottom ? downest : upest) - scanLineY) <= 5)
                scanLineY = isDirectionToBottom ? downest : upest;
        }

        RenderSystem.drawGradientRectBottomToTop(posX, scanLineY - scanLineHeight, width, scanLineY, RenderSystem.hexColor(2, 253, 3, 255), Color.BLACK.getRGB());
        RenderSystem.drawGradientRectTopToBottom(posX, scanLineY, width, scanLineY + scanLineHeight, RenderSystem.hexColor(2, 253, 3, 255), Color.BLACK.getRGB());


        this.rects.forEach(rect -> {
            rect.draw();

            if (RenderSystem.isHovered(mouseX, mouseY, rect.posX, rect.posY, rect.width, rect.height)) {
                rect.setMouseBlured();
            }

        });
    }


    @RequiredArgsConstructor
    class RetroRect {
        final double posX, posY, width, height;

        int alpha = 0;

        public void draw() {
            Rect.draw(posX, posY, width, height, ColorUtils.getColor(ColorUtils.ColorType.Base), Rect.RectType.EXPAND);

            if (alpha != 0) {
                Rect.draw(posX, posY, width, height, RenderSystem.hexColor(2, 253, 3, alpha), Rect.RectType.EXPAND);
                alpha = (int) AnimationSystem.interpolate(alpha, 0, 0.1f);
            }
        }

        public void setMouseBlured() {
            this.alpha = 255;
        }
    }
}
