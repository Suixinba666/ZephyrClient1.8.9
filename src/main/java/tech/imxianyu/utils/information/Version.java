package tech.imxianyu.utils.information;

import lombok.Getter;

import java.util.Calendar;

@Getter
public class Version {
    VersionType type;
    String buildDate;
    public Version(VersionType type, String buildDate) {
        this.type = type;
        this.buildDate = buildDate;

        if (this.type == VersionType.Dev) {
            Calendar calendar = Calendar.getInstance();

            int y = calendar.get(Calendar.YEAR);
            int m = calendar.get(Calendar.MONTH) + 1;
            int d = calendar.get(Calendar.DATE);

            String year = String.valueOf(y).substring(2, 4);
            String month = String.valueOf(m);

            if (month.length() == 1)
                month = "0" + month;

            String day = String.valueOf(d);

            if (day.length() == 1)
                day = "0" + day;

            this.buildDate = year + month + day + " (WIP)";
        }
    }

    @Override
    public String toString() {
        return this.getBuildDate() + " - " +
                this.type.toString();
    }

    public enum VersionType {
        Dev, Beta, Release
    }
}