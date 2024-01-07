package tech.imxianyu.widget.impl;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.management.WidgetsManager;
import tech.imxianyu.music.CloudMusic;
import tech.imxianyu.rendering.Bloom;
import tech.imxianyu.rendering.Blur;
import tech.imxianyu.rendering.Stencil;
import tech.imxianyu.rendering.TexturedShadow;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.rendering.shader.StencilShader;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.settings.ZephyrSettings;
import tech.imxianyu.utils.lyric.LyricBean;
import tech.imxianyu.utils.lyric.LyricParser;
import tech.imxianyu.utils.other.StringUtils;
import tech.imxianyu.widget.Widget;

import java.awt.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import static tech.imxianyu.rendering.entities.impl.GradientRect.glColor;

/**
 * @author ImXianyu
 * @since 6/17/2023 10:04 AM
 */
public class MusicLyrics extends Widget {
    public static final List<LyricBean> lyrics = new ArrayList<>();
    public static final List<LyricBean> allLyrics = new ArrayList<>();
    static double scrollOffset = 0;
    public static LyricBean currentDisplaying = null;

    public NumberSetting<Integer> width = new NumberSetting<>("Boarder Width", 450, 100, 1000, 1);
    public NumberSetting<Integer> height = new NumberSetting<>("Boarder Height", 80, 25, 800, 1);

    public static boolean hasTransLyrics = false, hasRomalrc = false;
    public static List<ScrollTiming> timings = new ArrayList<>();

    public ModeSetting<ScrollMode> scrollMode = new ModeSetting<>("Scroll Mode", ScrollMode.Style1);
    public enum ScrollMode {
        Style1,
        Style2;
    }

    public BooleanSetting rectBlur = new BooleanSetting("Lyrics Blur Rect", false, () -> !ZephyrSettings.reduceShaders.getValue());
    public BooleanSetting rectShadow = new BooleanSetting("Lyrics Rect Shadow", false, () -> rectBlur.getValue() && !ZephyrSettings.reduceShaders.getValue());
    public BooleanSetting lyricsShadow = new BooleanSetting("Lyrics Shadow", false, () -> !rectBlur.getValue() && !ZephyrSettings.reduceShaders.getValue());
    public BooleanSetting showRoman = new BooleanSetting("Show Romanization in Japanese songs", false);

    public static class ScrollTiming {
        public long start, duration;
        public String text;
        public String totalLyric;

        /**
         * 分词Timing
         */
        public List<WordTiming> timings = new ArrayList<>();
        public List<Long> timingsDuration = new ArrayList<>();
    }

    public static class WordTiming {
        public String word;
        public long timing;
    }

    public MusicLyrics() {
        super("Music Lyrics");
    }

    public static void initLyric(JsonObject lyric) {
        hasTransLyrics = false;
        hasRomalrc = false;
        timings.clear();


        synchronized (lyrics) {
            lyrics.clear();
            lyrics.addAll(LyricParser.parse(lyric));
        }

        synchronized (allLyrics) {
            allLyrics.clear();
            allLyrics.addAll(lyrics);
        }
        scrollOffset = 0;

        if (!timings.isEmpty()) {

            List<LyricBean> beans = new ArrayList<>();
            for (ScrollTiming timing : timings) {
                StringBuilder sb = new StringBuilder();
                for (WordTiming wordTiming : timing.timings) {
                    sb.append(wordTiming.word);
                }
                LyricBean lyricBean = new LyricBean(timing.start, "NONE", sb.toString());
                beans.add(lyricBean);
            }

            if (hasTransLyrics) {
                Map<String, String> transMap = new HashMap<>();

                for (LyricBean allLyric : allLyrics) {
                    transMap.put(allLyric.getLyric(), allLyric.getCLyric());
                }

                for (LyricBean bean : beans) {
                    for (Map.Entry<String, String> ent : transMap.entrySet()) {
                        if (StringUtils.getSimilarityRatio(bean.getLyric(), ent.getKey()) >= 55) {
                            bean.cLyric = ent.getValue();
                            break;
                        }
                    }
                }
            }

            if (hasRomalrc) {
                Map<String, String> transMap = new HashMap<>();

                for (LyricBean allLyric : allLyrics) {
                    transMap.put(allLyric.getLyric(), allLyric.getRLyric());
                }

                for (LyricBean bean : beans) {
                    for (Map.Entry<String, String> ent : transMap.entrySet()) {
                        if (StringUtils.getSimilarityRatio(bean.getLyric(), ent.getKey()) >= 55) {
                            bean.rLyric = ent.getValue();
                            break;
                        }
                    }
                }
            }

            allLyrics.clear();
            lyrics.clear();
            allLyrics.addAll(beans);
            lyrics.addAll(beans);
        }

//        for (LyricBean allLyric : allLyrics) {
//            System.out.println(allLyric.getLyric());
//        }

    }

