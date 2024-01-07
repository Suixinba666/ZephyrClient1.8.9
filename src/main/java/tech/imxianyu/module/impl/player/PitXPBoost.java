package tech.imxianyu.module.impl.player;

import lombok.SneakyThrows;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Formatting;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.packet.ReceivePacketEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.rendering.notification.Notification;
import tech.imxianyu.rendering.notification.NotificationManager;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.utils.entity.PlayerUtils;
import tech.imxianyu.utils.multithreading.MultiThreadingUtil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Random;

public class PitXPBoost extends Module {


    public BooleanSetting autoMath = new BooleanSetting("Auto Fast Math", false);
    public BooleanSetting cakeReminder = new BooleanSetting("Cake Reminder", false);
    @Handler
    public void onReceive(ReceivePacketEvent event) {
        if (event.getPacket() instanceof S02PacketChat) {
            String msg = ((S02PacketChat) event.getPacket()).getChatComponent().getUnformattedText();
            String pattern = "速算! 在聊天栏里写下你的答案: ";
            if (msg.contains(pattern) && autoMath.getValue()) {
                String x = msg.replaceAll(pattern, "").replaceAll("x", "*").replaceAll("÷", "/");
                int result;
                if (x.contains("+")) {
                    String[] split = x.split("\\+");
                    int left = Integer.parseInt(split[0]);
                    int right = Integer.parseInt(split[1]);
                    result = left + right;
                } else if (x.contains("-")) {
                    String[] split = x.split("-");
                    int left = Integer.parseInt(split[0]);
                    int right = Integer.parseInt(split[1]);
                    result = left - right;
                } else if (x.contains("*")) {
                    String[] split = x.split("\\*");
                    int left = Integer.parseInt(split[0]);
                    int right = Integer.parseInt(split[1]);
                    result = left * right;
                } else if (x.contains("/")) {
                    String[] split = x.split("/");
                    int left = Integer.parseInt(split[0]);
                    int right = Integer.parseInt(split[1]);
                    result = left / right;
                } else {
                    result = 0;
                }

                MultiThreadingUtil.runAsync(() -> {
                    try {
                        Thread.sleep(500 + new Random().nextInt(500));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    mc.thePlayer.sendChatMessage(String.valueOf(result));
                    mc.thePlayer.addChatMessage(new ChatComponentText(Formatting.GOLD + "速算: " + Formatting.GREEN + result));
                });
            }


            if (msg.contains("生成") && msg.contains("巨型蛋糕") && cakeReminder.getValue()) {
                MultiThreadingUtil.runAsync(
                        new Runnable() {
                            @Override
                            @SneakyThrows
                            public void run() {
                                NotificationManager.show("蛋糕已生成!", Notification.Type.INFO, 5000);
                                long bigSleep = 250, smallSleep = 80;
                                PlayerUtils.playNotes("135!1");

                                Thread.sleep(bigSleep);

                                PlayerUtils.playNotes("146!1");

                                Thread.sleep(bigSleep);

                                PlayerUtils.playNotes("146!1");

                                Thread.sleep(bigSleep);

                                PlayerUtils.playNotes("1");
                                Thread.sleep(smallSleep);

                                PlayerUtils.playNotes("3");
                                Thread.sleep(smallSleep);

                                PlayerUtils.playNotes("5");
                                Thread.sleep(smallSleep);

                                PlayerUtils.playNotes("!1");
                                Thread.sleep(smallSleep);

                                PlayerUtils.playNotes("5");
                                Thread.sleep(smallSleep);

                                PlayerUtils.playNotes("3");
                                Thread.sleep(smallSleep);

                                PlayerUtils.playNotes("1");
                                Thread.sleep(smallSleep);
                            }
                        }
                );
            }
        }
    };

    public PitXPBoost() {
        super("Pit XP Boost", Category.PLAYER);
    }

    private String subs(String text, int start, int end) {
        StringBuilder sb = new StringBuilder();

        int cur = end - 1;

        boolean canCont = true;

        while (canCont) {

            try {
                if (cur < start)
                    break;
                sb.append(text, cur, cur + 1);
                cur--;
            } catch (Exception e) {
                canCont = false;
            }
        }

        return sb.reverse().toString();
    }

    public String alphaNumToChinese(String num) {
        String result = "";

        String one = subs(num, num.length() - 1, num.length());
        String ten = subs(num, num.length() - 2, num.length() - 1);
        String hundred = subs(num, num.length() - 3, num.length() - 2);
        String thousand = subs(num, num.length() - 4, num.length() - 3);
        String tenThousand = subs(num, num.length() - 8, num.length() - 4);
        String billion = subs(num, 0, num.length() - 8);


        if (!billion.equals("0") && !billion.isEmpty()) {
            result += this.alphaNumToChinese(billion) + "亿";
        }

        if (!tenThousand.equals("0") && !tenThousand.isEmpty()) {
            result += this.alphaNumToChinese(tenThousand) + "万";
        }

        if (!thousand.equals("0") && !thousand.isEmpty()) {
            result += this.getNumInChinese(thousand) + "千";
        }

        if (!hundred.equals("0") && !hundred.isEmpty()) {
            result += this.getNumInChinese(hundred) + "百";
        }

        if (!ten.equals("0") && !ten.isEmpty()) {
            result += this.getNumInChinese(ten) + "十";
        }

        if (!one.equals("0") && !one.isEmpty()) {
            result += this.getNumInChinese(one);
        }


        return result;
    }

    private String getNumInChinese(String str) {
        StringBuilder result = new StringBuilder();

        for (char c : str.toCharArray()) {

            switch (c) {
                case '0':
                    result.append("零");
                    break;
                case '1':
                    result.append("一");
                    break;
                case '2':
                    result.append("二");
                    break;
                case '3':
                    result.append("三");
                    break;
                case '4':
                    result.append("四");
                    break;
                case '5':
                    result.append("五");
                    break;
                case '6':
                    result.append("六");
                    break;
                case '7':
                    result.append("七");
                    break;
                case '8':
                    result.append("八");
                    break;
                case '9':
                    result.append("九");
                    break;
            }
        }

        return result.toString();
    }
}
