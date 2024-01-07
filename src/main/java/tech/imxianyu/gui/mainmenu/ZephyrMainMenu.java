package tech.imxianyu.gui.mainmenu;

import tech.imxianyu.gui.ZephyrScreen;
import tech.imxianyu.gui.mainmenu.impl.NormalMainMenu;
import tech.imxianyu.rendering.transition.TransitionAnimation;

import java.util.Calendar;
import java.util.Date;

/**
 * @author ImXianyu
 * @since 4/15/2023 7:50 PM
 */
public class ZephyrMainMenu extends ZephyrScreen {

    public static ZephyrMainMenu getMainMenu() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);

        if (month == 4 && day == 1) {
            // April Fool's Day?
        }

        return NormalMainMenu.getInstance();
    }

}