    public static void quickResetProgress(long progress) {

        if (allLyrics.isEmpty())
            return;


        try {

            for (LyricBean l : allLyrics) {
                l.scrollWidth = 0;
            }

            lyrics.clear();
            lyrics.addAll(allLyrics);
            scrollOffset = 0;

            currentDisplaying = allLyrics.get(0);
            LyricBean lyric = allLyrics.get(0);
            while (lyric.getTimeStamp() <= progress) {
                currentDisplaying = lyric;
                lyrics.remove(0);
                lyric = lyrics.get(0);
            }

            scrollOffset = (allLyrics.indexOf(currentDisplaying)) * (FontManager.pf25bold.getHeight() + 12) - FontManager.pf25bold.getHeight() / 2.0;

        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

    }

    ZFontRenderer fontRenderer = FontManager.pf25bold;
    ZFontRenderer smallFontRenderer = FontManager.pf16;

    Framebuffer backgroundBuffer;
    Framebuffer blendBuffer;
    Framebuffer blendBuffer2;
    Framebuffer coverBuffer;

    public static void drawQuads2(double x, double y, double width, double height, int color) {
        GlStateManager.disableTexture2D();

        glColor(color);
        GL11.glBegin(7);
        GL11.glVertex2d(x, y + height);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x, y);
        GL11.glEnd();

        GlStateManager.enableTexture2D();
    }

