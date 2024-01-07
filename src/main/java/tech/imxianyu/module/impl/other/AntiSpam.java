package tech.imxianyu.module.impl.other;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.IChatComponent;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.ChatComponentEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.notification.Notification;
import tech.imxianyu.rendering.notification.NotificationManager;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.other.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AntiSpam extends Module {

    NumberSetting<Integer> degreeOfFit = new NumberSetting<Integer>("Similarity Ratio", 90, 1, 100, 1) {
        @Override
        public String getStringForRender() {
            return super.getStringForRender() + "%";
        }
    };

    BooleanSetting removePrevious = new BooleanSetting("Remove Previous", false);
    BooleanSetting notification = new BooleanSetting("Notification On Removal", false);

    int filteredNum = 0;

    @Handler
    public void onTick(TickEvent event) {
        this.setSuffix("Filtered: " + filteredNum);
    };

    @Handler
    public void onEvent(ChatComponentEvent event) {
        List<ChatLine> chatLines = event.getChatLines();
        if (chatLines.isEmpty()) {
            return;
        }
        GuiNewChat chatGUI = this.mc.ingameGUI.getChatGUI();
        List<IChatComponent> splitText = GuiUtilRenderComponents.splitText(event.getComponent(), floor(chatGUI.getChatWidth() / chatGUI.getChatScale()), this.mc.fontRendererObj, false, false);

        int i = 0;

        boolean cont = false/*, filtered = false*/;

        while (i < chatLines.size()) {
            for (int j = 0; j < splitText.size(); j++) {

                if (i + j > chatLines.size() - 1)
                    break;

                ChatLine chatLine = chatLines.get(i + j);
                String text = chatLine.getChatComponent().getUnformattedText();

                IChatComponent chatComponent = splitText.get(j);
                String text2 = chatComponent.getUnformattedText();
                float similarityRatio = StringUtils.getSimilarityRatio(text, text2);
                if (similarityRatio < degreeOfFit.getValue() || checkUserName(text) || checkUserName(text2))
                    cont = true;
                else {
                    if (removePrevious.getValue()) {
                        for (int k = 0; k < splitText.size(); k++) {
                            if (i + k < chatLines.size() - 1) {
                                chatLines.remove(i + k);

                            }
                        }
                    } else {
                        event.setCancelled();
                    }
//                    System.out.println("From: " + text + ", To: " + text2);
                    if (notification.getValue()) {
                        NotificationManager.show("AntiSpam", "\"" + text2 + "\" is removed! [Ratio: " + similarityRatio + "]", Notification.Type.INFO, 2000);
                    }
//                    filtered = true;
                    filteredNum ++;
                    break;
                }
            }
            if (cont)
                i ++;
            else {
                break;
            }
        }
    };

    private boolean checkUserName(String input) {
        return input.contains(mc.getSession().getUsername()) && input.length() - mc.getSession().getUsername().length() <= 5;
    }


    public AntiSpam() {
        super("Anti Spam", Category.OTHER);
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static int floor(float n) {
        int n2 = (int) n;
        int n3;
        if (n < n2) {
            n3 = n2 - 1;
        } else {
            n3 = n2;
        }
        return n3;
    }

    public static int floor(double n) {
        int n2 = (int) n;
        int n3;
        if (n < n2) {
            n3 = n2 - 1;
        } else {
            n3 = n2;
        }
        return n3;
    }
}
