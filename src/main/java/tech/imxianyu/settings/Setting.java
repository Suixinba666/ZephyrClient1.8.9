package tech.imxianyu.settings;

import lombok.Getter;
import lombok.Setter;
import tech.imxianyu.module.Module;
import tech.imxianyu.widget.Widget;

import java.util.function.Supplier;

public abstract class Setting<T> {
    @Getter
    private final String name;
    @Getter
    private final T defaultValue;
    @Getter
    @Setter
    protected T value;
    @Getter
    @Setter
    private Supplier<Boolean> shouldRender = () -> true;

    @Getter
    @Setter
    private String description = "";


    public Setting(String name, T value) {
        this.name = name;
        this.value = value;
        this.defaultValue = value;

        this.description = this.name;
    }

    public Setting(String name, T value, Supplier<Boolean> shouldRender) {
        this(name, value);

        this.shouldRender = shouldRender;
    }

    public abstract void loadValue(String value);

    public boolean shouldRender() {
        return this.shouldRender.get();
    }

    public String getValueForConfig() {
        return this.getValue().toString();
    }

    public void reset() {
        this.setValue(this.getDefaultValue());
    }

    public void onInit(Module module) {

    }

    public void onInit(Widget module) {

    }

    public void onInit() {

    }
}
