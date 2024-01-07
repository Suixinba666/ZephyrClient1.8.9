package tech.imxianyu.module.impl.other;

import tech.imxianyu.eventapi.Handler;
import tech.imxianyu.events.player.AttackEvent;
import tech.imxianyu.management.FriendManager;
import tech.imxianyu.module.Module;
import tech.imxianyu.settings.BooleanSetting;

public class Friends extends Module {

    public BooleanSetting notAttack = new BooleanSetting("NoAttackingFriends", false);
    @Handler
    public void onAttack(AttackEvent event) {
        if (notAttack.getValue() && FriendManager.isFriend(event.getAttackedEntity())) {
            event.setCancelled();
        }
    };

    public Friends() {
        super("Friends", Category.OTHER);
    }
}
