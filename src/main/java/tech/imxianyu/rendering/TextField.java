package tech.imxianyu.rendering;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;
import org.lwjglx.input.Keyboard;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.animation.AnimationSystem;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.utils.math.MathUtils;

import java.awt.*;

public class TextField extends GuiTextField {
    private final int textFieldNumber;
    private final float yOffset = -1.5f;
    public float xPosition;
    public float yPosition;
    /**
     * The width of this text field.
     */
    public float width;
    public float height;
    public boolean dragging;
    public int startChar;
    public int endChar;
    public MsTimer backspaceTime = new MsTimer();
    public String placeholder = "";
    public boolean isPassword;
    /**
     * Has the current text being edited on the textbox.
     */
    private String text = "";
    private int maxStringLength = 128;
    private int cursorCounter;
    private boolean enableBackgroundDrawing = true;
    private boolean drawLineUnder = true;
    /**
     * if true the textbox can lose focus by clicking elsewhere on the screen
     */
    private boolean canLoseFocus = true;
    /**
     * If this value is true along with isEnabled, keyTyped will process the keys.
     */
    private boolean isFocused;
    /**
     * If this value is true along with isFocused, keyTyped will process the keys.
     */
    private boolean isEnabled = true;
    /**
     * The current character index that should be used as start of the rendered text.
     */
    private int lineScrollOffset;
    private float offset;
    private float lastOffset;
    private int cursorPosition;

    private ZFontRenderer fontRenderer = FontManager.pf18;
    /**
     * other selection position, maybe the same as the cursor
     */
    private int selectionEnd;
    public int enabledColor = 14737632;
    private int disabledColor = 7368816;
    /**
     * True if this textbox is visible
     */
    private boolean visible = true;
    private GuiPageButtonList.GuiResponder field_175210_x;
    private Predicate field_175209_y = Predicates.alwaysTrue();
    private float lineOffset;

    private float howerAlpha;

    private Color lineColor;
    private Color fontColor;

    private float wholeAlpha = 1;
    private float opacity;

    public TextField(int number, float x, float y, int width, int height) {
        super(number, Minecraft.getMinecraft().fontRendererObj, (int) x, (int) y, width, height);
        this.textFieldNumber = number;
        this.xPosition = x;
        this.yPosition = y;
        this.width = width;
        this.height = height;
        this.lineColor = new Color(160, 160, 160);
    }

    public TextField(int number, float x, float y, int width, int height, float yOffset) {
        super(number, Minecraft.getMinecraft().fontRendererObj, (int) x, (int) y, width, height);
        this.textFieldNumber = number;
        this.xPosition = x;
        this.yPosition = y;
        this.width = width;
        this.height = height;
        this.lineColor = new Color(160, 160, 160);
    }

    public void setLineOffset(float lineOffset) {
        this.lineOffset = lineOffset;
    }

    public void setWholeAlpha(float wholeAlpha) {
        this.wholeAlpha = wholeAlpha;
    }

    public void setPlaceholder(String s) {
        placeholder = s;
    }

    public void func_175207_a(GuiPageButtonList.GuiResponder p_175207_1_) {
        this.field_175210_x = p_175207_1_;
    }

    /**
     * Increments the cursor counter
     */
    public void updateCursorCounter() {
        ++this.cursorCounter;
    }

    /**
     * Returns the contents of the textbox
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets the text of the textbox
     */
    public void setText(String p_146180_1_) {
        this.text = p_146180_1_;
        this.setCursorPositionEnd();
    }

    /**
     * returns the text between the cursor and selectionEnd
     */
    public String getSelectedText() {
        int var1 = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int var2 = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return this.text.substring(var1, var2);
    }

    public void func_175205_a(Predicate p_175205_1_) {
        this.field_175209_y = p_175205_1_;
    }

