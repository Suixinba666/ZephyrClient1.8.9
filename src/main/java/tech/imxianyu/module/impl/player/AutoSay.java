package tech.imxianyu.module.impl.player;

import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import net.minecraft.util.Formatting;
import tech.imxianyu.Zephyr;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.ModeSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.settings.StringModeSetting;
import tech.imxianyu.utils.timing.Timer;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author ImXianyu
 * @since 7/4/2023 6:13 PM
 */
public class AutoSay extends Module {

    public AutoSay() {
        super("Auto Say", Category.PLAYER);
        replacements.put("%player%", this::getRandomPlayerName);
    }

    public NumberSetting<Integer> delay = new NumberSetting<>("Delay (Seconds)", 5, 1, 60, 1);

    public StringModeSetting mode = new StringModeSetting("Mode", "Advertisement", "Advertisement") {
        @Override
        public void onModeChanged(String before, String now) {
            reInitSayings();
        }
    };

    public final Map<String, Integer> sayings = new HashMap<>();
    public final Map<String, Supplier<String>> replacements = new HashMap<>();

    public final Timer timer = new Timer();

    public final Random random = new Random();

    @Handler
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !timer.isDelayed(delay.getValue() * 1000))
            return;

        timer.reset();

        if (sayings.isEmpty()) {
            mc.thePlayer.addChatMessage(Formatting.RED + "Err: Saying is empty!");
            this.toggle();
            return;
        }

        String randomSaying = this.getRandomSaying();

        for (Map.Entry<String, Supplier<String>> ent : this.replacements.entrySet()) {
            while (randomSaying.contains(ent.getKey()))
                    randomSaying = randomSaying.replaceFirst(ent.getKey(), ent.getValue().get());
        }

        mc.thePlayer.sendChatMessage(randomSaying);
    };

    public String getRandomPlayerName() {
        return mc.theWorld.playerEntities.get(Math.abs(random.nextInt()) % mc.theWorld.playerEntities.size()).getName();
    }

    public String getRandomSaying() {
        int smallest = Integer.MAX_VALUE;
        List<String> smallests = new ArrayList<>();
        for (Map.Entry<String, Integer> set : sayings.entrySet()) {
            if (set.getValue() < smallest) {
                smallest = set.getValue();
                smallests.clear();
                smallests.add(set.getKey());
            } else if (set.getValue() == smallest) {
                smallests.add(set.getKey());
            }
        }

        int r = Math.abs(random.nextInt()) % smallests.size();
        String cihui = smallests.get(r);

        sayings.replace(cihui, sayings.get(cihui) + 1);

        return cihui;
    }

    @Override
    public void onEnable() {
        this.reInitSayings();
    }

    @SneakyThrows
    private void reInitSayings() {
        sayings.clear();

        if (mode.getValue().equals("Advertisement")) {
            for (String ad : this.getAdvertisements()) {
                sayings.put(ad, 0);
            }
        } else {
            File configDir = Zephyr.getInstance().getConfigManager().configDir;
            File autoSayDir = new File(configDir, "AutoSay");

            if (!autoSayDir.exists()) {
                autoSayDir.mkdir();

                File placeHolder = new File(autoSayDir, "place_your_txt_here");
                placeHolder.createNewFile();
            }

            File cur = new File(autoSayDir, mode.getValue() + ".txt");

            if (!cur.exists()) {
                System.out.println("NOT EXIST");
                return;
            }

            Scanner scanner = new Scanner(new FileReader(cur)).useDelimiter("\n");

            while (scanner.hasNext()) {
                String s = scanner.nextLine();

                if (!s.trim().startsWith("//"))
                    sayings.put(s, 0);
            }

            this.print("Loaded Saying %s (%d sayings.)", mode.getValue(), sayings.size());
        }
    }

    private List<String> getAdvertisements() {
        return Arrays.asList("Zephyr Client | DONT SEARCH IT ON THE INTERNET!");
    }

    @Override
    @SneakyThrows
    public void loadConfig(JsonObject directory) {

        List<String> sayings = new ArrayList<>();
        sayings.add("Advertisement");

        File configDir = Zephyr.getInstance().getConfigManager().configDir;
        File autoSayDir = new File(configDir, "AutoSay");

        if (!autoSayDir.exists()) {
            autoSayDir.mkdir();

            File placeHolder = new File(autoSayDir, "place_your_txt_here");
            placeHolder.createNewFile();
        }

        File[] files = autoSayDir.listFiles(pathname -> pathname.getName().endsWith(".txt"));

        if (files != null) {
            for (File file : files) {
                sayings.add(file.getName().substring(0, file.getName().lastIndexOf(".")));
            }
        }

        int idx = this.getSettings().indexOf(mode);

        mode = new StringModeSetting("Mode", sayings.get(0), sayings) {
            @Override
            public void onModeChanged(String before, String now) {
                reInitSayings();
            }
        };

        this.getSettings().set(idx, mode);

        super.loadConfig(directory);
    }
}
