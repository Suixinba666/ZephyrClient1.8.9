package tech.imxianyu.eventapi;

import lombok.Getter;
import lombok.SneakyThrows;
import scala.Int;
import tech.imxianyu.events.player.UpdateEvent;
import tech.imxianyu.interfaces.InstanceAccess;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * the event bus.
 * @author ImXianyu
 * @since 3/25/2023 11:04 PM
 */
public class EventBus implements InstanceAccess {
    // 注释懒得改英文了 我好久之前用中文写的 凑合看吧
    //方法Map
    private static final Map<Type, List<Target>> registrationMap = new HashMap<>();

    /**
     * 注册该对象中所有被@Handler修饰的方法
     * @param obj 类实例
     */
    @SneakyThrows
    public static void register(Object obj) {
        //遍历所有的方法
        for (Method method : obj.getClass().getDeclaredMethods()) {
            //如果被@Handler注解修饰
            if (method.isAnnotationPresent(Handler.class)) {
//                boolean accessible = method.isAccessible();
                //设置访问权限
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                //优先级
                int pr;
                if (method.isAnnotationPresent(Priority.class)) {
                    Priority priority = method.getAnnotation(Priority.class);
                    pr = priority.priority().getLevel();
                } else {
                    //如果没有@Priority注解的话默认就是Normal优先级
                    pr = EnumPriority.NORMAL.getLevel();
                }

//                method.setAccessible(accessible);
                //获取方法的Event类型
                Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];

                Target target = new Target(method, eventClass, obj, pr);

                if (registrationMap.containsKey(eventClass)) {
                    if (!registrationMap.get(eventClass).contains(target)) {
                        registrationMap.get(eventClass).add(target);
                    }
                } else {
                    registrationMap.put(eventClass, new CopyOnWriteArrayList<Target>() {
                        {
                            add(target);
                        }
                    });
                }

                registrationMap.get(eventClass).sort(Comparator.comparingInt(o -> ((Target) o).priority).reversed());
            }

        }

    }

    /**
     * 取消订阅指定类实例中的带有@Handler注解的方法.
     *
     * @param source 类实例
     */
    public static void unregister(Object source) {
        for (List<Target> dataList : registrationMap.values()) {
            dataList.removeIf(data -> data.getSource().equals(source));
        }

        cleanMap(true);
    }

    /**
     * 清理Map中的空Entry
     */
    public static void cleanMap(boolean onlyEmptyEntries) {
        Iterator<Map.Entry<Type, List<Target>>> mapIterator = registrationMap.entrySet().iterator();

        while (mapIterator.hasNext()) {
            if (!onlyEmptyEntries || mapIterator.next().getValue().isEmpty()) {
                mapIterator.remove();
            }
        }
    }

    /**
     * 是否有方法可以接受这个event
     * @param eventClass event类型
     * @return 是否有方法可以接受这个event
     */
    @SneakyThrows
    public static boolean canReceive(Class<? extends Event> eventClass) {
        List<Target> methodList = registrationMap.get(eventClass);

        if (methodList != null) {
            for (Target target : methodList) {
                if (target.getType() == eventClass) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 调用所有注册的event
     * @param event event类型
     */
    @SneakyThrows
    public static <T extends Event> T call(T event) {
        List<Target> methodList = registrationMap.get(event.getClass());

        if (methodList != null) {
            for (Target target : methodList) {
                if (target.getType() == event.getClass()) {
                    event.setResponded(true);

                    if (event.isParallel()) {
                        threadPool.execute(() -> {
                            try {
                                target.getTarget().invoke(target.getSource(), event);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } else {

                        try {
                            target.getTarget().invoke(target.getSource(), event);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (event instanceof EventCancellable && ((EventCancellable) event).isCancelled()) {
                            break;
                        }

                    }
                }
            }

        }

        return event;
    }

    private static class Target {

        @Getter
        private final Method target;

        @Getter
        private final Type type;

        @Getter
        private final Object source;

        @Getter
        private final int priority;

        public Target(Method target, Type type, Object source, int priority) {
            this.target = target;
            this.type = type;
            this.source = source;
            this.priority = priority;
        }

    }
}