    /**
     * replaces selected text, or inserts text at the position on the cursor
     */
    public void writeText(String p_146191_1_) {
        String var2 = "";
        String var3 = ChatAllowedCharacters.filterAllowedCharacters(p_146191_1_);
        int var4 = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int var5 = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        int var6 = this.maxStringLength - this.text.length() - (var4 - var5);
        boolean var7 = false;

        if (this.text.length() > 0) {
            var2 = var2 + this.text.substring(0, var4);
        }

        int var8;

        if (var6 < var3.length()) {
            var2 = var2 + var3.substring(0, var6);
            var8 = var6;
        } else {
            var2 = var2 + var3;
            var8 = var3.length();
        }

        if (this.text.length() > 0 && var5 < this.text.length()) {
            var2 = var2 + this.text.substring(var5);
        }

        if (this.field_175209_y.apply(var2)) {
            this.text = var2;
            this.moveCursorBy(var4 - this.selectionEnd + var8/* - 1*/);

            if (this.field_175210_x != null) {
                this.field_175210_x.func_175319_a(this.textFieldNumber, this.text);
            }
        }
    }

    /**
     * Deletes the specified number of words starting at the cursor position. Negative numbers will delete words left of
     * the cursor.
     */
    public void deleteWords(int p_146177_1_) {
        if (this.text.length() != 0) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(p_146177_1_) - this.cursorPosition);
            }
        }
    }

    /**
     * delete the selected text, otherwsie deletes characters from either side of the cursor. params: delete num
     */
    public void deleteFromCursor(int p_146175_1_) {
        if (this.text.length() != 0) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean var2 = p_146175_1_ < 0;
                int var3 = var2 ? this.cursorPosition + p_146175_1_ : this.cursorPosition;
                int var4 = var2 ? this.cursorPosition : this.cursorPosition + p_146175_1_;
                String var5 = "";

                if (var3 >= 0) {
                    var5 = this.text.substring(0, var3);
                }

                if (var4 < this.text.length()) {
                    var5 = var5 + this.text.substring(var4);
                }

                this.text = var5;

                if (var2) {
                    this.moveCursorBy(p_146175_1_);
                }

                if (this.field_175210_x != null) {
                    this.field_175210_x.func_175319_a(this.textFieldNumber, this.text);
                }
            }
        }
    }

    public int func_175206_d() {
        return this.textFieldNumber;
    }

    /**
     * see @getNthNextWordFromPos() params: N, position
     */
    public int getNthWordFromCursor(int p_146187_1_) {
        return this.getNthWordFromPos(p_146187_1_, this.getCursorPosition());
    }

    /**
     * gets the position of the nth word. N may be negative, then it looks backwards. params: N, position
     */
    public int getNthWordFromPos(int p_146183_1_, int p_146183_2_) {
        return this.func_146197_a(p_146183_1_, p_146183_2_, true);
    }

    public int func_146197_a(int p_146197_1_, int p_146197_2_, boolean p_146197_3_) {
        int var4 = p_146197_2_;
        boolean var5 = p_146197_1_ < 0;
        int var6 = Math.abs(p_146197_1_);

        for (int var7 = 0; var7 < var6; ++var7) {
            if (var5) {
                while (p_146197_3_ && var4 > 0 && this.text.charAt(var4 - 1) == 32) {
                    --var4;
                }

                while (var4 > 0 && this.text.charAt(var4 - 1) != 32) {
                    --var4;
                }
            } else {
                int var8 = this.text.length();
                var4 = this.text.indexOf(32, var4);

                if (var4 == -1) {
                    var4 = var8;
                } else {
                    while (p_146197_3_ && var4 < var8 && this.text.charAt(var4) == 32) {
                        ++var4;
                    }
                }
            }
        }

        return var4;
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    public void moveCursorBy(int by) {
        this.setCursorPosition(this.selectionEnd + by);
    }

    /**
     * sets the cursors position to the beginning
     */
    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    /**
     * sets the cursors position to after the text
     */
    public void setCursorPositionEnd() {
        this.setCursorPosition(this.text.length());
        startChar = 0;
        endChar = text.length();
    }

    /**
     * Call this method from your GuiScreen to process the keys into the textbox
     */
    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        if (!this.isFocused) {
            return false;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        } else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            if (this.isEnabled) {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            GuiScreen.setClipboardString(this.getSelectedText());

            if (this.isEnabled) {
                this.writeText("");
            }

            return true;
        } else {
            switch (keyCode) {
                case Keyboard.KEY_BACK:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(-1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(-1);
                    }

                    return true;

                case Keyboard.KEY_HOME:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(0);
                    } else {
                        this.setCursorPositionZero();
                    }

                    return true;

                case Keyboard.KEY_LEFT:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() - 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    } else {
                        this.moveCursorBy(-1);
                    }

                    return true;

                case Keyboard.KEY_RIGHT:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() + 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    } else {
                        this.moveCursorBy(1);
                    }

                    return true;

                case Keyboard.KEY_END:
                    if (GuiScreen.isShiftKeyDown()) {
                        this.setSelectionPos(this.text.length());
                    } else {
                        this.setCursorPositionEnd();
                    }

                    return true;

                case Keyboard.KEY_DELETE:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (this.isEnabled) {
                            this.deleteWords(1);
                        }
                    } else if (this.isEnabled) {
                        this.deleteFromCursor(1);
                    }

                    return true;

                default: {

                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                        if (this.isEnabled) {
                            this.writeText(Character.toString(typedChar));
                        }

                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
    }

    public void onTick() {
        lastOffset = offset;
        String missing = this.text.substring(0, this.lineScrollOffset);
        offset += (((getFontRenderer().getStringWidth(missing)) - offset) / (2)) + 0.01;

        if (Keyboard.isKeyDown(14) && isFocused()) {
            if (this.getText().length() > 0 && backspaceTime.sleep(500, false))
                this.setText(this.getText().substring(0, this.getText().length() - 1));
        } else {
            backspaceTime.reset();
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int mouseButton) {
        dragging = false;
    }

    /**
     * Args: x, y, buttonClicked
     */
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean isHovered = mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height;

        if (this.canLoseFocus) {
            if (!isHovered) {
                dragging = false;
                lineScrollOffset = 0;
            }
            this.setFocused(isHovered);
        }

        if (this.isFocused && isHovered && mouseButton == 0) {
            float var5 = (float) (mouseX - this.xPosition);

            if (this.enableBackgroundDrawing) {
                var5 += isPassword ? 1 : 4;
            }
            String var6 = text;
            if (!dragging) {
                if (var6.length() > 0) {
                    if (isPassword) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < var6.length(); i++) {
                            stringBuilder.append("·");
                        }
                        var6 = stringBuilder.toString();
                    }
                    this.startChar = (int) (var5 / (getFontRenderer().getStringWidth(var6) / this.text.length()));
                    this.setCursorPosition(startChar);
                }
                dragging = true;
            }
            //startChar = this.text.length() + this.lineScrollOffset;
            //startChar = 0;
        }
    }

    public int applyAlpha(int color, float alpha) {
        float f = (color >> 24 & 0xFF) * 0.003921568627451F;
        Color c = new Color(color);
        Color c2 = new Color(c.getRed() * 0.003921568627451F, c.getGreen() * 0.003921568627451F, c.getBlue() * 0.003921568627451F, (f) * alpha);
        return c2.getRGB();
    }

    public float smoothTrans(double current, double last) {
        return (float) (current * Minecraft.getMinecraft().timer.renderPartialTicks + (last * (1.0f - Minecraft.getMinecraft().timer.renderPartialTicks)));
    }

    /**
     * Draws the textbox
     */
    public void drawTextBox(int mouseX, int mouseY) {
        if (this.getVisible()) {
//            float percent = smoothTrans(Jello.jgui.percent, Jello.jgui.lastPercent);
//            float outro = smoothTrans(Jello.jgui.outro, Jello.jgui.lastOutro);
//
//            float opacity = (float) (Minecraft.getMinecraft().currentScreen == null ? Math.min(1, Math.max(((1-(outro-1)-.4)*1.66667), 0)) : (float)Math.max(Math.min(-(((percent-1.23)*4)), 1), 0));
            this.wholeAlpha = MathUtils.clamp(wholeAlpha, 0, 1);
            opacity = AnimationSystem.interpolate(opacity, 1.0f, 0.05f) * wholeAlpha;
            double offse = width - getFontRenderer().getStringWidth(text) <= 0 ? getFontRenderer().getStringWidth(text) - width : 0;

            float realOpacity = wholeAlpha;

            lineColor = AnimationSystem.getColorAnimationState(lineColor, (RenderSystem.isHovered(mouseX, mouseY, this.xPosition, this.yPosition, this.width, this.height) || isFocused) ? new Color(255, 133, 155) : new Color(180, 180, 180), 100f);
            howerAlpha = AnimationSystem.getAnimationState(howerAlpha, (RenderSystem.isHovered(mouseX, mouseY, this.xPosition, this.yPosition, this.width, this.height) || isFocused) ? 0.4f : 0.3f, 0.1f) * wholeAlpha;

            if (this.getEnableBackgroundDrawing()) {
                GlStateManager.disableBlend();


                GlStateManager.enableBlend();
                if (drawLineUnder)
                    RenderSystem.drawRect(this.xPosition - 1, this.yPosition + this.height + lineOffset, this.xPosition + this.width + 1, this.yPosition + this.height + 0.5 + lineOffset, RenderSystem.reAlpha(lineColor.getRGB(), wholeAlpha));
            }

            if (this.isFocused && dragging) {
                float mouseXRelative = (mouseX - this.xPosition);

                if (this.enableBackgroundDrawing) {
                    mouseXRelative += isPassword ? 1 : 4;
                }

                String var6 = this.text;//this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getStringWidth(), false, true);
                if (this.text.length() > 0) {
                    endChar = (int) (mouseXRelative / (getFontRenderer().getStringWidth(var6) / this.text.length()));
                    if (endChar > this.text.length()) endChar = this.text.length();
                    if (endChar < 0) endChar = 0;
                    this.setSelectionPos(endChar);
                }
            }

            /*if (this.isFocused && dragging) {
                int var5 = (int) (mouseX - this.xPosition);

                if (this.enableBackgroundDrawing) {
                    var5 += 4;
                }

                String var6 = this.text;//this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getStringWidth(), false, true);
                if (this.text.length() > 0) {
//                    endChar = getTextFieldFont(isPassword).getClickPos(var6, var5);//this.fontRendererInstance.trimStringToWidth(var6, var5, false, true).length() + this.lineScrollOffset;
//                    endChar = (int)(var5 / (getTextFieldFont(isPassword).getStringWidth(var6) / this.text.length()));
//                    if (endChar > this.text.length() - 1) endChar = this.text.length() - 1;
//                    this.setSelectionPos(endChar);
                }
            }*/

            /*int var1 = applyAlpha(this.isEnabled ? Minecraft.getMinecraft().currentScreen instanceof GuiChat ? 0xffffffff : isFocused ? 0xff000000 : 0xff828182 : Minecraft.getMinecraft().currentScreen instanceof GuiChat ? 0xfffffffe : 0xff000001, realOpacity);*/
            int color = applyAlpha(this.enabledColor, realOpacity);
            int positionDiff = this.cursorPosition - this.lineScrollOffset;
            int positionEndDiff = this.selectionEnd - this.lineScrollOffset;


            String text = this.text;//this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getStringWidth(), false, true);
            if (isPassword) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < text.length(); i++) {
                    stringBuilder.append("·");
                }
                text = stringBuilder.toString();
            }

            boolean var5 = positionDiff >= 0 && positionDiff <= text.length();
            boolean var6 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && var5;
            float var7 = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
            float var8 = this.enableBackgroundDrawing ? this.yPosition + (this.height - 4) / 2 : this.yPosition;

            if (positionEndDiff > text.length()) {
                positionEndDiff = text.length();
            }

            boolean erase = Stencil.isErasing;

            if (!erase) {
                Stencil.write();
                RenderSystem.drawRect(xPosition - 1, yPosition, xPosition + this.width + 1, yPosition + this.height - 1, applyAlpha(-1, wholeAlpha));
                Stencil.erase(true);
            }

            if (this.getText().isEmpty() && !placeholder.isEmpty() && !this.isFocused) {
                getFontRenderer().drawString(placeholder, var7 - 3.5f, var8 - 1.5f + yOffset, applyAlpha(enabledColor, howerAlpha));
            }

            if (text.length() > 0) {
                GlStateManager.color(1, 1, 1, 1);
                //getTextFieldFont(isPassword).drawString(var10, (float)var7 - 3.5f, (float)var8, -1);
                GlStateManager.color(1, 1, 1, 1);
            }

            boolean var13 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();

            boolean highlighting = false;
            if (positionEndDiff != positionDiff) {
                GlStateManager.color(1, 1, 1, 1);

                int lowestChar = Math.min(startChar, endChar);
                int highestChar = Math.max(startChar, endChar);

                if (startChar != endChar) {
                    highlighting = true;
                }

                if (lowestChar > text.length()) lowestChar = text.length();
                if (lowestChar < 0) lowestChar = 0;
                if (highestChar > text.length()) highestChar = text.length();
                if (highestChar < 0) highestChar = 0;
                RenderSystem.drawRect(4 + xPosition + getFontRenderer().getStringWidth(text.substring(0, lowestChar)) - offse - 4f, yPosition - 1, 4 + xPosition + getFontRenderer().getStringWidth(text.substring(0, lowestChar)) - offse + getFontRenderer().getStringWidth(text.substring(lowestChar, highestChar)) - 3f, yPosition + height - 1.5f, applyAlpha(new Color(196, 225, 245).getRGB(), realOpacity));
                GlStateManager.color(1, 1, 1, 1);
            }

            //DRAW OVERLAY STRING
            if (text.length() > 0) {
                GlStateManager.color(1, 1, 1, 1);
                getFontRenderer().drawString(text, var7 - offse - 3.5f, var8 + -1.5f + yOffset, this.isFocused() ? enabledColor : disabledColor);
                GlStateManager.color(1, 1, 1, 1);
            }
            if (var6) {
                if (var13) {

                    String sub = "";
                    int alpha = (int) Math.min(255, ((System.currentTimeMillis() / 3 % 255) > 255 / 2 ? (Math.abs(Math.abs(System.currentTimeMillis() / 3) % 255 - 255)) : System.currentTimeMillis() / 3 % 255) * 2);

                    if (startChar > 0)
                        sub = text.substring(0, (startChar));
                    if (!highlighting) {

                        if (startChar > endChar)
                            RenderSystem.drawRect(xPosition + getFontRenderer().getStringWidth(sub) + 3.5f - offse - 3.5f, var8 - 5, xPosition + getFontRenderer().getStringWidth(sub) + 0.5f + 3.5f - offse - 3.5f, var8 + 1 + getFontRenderer().getHeight() - 2, alpha > 255 / 2f ? 0xffcdcbcd : 0xff000000);
                        else
                            RenderSystem.drawRect(xPosition + getFontRenderer().getStringWidth(sub) + 4.0f - offse - 3.5f, var8 - 5, xPosition + getFontRenderer().getStringWidth(sub) + +4.5f - offse - 3.5f, var8 + 1 + getFontRenderer().getHeight() - 2, alpha > 255 / 2f ? 0xffcdcbcd : 0xff000000);

                    }
                } else {
                    GlStateManager.color(1, 1, 1, 1);
                    float alpha = (float) (MathUtils.clamp(80 + (Math.sin(System.nanoTime() * 0.000000009f) * 0.5f + 0.5f) * (255 - 80), 0, 255) * 0.003921568627451F) * wholeAlpha;
                    if (!highlighting) {
                        RenderSystem.drawRect(xPosition + getFontRenderer().getStringWidth(text) + 4.5f - offse - 3f, var8 - 5, xPosition + getFontRenderer().getStringWidth(text) + 0.5f + 4.5f - offse - 4f, var8 + 1 + getFontRenderer().getHeight() - 3, RenderSystem.reAlpha(RenderSystem.hexColor(80, 80, 80, 255), alpha));
                    }
                    GlStateManager.color(1, 1, 1, 1);
                }
            }

            if (!erase) {
                Stencil.dispose();
            }
        }
    }

    public void drawPasswordBox(int mouseX, int mouseY) {
        if (this.getVisible()) {
            float offse = offset * Minecraft.getMinecraft().timer.elapsedPartialTicks + (lastOffset * (1.0f - Minecraft.getMinecraft().timer.elapsedPartialTicks));
            String var4 = text;//text.replaceAll(".", ".");

            if (this.getEnableBackgroundDrawing()) {
                GlStateManager.disableBlend();


                GlStateManager.enableBlend();
                RenderSystem.drawRect(this.xPosition - 1, this.yPosition + this.height, this.xPosition + this.width + 1, this.yPosition + this.height + 1, isFocused ? 0xffcac9ca : 0xffe5e4e5);
                // drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, -16777216);
            }

            if (dragging && mouseX >= xPosition + width) {
                //if(this.getSelectionEnd() < 14)
                this.setSelectionPos(this.getSelectionEnd() + 1);
            }

            if (this.isFocused && dragging) {
                int var5 = (int) (mouseX - this.xPosition);

                if (this.enableBackgroundDrawing) {
                    var5 += 1;
                }

                String var6 = this.text;//this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getStringWidth(), false, true);
                //this.setCursorPosition(this.fontRendererInstance.trimStringToWidth(var6, var5).length() + this.lineScrollOffset);
//                endChar = getTextFieldFont(true).trimStringToWidthPassword(var6, var5, false).length() + this.lineScrollOffset;//this.fontRendererInstance.trimStringToWidth(var6, var5, false, true).length() + this.lineScrollOffset;
//                endChar = (int)(var5 / (getTextFieldFont(isPassword).getStringWidth(var6) / this.text.length()));
//                this.setSelectionPos(endChar);
                if (this.text.length() > 0) {
                    endChar = var5 / (getFontRenderer().getStringWidth(var6) / this.text.length());
                    if (endChar > this.text.length()) endChar = this.text.length();
                    if (endChar < 0) endChar = 0;
                    this.setSelectionPos(endChar);
                }
            }

            int var1 = this.isEnabled ? Minecraft.getMinecraft().currentScreen instanceof GuiChat ? 0xffffffff : isFocused ? 0xff000000 : 0xff828182 : Minecraft.getMinecraft().currentScreen instanceof GuiChat ? 0xfffffffe : 0xff000001;
            int var2 = this.cursorPosition - this.lineScrollOffset;
            int var3 = this.selectionEnd - this.lineScrollOffset;


            //this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getStringWidth(), false, true);
            String missing = var4.substring(0, this.lineScrollOffset);
            boolean var5 = var2 >= 0 && var2 <= var4.length();
            boolean var6 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && var5;
            float var7 = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
            float var8 = this.enableBackgroundDrawing ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
            float var9 = var7;

            if (var3 > var4.length()) {
                var3 = var4.length();
            }


            Stencil.write();
            RenderSystem.drawRect(xPosition - 1, yPosition, xPosition + this.width + 1, yPosition + this.height, -1);
            Stencil.erase(true);

            if (this.getText().isEmpty() && !placeholder.isEmpty() && !this.isFocused) {
                FontManager.pf25.drawString(placeholder, var7 - 3.5f, var8 - 1.5f + yOffset, 0xff8d8b8d);
            }

            String s = var5 ? var4.substring(0, var2) : var4;
            if (var4.length() > 0) {
                String var10 = s;
                GlStateManager.color(1, 1, 1, 1);
                var9 = getFontRenderer().drawString(var10, var7 - 3.5f, var8, -1);
                GlStateManager.color(1, 1, 1, 1);
            }

            boolean var13 = this.cursorPosition < var4.length() || var4.length() >= this.getMaxStringLength();

            if (!var5) {
            } else if (var13) {
                --var9;
            }
            boolean highlighting = false;
            if (var3 != var2) {
                GlStateManager.color(1, 1, 1, 1);
                double var12 = var7 + getFontRenderer().getStringWidth(var4.substring(0, var3)) - 3.5f;
                //this.drawCursorVertical( startChar > endChar ? var11+ offse:var11+2 - offse, var8 - 1, startChar > endChar ? var12+ offse:var12-1 - offse, var8 + 1 + this.fontRendererInstance.FONT_HEIGHT);

                int lowestChar = Math.min(startChar, endChar);
                int highestChar = Math.max(startChar, endChar);

                if ((4 + xPosition + getFontRenderer().getStringWidth(var4.substring(0, lowestChar)) - offse - 3.5f) - (4 + xPosition + getFontRenderer().getStringWidth(var4.substring(0, lowestChar)) - offse + getFontRenderer().getStringWidth(var4.substring(lowestChar, highestChar)) - 3.5f) != 0) {
                    highlighting = true;
                }

                RenderSystem.drawRect(4 + xPosition + getFontRenderer().getStringWidth(var4.substring(0, lowestChar)) - offse - 3.5f, yPosition + 2 - 5 + 4, 4 + xPosition + getFontRenderer().getStringWidth(var4.substring(0, lowestChar)) - offse + getFontRenderer().getStringWidth(var4.substring(lowestChar, highestChar)) - 3.5f, yPosition + height - 1.5f - 2, 0xffadcffe);
                GlStateManager.color(1, 1, 1, 1);
            }


            //DRAW OVERLAY STRING
            if (var4.length() > 0) {
                GlStateManager.color(1, 1, 1, 1);
                getFontRenderer().drawString(var4, var7 - offse - 3.5f, var8 - 1.5f + 6 - 19 / 2f, var1);
                GlStateManager.color(1, 1, 1, 1);
            }
            if (var6) {
                if (var13) {
                    String sub = "";
                    int alpha = (int) Math.min(255, ((System.currentTimeMillis() / 3 % 255) > 255 / 2 ? (Math.abs(Math.abs(System.currentTimeMillis() / 3) % 255 - 255)) : System.currentTimeMillis() / 3 % 255) * 2);

                    if (startChar > 0)
                        sub = var4.substring(0, (startChar));
                    if (!highlighting) {

                        if (startChar > endChar)
                            RenderSystem.drawRect(xPosition + getFontRenderer().getStringWidth(sub) + 3.5f - offse - 3.5f, var8 - 5, xPosition + getFontRenderer().getStringWidth(sub) + 0.5f + 3.5f - offse - 3.5f, var8 + 1 + getFontRenderer().getHeight() - 2, alpha > 255 / 2f ? 0xffcdcbcd : 0xff000000);
                        else
                            RenderSystem.drawRect(xPosition + getFontRenderer().getStringWidth(sub) + 4.0f - offse - 3.5f, var8 - 5, xPosition + getFontRenderer().getStringWidth(sub) + +4.5f - offse - 3.5f, var8 + 1 + getFontRenderer().getHeight() - 2, alpha > 255 / 2f ? 0xffcdcbcd : 0xff000000);

                    }
                } else {
                    GlStateManager.color(1, 1, 1, 1);
                    //if(!dragging)
                    int alpha = (int) Math.min(255, ((System.currentTimeMillis() / 3 % 255) > 255 / 2 ? (255) : 255 / 2f));
                    if (!highlighting) {
                        RenderSystem.drawRect(xPosition + getFontRenderer().getStringWidth(var4) + 4.5f - offse - 4f, var8 - 5, xPosition + getFontRenderer().getStringWidth(var4) + 0.5f + 4.5f - offse - 4f, var8 + 1 + getFontRenderer().getHeight() - 2, alpha > 255 / 2f ? 0xffcdcbcd : 0xff000000);
                    }
                    GlStateManager.color(1, 1, 1, 1);
                }
            }
            Stencil.dispose();

        }
    }

    /**
     * draws the vertical line cursor in the textbox
     */
    private void drawCursorVertical(float f, float p_146188_2_, float g, float p_146188_4_) {
        float var5;

        if (f < g) {
            var5 = f;
            f = g;
            g = var5;
        }

        if (p_146188_2_ < p_146188_4_) {
            var5 = p_146188_2_;
            p_146188_2_ = p_146188_4_;
            p_146188_4_ = var5;
        }

        if (g > this.xPosition + this.width) {
            g = this.xPosition + this.width;
        }

        if (f > this.xPosition + this.width) {
            f = this.xPosition + this.width;
        }

        RenderSystem.drawRect(f + 1.5f, p_146188_2_ + 0.5f, g + 0.5f, p_146188_4_ - 1, 0xffadcffe);

       /* Tessellator var7 = Tessellator.getInstance();
        WorldRenderer var6 = var7.getWorldRenderer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(5387);
        var6.startDrawingQuads();
        var6.addVertex((double)p_146188_1_, (double)p_146188_4_, 0.0D);
        var6.addVertex((double)p_146188_3_, (double)p_146188_4_, 0.0D);
        var6.addVertex((double)p_146188_3_, (double)p_146188_2_, 0.0D);
        var6.addVertex((double)p_146188_1_, (double)p_146188_2_, 0.0D);
        var7.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();*/
    }

    /**
     * returns the maximum number of character that can be contained in this textbox
     */
    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    public void setMaxStringLength(int p_146203_1_) {
        this.maxStringLength = p_146203_1_;

        if (this.text.length() > p_146203_1_) {
            this.text = this.text.substring(0, p_146203_1_);
        }
    }

    /**
     * returns the current position of the cursor
     */
    public int getCursorPosition() {
        return this.cursorPosition;
    }

    /**
     * sets the position of the cursor to the provided index
     */
    public void setCursorPosition(int p_146190_1_) {
        this.cursorPosition = p_146190_1_;
        startChar = p_146190_1_;
        endChar = p_146190_1_;
        int var2 = this.text.length();
        this.cursorPosition = MathHelper.clamp_int(this.cursorPosition, 0, var2);
        this.setSelectionPos(this.cursorPosition);
    }

    /**
     * get enable drawing background and outline
     */
    public boolean getEnableBackgroundDrawing() {
        return this.enableBackgroundDrawing;
    }

    /**
     * enable drawing background and outline
     */
    public void setEnableBackgroundDrawing(boolean enabled) {
        this.enableBackgroundDrawing = enabled;
    }

    /**
     * Sets the text colour for this textbox (disabled text will not use this colour)
     */
    public void setTextColor(int textColor) {
        this.enabledColor = textColor;
    }

    public void setDisabledTextColour(int disabledColor) {
        this.disabledColor = disabledColor;
    }

    /**
     * Getter for the focused field
     */
    public boolean isFocused() {
        return this.isFocused;
    }

    /**
     * Sets focus to this gui element
     */
    public void setFocused(boolean focused) {
        if (focused && !this.isFocused) {
            this.cursorCounter = 0;
        }

        this.isFocused = focused;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    /**
     * the side of the selection that is not the cursor, may be the same as the cursor
     */
    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    /**
     * returns the width of the textbox depending on if background drawing is enabled
     */
    public int getWidth() {
        return (int) (this.getEnableBackgroundDrawing() ? this.width - 8 : this.width);
    }

    /**
     * Sets the position of the selection anchor (i.e. position the selection was started at)
     */
    public void setSelectionPos(int p_146199_1_) {
        int var2 = this.text.length();

        if (p_146199_1_ > var2) {
            p_146199_1_ = var2;
        }

        if (p_146199_1_ < 0) {
            p_146199_1_ = 0;
        }

        this.selectionEnd = p_146199_1_;

        if (this.lineScrollOffset > var2) {
            this.lineScrollOffset = var2;
        }

        float var3 = this.getWidth();
        String var4 = String.join("\n", getFontRenderer().wrapWords(this.text.substring(this.lineScrollOffset), var3));

        float var5 = var4.length() + this.lineScrollOffset;

        if (p_146199_1_ == this.lineScrollOffset) {
            this.lineScrollOffset -= String.join("\n", getFontRenderer().wrapWords(this.text, (int) var3)).length();
        }

        if (p_146199_1_ > var5) {
            this.lineScrollOffset += p_146199_1_ - var5;
        } else if (p_146199_1_ <= this.lineScrollOffset) {
            this.lineScrollOffset -= this.lineScrollOffset - p_146199_1_;
        }

        this.lineScrollOffset = MathHelper.clamp_int(this.lineScrollOffset, 0, var2);
    }

    /**
     * if true the textbox can lose focus by clicking elsewhere on the screen
     */
    public void setCanLoseFocus(boolean p_146205_1_) {
        this.canLoseFocus = p_146205_1_;
    }

    /**
     * returns true if this textbox is visible
     */
    public boolean getVisible() {
        return this.visible;
    }

    /**
     * Sets whether or not this textbox is visible
     */
    public void setVisible(boolean p_146189_1_) {
        this.visible = p_146189_1_;
    }

    public void setDrawLineUnder(boolean drawLineUnder) {
        this.drawLineUnder = drawLineUnder;
    }

    private ZFontRenderer getFontRenderer() {
        return this.fontRenderer;
    }

    public void setFontRenderer(ZFontRenderer fr) {
        this.fontRenderer = fr;
    }


    public final class MsTimer {
        private long time;
        private boolean active;

        public MsTimer() {
            time = System.currentTimeMillis();
            active = true;
        }

        public boolean reach(final long time) {
            if (!active)
                return false;
            return time() >= time;
        }

        public void reset() {
            time = System.currentTimeMillis();
        }

        public boolean sleep(final long time) {
            if (!active)
                return false;
            if (time() >= time) {
                reset();
                return true;
            }
            return false;
        }

        public boolean sleep(final long time, boolean reset) {
            if (!active)
                return false;
            if (time() >= time) {
                if (reset) {
                    reset();
                }
                return true;
            }
            return false;
        }

        public long time() {
            return System.currentTimeMillis() - time;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}