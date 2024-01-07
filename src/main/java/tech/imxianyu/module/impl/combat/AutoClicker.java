package tech.imxianyu.module.impl.combat;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import org.lwjglx.input.Mouse;
import tech.imxianyu.eventapi.Event;
import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.events.rendering.Render2DEvent;
import tech.imxianyu.events.world.TickEvent;
import tech.imxianyu.management.ModuleManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;
import tech.imxianyu.settings.NumberSetting;
import tech.imxianyu.utils.entity.PlayerUtils;
import tech.imxianyu.utils.timing.Timer;

import java.time.Duration;
import java.util.List;
import java.util.Random;

/**
 * @author ImXianyu
 * @since 6/19/2023 12:52 PM
 */
public class AutoClicker extends Module {

    private final Timer delayTimer = new Timer();
    public BooleanSetting perfectHit = new BooleanSetting("Perfect Hit", false);
    public BooleanSetting blockHit = new BooleanSetting("Block Hit", false);
    public BooleanSetting breakBlock = new BooleanSetting("Break blocks", false);
    public BooleanSetting multiAttack = new BooleanSetting("Multi Attack", false);
    public BooleanSetting attackPlayersFirst = new BooleanSetting("Attack Players First", false, () -> multiAttack.getValue());    public NumberSetting<Double> minCPS = new NumberSetting<Double>("Min CPS", 7.0d, 1.0d, 30.0d, 0.1d) {
        @Override
        public void onValueChanged(Double last, Double now) {
            if (now > maxCPS.getValue()) {
                this.setValue(maxCPS.getValue());
            }
        }
    };
    public NumberSetting<Double> maxCPS = new NumberSetting<Double>("Max CPS", 9.0d, 1.0d, 30.0d, 0.1d) {
        @Override
        public void onValueChanged(Double last, Double now) {
            if (now < minCPS.getValue()) {
                this.setValue(minCPS.getValue());
            }
        }
    };
    private int mAttackIdx = 0;    private double lastDelay = randomClickDelay(minCPS.getValue(), maxCPS.getValue());
    private final Timer blocktimer = new Timer();
    public AutoClicker() {
        super("Auto Clicker", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        mAttackIdx = 0;
    }

    public double randomClickDelay(double minCPS, double maxCPS) {

        // random cps
        double difference = maxCPS - minCPS;
        difference = Math.max(1, difference);

        double random = new Random().nextInt((int) (difference * 1000)) / 1000.0;

        double result = minCPS + random;
        return 1000 / result;
    }

    @Handler
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !event.isPre())
            return;

        if (this.shouldHit() && !mc.thePlayer.isUsingItem() && !mc.thePlayer.isBlocking()) {
            mc.leftClickCounter = 0;
            mc.clickMouse();
            lastDelay = randomClickDelay(minCPS.getValue(), maxCPS.getValue());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Min: ").append(minCPS.getValue()).append(", ");
        sb.append("Max: ").append(maxCPS.getValue()).append(", ");

        if (perfectHit.getValue())
            sb.append("PH").append(", ");

        if (breakBlock.getValue())
            sb.append("BB").append(", ");

        if (multiAttack.getValue())
            sb.append("MA").append(", ");

        if (multiAttack.getValue() && attackPlayersFirst.getValue())
            sb.append("APF").append(", ");

        this.setSuffix(sb.substring(0, sb.toString().length() - 2));
    };

    public boolean shouldHit() {

        if (!this.isEnabled())
            return false;

        if (!Mouse.isButtonDown(0)) {

            if (!Mouse.isButtonDown(1) && blockHit.getValue() && mc.thePlayer.isBlocking() && mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            }

            return false;
        }

        if (breakBlock.getValue() && mc.playerController.getCurBlockDamageMP() != 0F) {
            return false;
        }

        if (mc.currentScreen != null)
            return false;

        // autoblock
        if (blockHit.getValue() && mc.objectMouseOver.entityHit != null && mc.objectMouseOver.entityHit.isEntityAlive() && mc.objectMouseOver.entityHit.hurtResistantTime > 10){
            if (mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword && blocktimer.isDelayed(100)) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                blocktimer.reset();
            } else if (!Mouse.isButtonDown(1)) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            }
        } else if (!Mouse.isButtonDown(1)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        }

        if (delayTimer.isDelayed(lastDelay)) {
            if (multiAttack.getValue()) {
                List<Entity> entities = PlayerUtils.getEntities(3.0, 0.0);

                if (entities.size() > 0) {
                    if (mAttackIdx > entities.size() - 1) {
                        mAttackIdx = 0;
                    }

                    if (attackPlayersFirst.getValue()) {
                        if (!(entities.get(mAttackIdx) instanceof EntityPlayer)) {
                            mAttackIdx++;

                            if (mAttackIdx > entities.size() - 1) {
                                mAttackIdx = 0;
                            }
                        }
                    }

//                            mc.thePlayer.addChatMessage(new ChatComponentText(Formatting.GREEN + String.valueOf(mAttackIdx) + ", " + Formatting.GOLD + entities.get(mAttackIdx).getName()));
                    mc.objectMouseOver.entityHit = entities.get(mAttackIdx);

                    mAttackIdx++;
                }
            }

            if (this.perfectHit.getValue()) {
                if(mc.pointedEntity != null){
                    Entity entity = mc.pointedEntity;

                    if(mc.thePlayer.getDistanceToEntity(entity) >= 2.6f) {
                        return entity.hurtResistantTime <= 10 && !entity.isDead;
                    }else{
                        delayTimer.reset();
                        lastDelay = randomClickDelay(minCPS.getValue(), maxCPS.getValue());
                        return true;
                    }
                }
            }

            delayTimer.reset();
            lastDelay = randomClickDelay(minCPS.getValue(), maxCPS.getValue());
            return true;
        }

        return false;
    }


}
