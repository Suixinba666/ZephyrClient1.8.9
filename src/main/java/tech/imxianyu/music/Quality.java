package tech.imxianyu.music;

import lombok.Getter;

/**
 * @author ImXianyu
 * @since 6/16/2023 10:03 AM
 */
public enum Quality {

    STANDARD("Standard"),
    HIGHER("Higher"),
    EXHIGH("ExHigh"),
    LOSSLESS("LossLess"),
    HIRES("HiRes");

    @Getter
    private final String quality;

    Quality(String quality) {
        this.quality = quality;
    }

}
