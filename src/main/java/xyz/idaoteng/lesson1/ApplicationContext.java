package xyz.idaoteng.lesson1;

import java.util.HashMap;

public class ApplicationContext {
    private static final HashMap<String, Object> context = new HashMap<>();

    public static void register(String name, Object obj) {
        if (context.containsKey(name)) {
            throw new RuntimeException("上下文中已含有相同的名字的组件：" + name);
        }
        context.put(name, obj);
    }

    public static Object get(String name) {
        return context.get(name);
    }
}
