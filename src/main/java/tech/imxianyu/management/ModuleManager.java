package tech.imxianyu.management;

import lombok.Getter;
import lombok.SneakyThrows;
import org.lwjglx.input.Keyboard;
import tech.imxianyu.eventapi.EventBus;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.input.KeyPressedEvent;
import tech.imxianyu.interfaces.AbstractManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.module.impl.combat.*;
import tech.imxianyu.module.impl.exploit.ClientBrandSpoof;
import tech.imxianyu.module.impl.movement.*;
import tech.imxianyu.module.impl.other.*;
import tech.imxianyu.module.impl.player.*;
import tech.imxianyu.module.impl.render.*;
import tech.imxianyu.module.impl.world.AntiBots;
import tech.imxianyu.module.impl.world.Disabler;
import tech.imxianyu.module.impl.world.Nuker;
import tech.imxianyu.settings.Setting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ImXianyu
 * @since 3/26/2023 1:08 PM
 */
public class ModuleManager extends AbstractManager {

    // Combat
    public static final AutoClicker autoClicker = new AutoClicker();
    public static final Reach reach = new Reach();
    public static final BowAimBot bowAimBot = new BowAimBot();
    public static final AimAssist aimAssist = new AimAssist();
    public static final AutoSword autoSword = new AutoSword();
    public static final KillAura killAura = new KillAura();
    public static final Criticals criticals = new Criticals();
    public static final Regen regen = new Regen();
    public static final Backtrack backTrack = new Backtrack();
    //Player
    public static final HeadRotations headRotations = new HeadRotations();
    public static final NoteBot noteBot = new NoteBot();
    public static final AutoEat autoEat = new AutoEat();
    public static final AutoSay autoSay = new AutoSay();
    public static final ChestStealer chestStealer = new ChestStealer();
    public static final AutoArmor autoArmor = new AutoArmor();
    public static final InvCleaner invCleaner = new InvCleaner();
    public static final InvSwapper invSwapper = new InvSwapper();
    public static final PitXPBoost pitXPBoost = new PitXPBoost();
    public static final PingSpoof pingSpoof = new PingSpoof();
    public static final FastUse fastUse = new FastUse();
    public static final AntiHunger antiHunger = new AntiHunger();
    public static final Timer timer = new Timer();
    //Movement
    public static final AutoSprint autoSprint = new AutoSprint();
    public static final FlagFly flagFly = new FlagFly();
    public static final Step step = new Step();
    public static final NoFall noFall = new NoFall();
    public static final BlockFly blockFly = new BlockFly();
    public static final Velocity velocity = new Velocity();
    public static final AutoWalkToPit autoWalkToPit = new AutoWalkToPit();
    public static final Fly fly = new Fly();
    public static final Blink blink = new Blink();
    public static final NoSlow noSlow = new NoSlow();
    public static final Speed speed = new Speed();
    public static final TargetStrafe targetStrafe = new TargetStrafe();
    public static final KeepSprint keepSprint = new KeepSprint();
    public static final Phase phase = new Phase();
    //Render
    public static final Hud hud = new Hud();
    public static final BreadCrumbs breadCrumbs = new BreadCrumbs();
    public static final HotBar hotBar = new HotBar();
    public static final ClickGui clickGui = new ClickGui();
    public static final CameraNoClip cameraNoClip = new CameraNoClip();
    public static final Perspective perspective = new Perspective();
    public static final BlockAnimations blockAnimations = new BlockAnimations();
    public static final WaveyCapes waveyCapes = new WaveyCapes();
    public static final AntiInvisible antiInvisible = new AntiInvisible();
    public static final BlockESP blockESP = new BlockESP();
    public static final Chat chat = new Chat();
    public static final ESP esp = new ESP();
    public static final ItemPhysic itemPhysic = new ItemPhysic();
    public static final MoreParticles moreParticles = new MoreParticles();
    public static final NameTags nameTags = new NameTags();
    public static final NightVision nightVision = new NightVision();
    public static final Wings wings = new Wings();
    //World
    public static final AntiBots antiBots = new AntiBots();
    public static final Disabler disabler = new Disabler();
    public static final Nuker nuker = new Nuker();
    //Other
    public static final AntiSpam antiSpam = new AntiSpam();

    public static final Friends friends = new Friends();

    public static final Test test = new Test();
    public static final NoCommand noCommand = new NoCommand();

    // EXPLOIT
    public static final ClientBrandSpoof clientBrandSpoof = new ClientBrandSpoof();
//    public static final AntiELO antiElo = new AntiELO();

    @Getter
    private static final List<Module> modules = new ArrayList<>();
    @Handler
    public void onKeyPress(KeyPressedEvent event) {
        modules.stream().filter(m -> m.getKeyBind() == event.getKeyCode()).forEach(Module::toggle);

        if (event.getKeyCode() == Keyboard.KEY_INSERT) {
            System.out.println(new String(Base64.getDecoder().decode("W1plcGh5ciBDbGllbnRdIE1hZGUgQnkgSW1YaWFueXU=")));
        }

    };

    public ModuleManager() {
        super();
    }

    public static List<Module> getModulesByCategory(Module.Category category) {

        return modules.stream().filter(m -> m.getCategory() == category).collect(Collectors.toList());

    }

    @Override
    @SneakyThrows
    public void onStart() {
        modules.forEach(EventBus::unregister);

        modules.clear();

        // I didn't want to use reflection at first, but hey, it just works and I don't give a fuck :)
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (Module.class.isAssignableFrom(field.getType())) {
                Module module = (Module) field.get(null);

                for (Field moduleField : module.getClass().getDeclaredFields()) {
                    moduleField.setAccessible(true);

                    if (Setting.class.isAssignableFrom(moduleField.getType())) {
                        module.addSettings((Setting<?>) moduleField.get(module));
                    }
                }

                modules.add(module);
            }
        }


        modules.sort(Comparator.comparing(Module::getName));
    }

    @Override
    public void onStop() {

    }

    public Module getModuleByName(String name) {

        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name))
                return module;
        }

        return null;
    }
}
