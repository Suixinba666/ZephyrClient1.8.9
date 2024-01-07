package tech.imxianyu.events;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;
import tech.imxianyu.eventapi.EventCancellable;

import java.util.List;

public class ChatComponentEvent extends EventCancellable {

    private final List<ChatLine> chatLines;
    private IChatComponent component;

    public ChatComponentEvent(IChatComponent p_i1496_1_, List<ChatLine> p_i1496_2_) {
        this.component = p_i1496_1_;
        this.chatLines = p_i1496_2_;
    }

    public IChatComponent getComponent() {
        return this.component;
    }

    public void setComponent(IChatComponent p_setComponent_1_) {
        this.component = p_setComponent_1_;
    }

    public List<ChatLine> getChatLines() {
        return this.chatLines;
    }
}
