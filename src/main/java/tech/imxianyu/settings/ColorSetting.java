package tech.imxianyu.settings;

import org.lwjglx.input.Mouse;
import tech.imxianyu.gui.clickgui.ZephyrClickGui;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.HSBColor;
import tech.imxianyu.rendering.entities.clickable.ClickEntity;
import tech.imxianyu.rendering.entities.clickable.MouseBounds;
import tech.imxianyu.rendering.entities.impl.GradientRect;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.widget.Widget;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ColorSetting extends Setting<HSBColor> {


    private final int chromaCount = 0;    public BooleanSetting rainbow = new BooleanSetting(this.getName() + "Rainbow", false, this.getShouldRender()) {
        @Override
        public void onToggle() {
            if (chroma.getValue()) {
                chroma.setValue(false);
            }
        }

        /*@Override
        public String getNameForRender() {
            return this.getInternalName().replaceFirst(ColorSetting.this.getInternalName() + " ", "");
        }*/
    };
    ClickEntity hue;    public BooleanSetting chroma = new BooleanSetting(this.getName() + "Chroma", false, this.getShouldRender()) {
        @Override
        public void onToggle() {
            if (rainbow.getValue()) {
                rainbow.setValue(false);
            }
        }

        /*@Override
        public String getNameForRender() {
            return this.getInternalName().replaceFirst(ColorSetting.this.getInternalName() + " ", "");
        }*/
    };
    ClickEntity alpha;    public NumberSetting<Integer> rainbowSpeed = new NumberSetting<Integer>(
            this.getName() + "RainbowSpeed",
            3, 1, 10, 1, () -> this.rainbow.getValue() && this.getShouldRender().get()) {
        /*@Override
        public String getNameForRender() {
            return this.getInternalName().replaceFirst(ColorSetting.this.getInternalName() + " ", "");
        }*/
    };
    ClickEntity colorrect;    public NumberSetting<Float> chromaSpeed = new NumberSetting<Float>(
            this.getName() + "ChromaSpeed",
            3f, 1f, 15f, 0.1f, () -> this.chroma.getValue() && this.getShouldRender().get()) {
        /*@Override
        public String getNameForRender() {
            return this.getInternalName().replaceFirst(ColorSetting.this.getInternalName() + " ", "");
        }*/
    };
    boolean hue_;    public NumberSetting<Long> chromaValue = new NumberSetting<Long>(
            this.getName() + "ChromaValue",
            150L, 1L, 200L, 1L, () -> this.chroma.getValue() && this.getShouldRender().get()) {
        /*@Override
        public String getNameForRender() {
            return this.getInternalName().replaceFirst(ColorSetting.this.getInternalName() + " ", "");
        }*/
    };
    boolean alpha_;
    boolean colorrect_;
    List<Color> huemap;
    List<Color> huemapForZeroDay;
    double zWidth = 50, zHeight = 50;
    int alp;
    private float mouseX, mouseY;
    private double positionX, positionY;

    public ColorSetting(String label, HSBColor value) {
        super(label, value);
    }

    public ColorSetting(String label, HSBColor value, Supplier<Boolean> show) {
        super(label, value, show);
    }

    @Override
    public void loadValue(String input) {
        String[] split = input.split(":");
        if (split.length < 4)
            return;
        this.value = new HSBColor(Float.parseFloat(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2]),
                Integer.parseInt(split[3]));

        if (split.length < 6)
            return;
        this.rainbow.loadValue(split[4]);
        this.chroma.loadValue(split[5]);
    }

    public int getRGB() {
        return this.getValue().getColor().getRGB();
    }

    //Render

    public int getRGB(int count) {
        return this.getValue(count).getColor().getRGB();
    }

    @Override
    public void onInit(Module module) {
        module.addSettings(this.rainbow, this.rainbowSpeed, this.chroma, this.chromaSpeed, this.chromaValue);

        //Render

        this.huemap = new ArrayList<>();
        this.huemapForZeroDay = new ArrayList<>();
        this.refreshHue();
        this.hue = new ClickEntity(0, 0, 0, 0, MouseBounds.CallType.Expand, () -> {
            hue_ = true;
        }, () -> {
        }, () -> {
        }, () -> {
        }, () -> {
        });
        this.colorrect = new ClickEntity(0, 0, 0, 0, MouseBounds.CallType.Expand, () -> {
            colorrect_ = true;
        }, () -> {
        }, () -> {
        }, () -> {
        }, () -> {
        });
        this.alpha = new ClickEntity(0, 0, 0, 0, MouseBounds.CallType.Expand, () -> {
            alpha_ = true;
        }, () -> {
        }, () -> {
        }, () -> {
        }, () -> {
        });
    }

    @Override
    public void onInit(Widget module) {
        module.addSettings(this.rainbow, this.rainbowSpeed, this.chroma, this.chromaSpeed, this.chromaValue);

        //Render

        this.huemap = new ArrayList<>();
        this.huemapForZeroDay = new ArrayList<>();
        this.refreshHue();
        this.hue = new ClickEntity(0, 0, 0, 0, MouseBounds.CallType.Expand, () -> {
            hue_ = true;
        }, () -> {
        }, () -> {
        }, () -> {
        }, () -> {
        });
        this.colorrect = new ClickEntity(0, 0, 0, 0, MouseBounds.CallType.Expand, () -> {
            colorrect_ = true;
        }, () -> {
        }, () -> {
        }, () -> {
        }, () -> {
        });
        this.alpha = new ClickEntity(0, 0, 0, 0, MouseBounds.CallType.Expand, () -> {
            alpha_ = true;
        }, () -> {
        }, () -> {
        }, () -> {
        }, () -> {
        });
    }

    @Override
    public void onInit() {
        ZephyrSettings.getSettings().addAll(Arrays.asList(this.rainbow, this.rainbowSpeed, this.chroma, this.chromaSpeed, this.chromaValue));

        //Render

        this.huemap = new ArrayList<>();
        this.huemapForZeroDay = new ArrayList<>();
        this.refreshHue();
        this.hue = new ClickEntity(0, 0, 0, 0, MouseBounds.CallType.Expand, () -> {
            hue_ = true;
        }, () -> {
        }, () -> {
        }, () -> {
        }, () -> {
        });
        this.colorrect = new ClickEntity(0, 0, 0, 0, MouseBounds.CallType.Expand, () -> {
            colorrect_ = true;
        }, () -> {
        }, () -> {
        }, () -> {
        }, () -> {
        });
        this.alpha = new ClickEntity(0, 0, 0, 0, MouseBounds.CallType.Expand, () -> {
            alpha_ = true;
        }, () -> {
        }, () -> {
        }, () -> {
        }, () -> {
        });
    }

    public HSBColor getValue(int count) {
        if (this.chroma.getValue()) {
            float speed = this.chromaSpeed.getValue().floatValue();
            long value = this.chromaValue.getValue();
            double v = (1 - speed / (this.chromaSpeed.getMaximum() + 0.1)) * 6000;
            float hue = (System.currentTimeMillis() + (count * value)) % (int) v;
            hue /= (int) v;
            super.getValue().setHue(hue);
        } else if (this.rainbow.getValue()) {
            float speed = this.rainbowSpeed.getValue().floatValue();
            double v = (1 - speed / (this.rainbowSpeed.getMaximum() + 0.1)) * 6000;
            float hue = System.currentTimeMillis() % (int) v;
            hue /= (int) v;
            super.getValue().setHue(hue);
        }

        return super.getValue();
    }

    @Override
    public HSBColor getValue() {
        if (this.rainbow.getValue()) {
            float speed = this.rainbowSpeed.getValue().floatValue();
            double v = (1 - speed / (this.rainbowSpeed.getMaximum() + 0.1)) * 6000;
            float hue = System.currentTimeMillis() % (int) v;
            hue /= (int) v;
            super.getValue().setHue(hue);
        }
        if (this.chroma.getValue()) {
            float speed = this.chromaSpeed.getValue();
            long value = this.chromaValue.getValue();
            double v = (1 - speed / (this.chromaSpeed.getMaximum() + 0.1)) * 6000;
            float hue = (System.currentTimeMillis() + value) % (int) v;
            hue /= (int) v;
            super.getValue().setHue(hue);
        }


        return super.getValue();
    }

    @Override
    public String getValueForConfig() {
        return this.getValue() + ":" + this.rainbow.getValue() + ":" + this.chroma.getValue();
    }

    public void draw(float mouseX, float mouseY, double positionX, double positionY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        this.positionX = positionX;
        this.positionY = positionY;

        if (!Mouse.isButtonDown(0)) {
            hue_ = false;
            alpha_ = false;
            colorrect_ = false;
        }
        float h = this.getValue().getHue();


        Rect.draw(positionX, positionY + 14, zWidth, zHeight, this.resetAlpha(Color.getHSBColor(h, 1, 1), this.getMenuAlpha()),
                Rect.RectType.EXPAND);
        new GradientRect(positionX, positionY + 14, zWidth, zHeight,
                this.resetAlpha(Color.getHSBColor(h, 0, 1), this.getMenuAlpha()), 0x00F, GradientRect.RenderType.Expand,
                GradientRect.GradientType.Horizontal).draw();
        new GradientRect(positionX, positionY + 14, zWidth, zHeight, 0x00F,
                this.resetAlpha(Color.getHSBColor(h, 1, 0), this.getMenuAlpha()), GradientRect.RenderType.Expand,
                GradientRect.GradientType.Vertical).draw();
        this.drawOutsideRect(positionX + this.getValue().getSaturation() * zWidth,
                positionY + 14 + (1.0f - this.getValue().getBrightness()) * zHeight, 1, 1, 0.5,
                new Color(32, 32, 32, this.getMenuAlpha()).getRGB());

        this.colorrect.setX(positionX - 2);
        this.colorrect.setY(positionY + 14 - 2);
        this.colorrect.setX1(zWidth + 4);
        this.colorrect.setY1(zHeight + 4);
        this.colorrect.tick(mouseX, mouseY);
//        Rect.draw( this.colorrect.getMouseX(),  this.colorrect.getMouseY(), 50, 50, 0xff0090ff, Rect.RectType.EXPAND).draw();


        //Rect.draw(colorrect.getX(), colorrect.getY(), colorrect.getX1(), colorrect.getY1(), 0xff0090ff, Rect.RectType.EXPAND).draw();

        for (int index = 0; index < zHeight; index++) {
            Rect.draw(positionX + zWidth + 5, positionY + 14 + index, 8, 1,
                    this.resetAlpha(this.huemapForZeroDay.get(index), this.getMenuAlpha()), Rect.RectType.EXPAND);
        }

        Rect.draw(positionX + zWidth + 5, positionY + 14 + this.getValue().getHue() * zHeight, 8, 1,
                new Color(32, 32, 32, Math.min(200, this.getMenuAlpha())).getRGB(), Rect.RectType.EXPAND);

        this.hue.setX(positionX + zWidth + 3);
        this.hue.setY(positionY + 14 - 2);
        this.hue.setX1(8 + 4);
        this.hue.setY1(zHeight + 4);
        this.hue.tick(mouseX, mouseY);

        for (int yExt = 0; yExt < zHeight / 2; yExt++)
            for (int xExt = 0; xExt < 4; xExt++)
                Rect.draw(positionX + zWidth + 16.5F + (xExt * 2), positionY + 14 + (yExt * 2), 2, 2,
                        this.resetAlpha((((yExt % 2 == 0) == (xExt % 2 == 0)) ? Color.WHITE : new Color(190, 190, 190)),
                                this.getMenuAlpha()),
                        Rect.RectType.EXPAND);

        new GradientRect(positionX + zWidth + 16.5F, positionY + 14, 8, zHeight, 0x00F,
                this.resetAlpha(this.getValue().getColor(), this.getMenuAlpha()),
                GradientRect.RenderType.Expand, GradientRect.GradientType.Vertical).draw();

        Rect.draw(positionX + zWidth + 16.5F, positionY + 14 + (this.getValue().getAlpha() * 0.003921568627451F) * zHeight, 8, 1,
                new Color(32, 32, 32, Math.min(200, this.getMenuAlpha())).getRGB(), Rect.RectType.EXPAND);

        this.alpha.setX(positionX + zWidth + 18 - 2);
        this.alpha.setY(positionY + 14 - 2);
        this.alpha.setX1(8 + 4);
        this.alpha.setY1(zHeight + 4);
        this.alpha.tick(mouseX, mouseY);

        for (int yExt = 0; yExt < zHeight / 2; yExt++)
            for (int xExt = 0; xExt < 2; xExt++)
                Rect.draw(positionX + zWidth + 28 + (xExt * 2), positionY + 14 + (yExt * 2), 2, 2,
                        this.resetAlpha((((yExt % 2 == 0) == (xExt % 2 == 0)) ? Color.WHITE : new Color(190, 190, 190)),
                                this.getMenuAlpha()),
                        Rect.RectType.EXPAND);

        Rect.draw(positionX + zWidth + 28, positionY + 14, 4, zHeight,
                this.resetAlpha(this.getValue().getColor(),
                        Math.min(this.getMenuAlpha(), this.getValue().getColor().getAlpha())),
                Rect.RectType.EXPAND);

        if (alpha_) {
            ZephyrClickGui.getInstance().lmbPressed = true;
            float pos = (float) (this.mouseY - (this.positionY + 14));
            if (pos < 0) {
                pos = 0;
            }
            if (pos > zHeight) {
                pos = (float) zHeight;
            }
            this.getValue().setAlpha((int) ((pos / zHeight) * 255.0f));
        }

        if (colorrect_) {
            ZephyrClickGui.getInstance().lmbPressed = true;
            float posX = (float) (this.mouseX - (this.positionX));
            float posY = (float) (this.mouseY - (this.positionY + 14));
            if (posX < 0) {
                posX = 0;
            }
            if (posX > zWidth) {
                posX = (float) zWidth;
            }
            if (posY < 0) {
                posY = 0;
            }
            if (posY > zHeight) {
                posY = (float) zHeight;
            }
            this.getValue().setSaturation((float) (posX / zWidth));
            this.getValue().setBrightness((float) ((zHeight - posY) / zHeight));
        }
        if (hue_) {
            ZephyrClickGui.getInstance().lmbPressed = true;
            float pos = (float) (this.mouseY - (this.positionY + 14));
            if (pos < 0) {
                pos = 0;
            }
            if (pos > zHeight) {
                pos = (float) zHeight;
            }
            this.getValue().setHue((float) (pos / zHeight));
        }
    }

    public void setRenderAlpha(int alpha) {
        alp = alpha;
    }

    private int getMenuAlpha() {
        return alp;
    }

    private void refreshHue() {
        this.huemap.clear();
        for (int index = 0; index < 100; index++) {
            this.huemap.add(Color.getHSBColor(index / 100.0f, 1.0f, 1.0f));
        }

        this.huemapForZeroDay.clear();
        for (int index = 0; index < zHeight; index++) {
            this.huemapForZeroDay.add(Color.getHSBColor((float) (index / (zHeight)), 1.0f, 1.0f));
        }
    }

    private int resetAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
    }

    private void drawOutsideRect(double x, double y, double x2, double y2, double zWidth, int color) {
        this.drawOutsideRect2(x, y, x + x2, y + y2, zWidth, color);
    }

    private void drawOutsideRect2(double x, double y, double x2, double y2, double zWidth, int color) {
        if (x > x2) {
            double i = x;
            x = x2;
            x2 = i;
        }

        if (y > y2) {
            double j = y;
            y = y2;
            y2 = j;
        }

        Rect.draw(x, y - zWidth, x - zWidth, y2, color, Rect.RectType.ABSOLUTE_POSITION);
        Rect.draw(x, y, x2 + zWidth, y - zWidth, color, Rect.RectType.ABSOLUTE_POSITION);
        Rect.draw(x2, y, x2 + zWidth, y2 + zWidth, color, Rect.RectType.ABSOLUTE_POSITION);
        Rect.draw(x - zWidth, y2, x2, y2 + zWidth, color, Rect.RectType.ABSOLUTE_POSITION);
    }










}
