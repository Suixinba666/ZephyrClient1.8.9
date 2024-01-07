package tech.imxianyu.settings;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModeSetting<T extends Enum<T>> extends Setting<T> {
    private final T[] constants;
    public boolean expanded = false;
    public double expandedHeight = 0;

    public ModeSetting(String name, T value) {
        super(name, value);
        this.constants = extractConstantsFromEnumValue(value);
    }

    public ModeSetting(String name, T value, Supplier<Boolean> show) {
        super(name, value, show);
        this.constants = extractConstantsFromEnumValue(value);
    }

    public T[] extractConstantsFromEnumValue(T value) {
        return value.getDeclaringClass().getEnumConstants();
    }

    public String getCurMode() {
        return this.getValue().toString();
    }

    public T[] getConstants() {
        return this.constants;
    }

    public void setMode(String mode) {
        for (T constant : this.getConstants()) {
            if (constant.toString().equalsIgnoreCase(mode)) {
                this.onModeChanged(this.getValue(), constant);
                this.setValue(constant);
            }
        }
    }

    public void onModeChanged(T before, T now) {

    }

    @Override
    public void setValue(@NonNull T value) {
        this.onModeChanged(this.getValue(), value);
        super.setValue(value);
    }

    @Override
    public void loadValue(String string) {
        this.setMode(string);
    }
}
