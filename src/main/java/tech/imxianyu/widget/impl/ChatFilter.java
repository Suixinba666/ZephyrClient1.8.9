package tech.imxianyu.widget.impl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Formatting;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.ChatComponentEvent;
import tech.imxianyu.events.ChatEvent;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.RoundedRect;
import tech.imxianyu.rendering.ShaderUtils;
import tech.imxianyu.rendering.Stencil;
import tech.imxianyu.rendering.entities.impl.Rect;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.rendering.rendersystem.RenderSystem;
import tech.imxianyu.widget.Widget;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatFilter extends Widget {

    public ChatFilter() {
        super("ChatFilter");
        super.setDescription("Show filtered chats.");
        super.setResizable(true, 200, 200);
    }

    List<String> text = new ArrayList<>();
    List<String> textToRender = new ArrayList<>();


    @Override
    public void onRender(Render2DEvent event, boolean editing) {


        double x = this.getX(), y = this.getY();
        double width = this.getWidth(), height = this.getHeight();
        ShaderUtils.doRoundedBlurAndBloom(x, y, width, height, 5);

        FontManager.pf18.drawString("AntiCheat Logs", x + 4, y + 3, -1);

        GlStateManager.alphaFunc(516, 0.0F);

        RoundedRect.drawRound(x + 3, y + 15, width - 6, height - 18, 5, RenderSystem.hexColor(255, 255, 255, 24));

        ZFontRenderer fontRenderer = FontManager.pf12;

        double offsetX = x + 6, offsetY = y + 19;

        Stencil.write();
        Rect.draw(x + 3, y + 15, width - 6, height - 18, -1, Rect.RectType.EXPAND);
        Stencil.erase(true);

        for (String s : this.textToRender) {
            fontRenderer.drawString(s, offsetX, offsetY, -1);

            offsetY += fontRenderer.getHeight() + 2;
        }

        Stencil.dispose();


    }

    @Handler
    public void onChat(ChatComponentEvent event) {
        String msg = event.getComponent().getUnformattedText();

        if (isSkipped(msg)) {
            event.setCancelled();
            return;
        }

        if (this.isFiltered(msg)) {

            event.setCancelled();

            this.text.add(event.getComponent().getFormattedText());
            this.updateList();

        }

    }

    private String processAntiCheatLog(String input) {

        input = input.substring(6);

        String[] split = input.split(" use ");

        String userName = split[0];
        String check = split[1];

        String checkName = check.substring(0, check.indexOf("-("));
        String checkDesc = this.getInnerText("-(", ")", check);
        String vl = this.getInnerText(" (+", ")", check);

        //
        return String.format(
                "%s%s%s: [%s%s%s] %s%sVL%s",
                Formatting.RED, userName, Formatting.RESET,
                Formatting.GOLD, checkName, Formatting.RESET,
                checkDesc.isEmpty() ? "" : String.format(
                        "(%s%s%s) ",
                        Formatting.GREEN, checkDesc, Formatting.RESET
                ),
                Formatting.AQUA, vl
        );
    }

    private String getInnerText(String left, String right, String text) {
        if (left.isEmpty() || right.isEmpty() || text.isEmpty()) {
            return "";
        }

        int i = text.indexOf(left);
        int j = text.indexOf(right, i);
        if (i == -1 || j == -1)
            return "";

        return text.substring(i + left.length(), j);
    }

    private void updateList() {

        ZFontRenderer fontRenderer = FontManager.pf12;

        double availableSpace = (this.getHeight() - 25);
        double fontHeight = fontRenderer.getHeight() + 2;

        int availableLines = (int) (availableSpace / fontHeight);

        if (text.size() < availableLines) {
            this.textToRender = text;
            return;
        }

        this.textToRender = text.subList(text.size() - availableLines, text.size());

    }

    List<String> filteredList = Arrays.asList("多杀!", "助攻!", "赏金!", "连杀!");

    public boolean isFiltered(String msg) {
        for (String s : filteredList) {
            if (msg.startsWith(s))
                return true;
        }

        return false;
    }

    private boolean isSkipped(String msg) {

        if (msg.startsWith("多杀! [玩家]  null"))
            return true;

        if (msg.startsWith("助攻!") && msg.contains("[玩家]  null"))
            return true;

        return false;
    }

    @Override
    public void onResized(double lastWidth, double lastHeight) {
        this.updateList();
    }
}
