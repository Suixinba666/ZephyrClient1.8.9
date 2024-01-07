package tech.imxianyu.rendering.notification;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager {
    private static final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public static void doRender(double posX, double posY) {
        double startY = posY;
        for (Notification notification : notifications) {
            if (notification == null)
                continue;

            notification.draw(posX, startY);

            startY += notification.getHeight() + 8;
        }
        notifications.removeIf(Notification::shouldDelete);
    }

    public static void show(String message, Notification.Type type) {
        NotificationManager.show(type.name(), message, type, 2500L);
    }
    public static void show(String title, String message, Notification.Type type, long stayTime) {
        notifications.add(new Notification(title, message, type, stayTime));
    }

    public static void show(String message, Notification.Type type, long stayTime) {
        NotificationManager.show(type.name(), message, type, stayTime);
    }
}
