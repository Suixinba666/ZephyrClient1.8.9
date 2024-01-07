package tech.imxianyu.utils.other;

import com.google.common.primitives.Primitives;

public class NumberUtil {
    public static <T extends Number, V extends Number> T cast(Class<T> numberClass, final V value) {
        numberClass = Primitives.wrap(numberClass);
        Object casted;
        if (numberClass == Byte.class) {
            casted = value.byteValue();
        } else if (numberClass == Short.class) {
            casted = value.shortValue();
        } else if (numberClass == Integer.class) {
            casted = value.intValue();
        } else if (numberClass == Long.class) {
            casted = value.longValue();
        } else if (numberClass == Float.class) {
            casted = value.floatValue();
        } else {
            if (numberClass != Double.class) {
                throw new ClassCastException(String.format("%s cannot be casted to %s", value.getClass(), numberClass));
            }
            casted = value.doubleValue();
        }
        return (T) casted;
    }

    public static <T extends Number> T parse(final String input, final Class<T> numberType) throws NumberFormatException {
        return cast(numberType, Double.parseDouble(input));
    }
}
