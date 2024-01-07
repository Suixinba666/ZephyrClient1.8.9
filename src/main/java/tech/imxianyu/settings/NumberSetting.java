package tech.imxianyu.settings;

import lombok.Getter;
import tech.imxianyu.utils.other.NumberUtil;

import java.text.DecimalFormat;
import java.util.function.Supplier;

public class NumberSetting<T extends Number> extends Setting<T> {

    @Getter
    public final T minimum, maximum, increment;
    public double nowWidth = 0;
    public DecimalFormat df = new DecimalFormat("#.##");
    private T lastValue;

    public NumberSetting(String name, T value, T minimum, T maximum, T increment) {
        super(name, value);
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
        this.lastValue = value;
    }

    public NumberSetting(String name, T value, T minimum, T maximum, T increment, Supplier<Boolean> show) {
        super(name, value, show);
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
        this.lastValue = value;
    }

    public float getFloatValue() {
        return this.getValue().floatValue();
    }

    public int getIntValue() {
        return this.getValue().intValue();
    }

    public long getLongValue() {
        return this.getValue().longValue();
    }

    public T getValue() {
        return this.value;
    }

    @Override
    public void setValue(T value) {
        double precision = 1 / increment.doubleValue();
        this.value = NumberUtil.cast((Class<T>) this.value.getClass(), Math.round(value.doubleValue() * precision) / precision);
        if (!this.lastValue.equals(this.value)) {
            this.onValueChanged(lastValue, value);
        }
        this.lastValue = this.value;
    }

    public void onValueChanged(T last, T now) {
    }

    public String getStringForRender() {
        return df.format(this.getValue());
    }

    @Override
    public void loadValue(String value) {
        T parse = NumberUtil.parse(value, (Class<T>) this.value.getClass());
        double precision = 1 / increment.doubleValue();
        this.value = NumberUtil.cast((Class<T>) this.value.getClass(), Math.round(parse.doubleValue() * precision) / precision);
        this.lastValue = this.value;
    }

}