    public static void drawHorizontalGradientRect(double x, double y, double width, double height, int yColor, int y1Color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//        GlStateManager.blendFunc(770, 771);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(7);
        glColor(yColor);
        GL11.glVertex2d(x, y + height);
        glColor(y1Color);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x + width, y);
        glColor(yColor);
        GL11.glVertex2d(x, y);
        GL11.glEnd();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    @Override
    public void onRender(Render2DEvent event, boolean editing) {
        if (CloudMusic.player == null) {
            return;
        }

        int songProgress = CloudMusic.player.getCurrentTimeMillis();

        synchronized (lyrics) {
            if (lyrics.size() > 0 && !CloudMusic.player.isPausing()) {
                LyricBean lyric = lyrics.get(0);
                if (lyric.getTimeStamp() <= songProgress) {
                    currentDisplaying = lyric;
                    lyrics.remove(0);
                }
            }
        }

        int indexOf = allLyrics.indexOf(currentDisplaying);

        if (currentDisplaying == null)
            scrollOffset = 0;
        else
            scrollOffset = AnimationSystem.interpolate(scrollOffset * 10000, (indexOf * this.getLyricHeight()) * 10000, 0.2f) / 10000.0;
        double offsetY = this.getY() + this.getHeight() / 2.0 - fontRenderer.getHeight() / 2.0 - scrollOffset;
        GlStateManager.pushMatrix();

        boolean reduceShaders = ZephyrSettings.reduceShaders.getValue();

        if (!reduceShaders) {
            blendBuffer = RenderSystem.createFrameBuffer(blendBuffer);
            blendBuffer2 = RenderSystem.createFrameBuffer(blendBuffer2);
            backgroundBuffer = RenderSystem.createFrameBuffer(backgroundBuffer);
            if (coverBuffer == null) {
                coverBuffer = new Framebuffer(200, (fontRenderer.getHeight() + 2), false);
            }

            mc.getFramebuffer().bindFramebuffer(true);
        }

        RenderSystem.doScissor((int) this.getX(), (int) this.getY(), (int) this.getWidth(), (int) this.getHeight());

        for (int i = 0; i < allLyrics.size(); i++) {
            LyricBean lyric = allLyrics.get(i);

            if (offsetY + fontRenderer.getHeight() < this.getY()) {
                offsetY += this.getLyricHeight();
                continue;
            }

            if (offsetY > this.getY() + this.getHeight())
                break;

            String secondaryLyric = "";

            if (hasSecondaryLyrics())
                secondaryLyric = this.getSecondaryLyrics(lyric);

            if (!reduceShaders) {

                if (rectBlur.getValue()) {
                    Blur.blurBuffer.bindFramebuffer(true);

                    drawQuads2(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2f - 2, offsetY - 1, fontRenderer.getStringWidth(lyric.getLyric()) + 5, fontRenderer.getHeight() + 2, Color.WHITE.getRGB());

                    if (!secondaryLyric.isEmpty()) {
                        drawQuads2(this.getX() + this.getWidth() / 2.0 - smallFontRenderer.getStringWidth(secondaryLyric) / 2f - 2, offsetY + fontRenderer.getHeight(), smallFontRenderer.getStringWidth(secondaryLyric) + 5, smallFontRenderer.getHeight() + 4, Color.WHITE.getRGB());
                    }

                    Blur.blurBuffer.unbindFramebuffer();

                    if (rectShadow.getValue()) {
                        Bloom.bloomBuffer.bindFramebuffer(true);
                        drawQuads2(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2f - 2, offsetY - 1, fontRenderer.getStringWidth(lyric.getLyric()) + 5, fontRenderer.getHeight() + 2, Color.WHITE.getRGB());
                        if (!secondaryLyric.isEmpty()) {
                            drawQuads2(this.getX() + this.getWidth() / 2.0 - smallFontRenderer.getStringWidth(secondaryLyric) / 2f - 2, offsetY + fontRenderer.getHeight(), smallFontRenderer.getStringWidth(secondaryLyric) + 5, smallFontRenderer.getHeight() + 4, Color.WHITE.getRGB());
                        }
                        Bloom.bloomBuffer.unbindFramebuffer();
                    }
                } else if (lyricsShadow.getValue()) {
                    Bloom.bloomBuffer.bindFramebuffer(true);
                    boolean hasScrollTimings = !timings.isEmpty();

                    fontRenderer.drawCenteredString(lyric.getLyric(), this.getX() + this.getWidth() / 2.0, offsetY, new Color(255, 255, 255, ((i < indexOf && hasScrollTimings) || (i <= indexOf && !hasScrollTimings) ? lyric.alpha : 70)).getRGB());
                    if (!secondaryLyric.isEmpty()) {
                        smallFontRenderer.drawCenteredString(secondaryLyric, this.getX() + this.getWidth() / 2.0, offsetY + fontRenderer.getHeight() + smallFontRenderer.getHeight() * 0.5 - 1, new Color(255, 255, 255, i <= indexOf ? lyric.alpha : 100).getRGB());
                    }

                    Bloom.bloomBuffer.unbindFramebuffer();
                }

            } else if (rectShadow.getValue()) {

                float shadowAlpha = 0.9f;
                double shadowRadius = 4;

                //Top
                TexturedShadow.drawTopShadow(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2f - 2, offsetY - 1, fontRenderer.getStringWidth(lyric.getLyric()) + 5, shadowAlpha, shadowRadius);
                //Top Left Corner
                TexturedShadow.drawTopLeftShadow(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2f - 2, offsetY - 1, shadowAlpha, shadowRadius);
                //Top Right Corner
                TexturedShadow.drawTopRightShadow(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2f - 2 + fontRenderer.getStringWidth(lyric.getLyric()) + 5, offsetY - 1, shadowAlpha, shadowRadius);
                //Left
                TexturedShadow.drawLeftShadow(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2f - 2, offsetY - 1, fontRenderer.getHeight() + 2, shadowAlpha, shadowRadius);
                //Left Bottom Corner
                TexturedShadow.drawBottomLeftShadow(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2f - 2, offsetY - 1 + (fontRenderer.getHeight() + 2), shadowAlpha, shadowRadius);
                //Right
                TexturedShadow.drawRightShadow(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2f - 2 + fontRenderer.getStringWidth(lyric.getLyric()) + 5, offsetY - 1, fontRenderer.getHeight() + 2, shadowAlpha, shadowRadius);
                //Right Bottom Corner
                TexturedShadow.drawBottomRightShadow(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2f - 2 + fontRenderer.getStringWidth(lyric.getLyric()) + 5, offsetY - 1 + fontRenderer.getHeight() + 2, shadowAlpha, shadowRadius);

                if (!secondaryLyric.isEmpty()) {

                    double bottomWidth = (fontRenderer.getStringWidth(lyric.getLyric()) + 5 - (smallFontRenderer.getStringWidth(secondaryLyric) + 5)) * 0.5;

                    //Bottom First Half
                    TexturedShadow.drawBottomShadow(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2f - 2, offsetY - 1 + fontRenderer.getHeight() + 2, bottomWidth, shadowAlpha, shadowRadius);
                    //Bottom Second Half
                    TexturedShadow.drawBottomShadow(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2f - 2 + bottomWidth + smallFontRenderer.getStringWidth(secondaryLyric) + 5, offsetY - 1 + fontRenderer.getHeight() + 2, bottomWidth, shadowAlpha, shadowRadius);

                    //CLyric Left
                    TexturedShadow.drawLeftShadow(this.getX() + this.getWidth() / 2.0 - smallFontRenderer.getStringWidth(secondaryLyric) / 2f - 2, offsetY - 1 + fontRenderer.getHeight() + 2, smallFontRenderer.getHeight() + 3.5, shadowAlpha, shadowRadius);

                    //CLyric Left Corner
                    TexturedShadow.drawBottomLeftShadow(this.getX() + this.getWidth() / 2.0 - smallFontRenderer.getStringWidth(secondaryLyric) / 2f - 2, offsetY - 1 + fontRenderer.getHeight() + 2 + smallFontRenderer.getHeight() + 3, shadowAlpha, shadowRadius);

                    //CLyric Bottom
                    TexturedShadow.drawBottomShadow(this.getX() + this.getWidth() / 2.0 - smallFontRenderer.getStringWidth(secondaryLyric) / 2f - 2, offsetY - 1 + fontRenderer.getHeight() + 2 + smallFontRenderer.getHeight() + 3, smallFontRenderer.getStringWidth(secondaryLyric) + 5, shadowAlpha, shadowRadius);

                    //CLyric Right
                    TexturedShadow.drawRightShadow(this.getX() + this.getWidth() / 2.0 - smallFontRenderer.getStringWidth(secondaryLyric) / 2f - 2 + smallFontRenderer.getStringWidth(secondaryLyric) + 5, offsetY - 1 + fontRenderer.getHeight() + 2, smallFontRenderer.getHeight() + 3.5, shadowAlpha, shadowRadius);

                    //CLyric Right Corner
                    TexturedShadow.drawBottomRightShadow(this.getX() + this.getWidth() / 2.0 - smallFontRenderer.getStringWidth(secondaryLyric) / 2f - 2 + smallFontRenderer.getStringWidth(secondaryLyric) + 5, offsetY - 1 + fontRenderer.getHeight() + 2 + smallFontRenderer.getHeight() + 3.5, shadowAlpha, shadowRadius);
                } else {
                    //Bottom
                    TexturedShadow.drawBottomShadow(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2f - 2, offsetY - 1 + fontRenderer.getHeight() + 2, fontRenderer.getStringWidth(lyric.getLyric()) + 5, shadowAlpha, shadowRadius);
                }
            }

            offsetY += this.getLyricHeight();
        }

        mc.getFramebuffer().bindFramebuffer(true);


        offsetY = this.getY() + this.getHeight() / 2.0 - fontRenderer.getHeight() / 2.0 - scrollOffset;

        synchronized (allLyrics) {
            for (int i = 0; i < allLyrics.size(); i++) {
                LyricBean lyric = allLyrics.get(i);

                if (offsetY + fontRenderer.getHeight() < this.getY()) {
                    offsetY += this.getLyricHeight();
                    continue;
                }

                if (offsetY > this.getY() + this.getHeight())
                    break;

                boolean hasScrollTimings = !timings.isEmpty();

                lyric.alpha = (int) AnimationSystem.interpolate(lyric.alpha, lyric == currentDisplaying ? 255 : 70, 0.15f);
                lyric.scale = AnimationSystem.interpolate(lyric.scale, lyric == currentDisplaying ? 1.0 : 0.8, 0.2f);
                String secondaryLyric = "";

                if (hasSecondaryLyrics())
                    secondaryLyric = this.getSecondaryLyrics(lyric);
                
                fontRenderer.drawCenteredString(lyric.getLyric(), this.getX() + this.getWidth() / 2.0, offsetY, new Color(255, 255, 255, ((i < indexOf && hasScrollTimings) || (i <= indexOf && !hasScrollTimings) ? lyric.alpha : 70)).getRGB());
                if (!secondaryLyric.isEmpty()) {
                    smallFontRenderer.drawCenteredString(secondaryLyric, this.getX() + this.getWidth() / 2.0, offsetY + fontRenderer.getHeight() + smallFontRenderer.getHeight() * 0.5 - 1, new Color(255, 255, 255, i <= indexOf ? lyric.alpha : 100).getRGB());
                }

                // lyric scroll timing ("yrc") scheme
                // bro this thing is totally fucked up
                if (hasScrollTimings && lyric == currentDisplaying) {
                    for (int j = 0; j < timings.size(); j++) {
                        ScrollTiming timing = timings.get(j);

                        if (j + 1 < timings.size() && songProgress < timings.get(j + 1).start) {

                            if (this.scrollMode.getValue() == ScrollMode.Style1) {
                                int cur = 0;

                                for (int k = 0; k < timing.timings.size(); k++) {
                                    if ((songProgress - timing.start) * 1.0 >= timing.timings.get(k).timing && k + 1 < timing.timings.size()) {
                                        cur = k + 1;
                                    }

                                }

                                StringBuilder sb = new StringBuilder();

                                WordTiming prev;
                                if (cur - 1 < 0) {
                                    if (j - 1 < 0) {
                                        prev = timing.timings.get(0);
                                    } else {
                                        prev = timings.get(j - 1).timings.get(timings.get(j - 1).timings.size() - 1);
                                    }
//                                prev = timing.timings.get(0);
                                } else {
                                    prev = timing.timings.get(cur - 1);
                                }

                                for (int m = 0; m < cur; m++) {
                                    sb.append(timing.timings.get(m).word);
                                }

                                double offsetX = (songProgress - timing.start - (cur == 0 ? 0 : prev.timing)) / (double) (timing.timings.get(cur).timing - (cur == 0 ? 0 : prev.timing)) * fontRenderer.getStringWidth(timing.timings.get(cur).word);
                                double percent = (double) (sb.length() + 0.0001) / (double) lyric.getLyric().length();

//                            if (timing.start != lyric.timeStamp /*&& (!timing.totalLyric.equals(lyric.getLyric()))*/)
//                                length = 0;

//

                                lyric.scrollWidth = fontRenderer.getStringWidth(sb.toString()) + offsetX;
                            }

                            if (this.scrollMode.getValue() == ScrollMode.Style2) {
                                int cur = 0;

                                for (int k = 0; k < timing.timings.size(); k++) {
                                    if ((songProgress - timing.start)/* * 1.45*/ >= timing.timings.get(k).timing && k + 1 < timing.timings.size()) {
                                        cur = k + 1;
                                    }

                                }
                                StringBuilder sb = new StringBuilder();
                                for (int m = 0; m <= cur; m++) {
                                    sb.append(timing.timings.get(m).word);
                                }
                                double length = (sb.toString().trim().length() + 0.0001) / (double) lyric.getLyric().length();

//                            if (timing.start != lyric.timeStamp /*&& (!timing.totalLyric.equals(lyric.getLyric()))*/)
//                                length = 0;

//                                FontManager.shs25.drawString(sb.toString(), 50, 50, RenderSystem.hexColor(255, 255, 255));
//                                FontManager.shs25.drawString(String.valueOf(length), 50, 70, RenderSystem.hexColor(255, 255, 255));
//                                FontManager.shs25.drawString(timing.timings.get(cur).word, 50, 90, RenderSystem.hexColor(255, 255, 255));

                                lyric.scrollWidth = AnimationSystem.interpolate(lyric.scrollWidth, length * fontRenderer.getStringWidth(lyric.getLyric()), 0.15);
                            }

//                            lyric.scrollWidth = percent;
                            break;
                        }
                    }
                }

                if (lyric == currentDisplaying) {

                    if (!reduceShaders) {
                        if (hasScrollTimings) {
                            blendBuffer.framebufferClear();
                            blendBuffer.bindFramebuffer(true);
                            drawQuads2(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2.0 - (this.scrollMode.getValue() == ScrollMode.Style1 ? 10 : 0), offsetY, lyric.scrollWidth, fontRenderer.getHeight(), RenderSystem.hexColor(255, 255, 255, 255));
                            drawHorizontalGradientRect(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2.0 + lyric.scrollWidth - (this.scrollMode.getValue() == ScrollMode.Style1 ? 10 : 0), offsetY, 10, fontRenderer.getHeight(), RenderSystem.hexColor(255, 255, 255, 255), 0);
                            blendBuffer.unbindFramebuffer();

                            backgroundBuffer.framebufferClear();
                            backgroundBuffer.bindFramebuffer(true);
                            fontRenderer.drawCenteredString(lyric.getLyric(), this.getX() + this.getWidth() / 2.0, offsetY, new Color(255, 255, 255, 255).getRGB());
                            backgroundBuffer.unbindFramebuffer();

                            mc.getFramebuffer().bindFramebuffer(true);
                            StencilShader.render(blendBuffer.framebufferTexture, backgroundBuffer.framebufferTexture);
                        }
//

                    } else {
                        Stencil.write();
                        drawQuads2(this.getX() + this.getWidth() / 2.0 - fontRenderer.getStringWidth(lyric.getLyric()) / 2.0 - (this.scrollMode.getValue() == ScrollMode.Style1 ? 10 : 0), offsetY, lyric.scrollWidth, fontRenderer.getHeight() + 2, RenderSystem.hexColor(255, 255, 255, 255));
                        Stencil.erase(true);
                        fontRenderer.drawCenteredString(lyric.getLyric(), this.getX() + this.getWidth() / 2.0, offsetY, -1);

                        Stencil.dispose();
                    }
                }

                offsetY += this.getLyricHeight();
            }
        }
        GlStateManager.popMatrix();
//        RenderSystem.endScissor();
//        Stencil.dispose();
        RenderSystem.endScissor();

        this.setWidth(width.getValue());
        this.setHeight(height.getValue());
    }

    public static boolean hasSecondaryLyrics() {
        return hasTransLyrics || hasRomalrc;
    }

    public static String getSecondaryLyrics(LyricBean bean) {
        if (hasTransLyrics) {
            if (!WidgetsManager.musicLyrics.showRoman.getValue()) {
                return StringUtils.returnEmptyStringIfNull(bean.getCLyric());
            } else {
                if (hasRomalrc)
                    return StringUtils.returnEmptyStringIfNull(bean.getRLyric());
                else
                    return StringUtils.returnEmptyStringIfNull(bean.getCLyric());
            }
        }

        if (hasRomalrc) {
            if (WidgetsManager.musicLyrics.showRoman.getValue()) {
                return StringUtils.returnEmptyStringIfNull(bean.getRLyric());
            } else {
                return "";
            }
        }


        return "";
    }

    public void drawQuads(double x, double y, double width, double height) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(0, 1);
        GL11.glVertex2d(x, y);
        GL11.glTexCoord2d(0, 0);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2d(1, 0);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(x + width, y);
        GL11.glEnd();
    }
    
    public double getLyricHeight() {
        return fontRenderer.getHeight() + (hasTransLyrics ? 0 : -10) + 16;
    }
}
