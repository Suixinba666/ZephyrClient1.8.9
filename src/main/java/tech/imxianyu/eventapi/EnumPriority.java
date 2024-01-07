package tech.imxianyu.eventapi;


import lombok.Getter;

public enum EnumPriority {
    HIGHEST(10),
    ULTIMATEHIGH(9),
    MOREHIGHER(8),
    HIGH(7),
    HIGHER(6),
    NORMAL(5),
    LOWER(4),
    LOW(3),
    MORELOWER(2),
    LOWEST(1);

    @Getter
    private final int level;

    EnumPriority(int level) {
        this.level = level;
    }
}
