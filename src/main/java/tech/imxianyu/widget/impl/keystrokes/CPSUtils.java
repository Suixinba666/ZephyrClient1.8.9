package tech.imxianyu.widget.impl.keystrokes;

import tech.imxianyu.management.WidgetsManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CPSUtils {

    private static final List<Long> leftCPS = new ArrayList<>();
    private static final List<Long> rightCPS = new ArrayList<>();

    public static void addLeftCPS() {
        leftCPS.add(System.currentTimeMillis());
        WidgetsManager.keyStrokes.mouseButtons[0].circles.add(new Circle());
    }

    public static void addRightCPS() {
        rightCPS.add(System.currentTimeMillis());
        WidgetsManager.keyStrokes.mouseButtons[1].circles.add(new Circle());
    }

    public static int getLeftCPS() {
        final Iterator<Long> iterator = leftCPS.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() >= System.currentTimeMillis() - 1000L) {
                continue;
            }
            iterator.remove();
        }
        return leftCPS.size();
    }

    public static int getRightCPS() {
        final Iterator<Long> iterator = rightCPS.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() >= System.currentTimeMillis() - 1000L) {
                continue;
            }
            iterator.remove();
        }
        return rightCPS.size();
    }
}