package tech.imxianyu.rendering.multithreading;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;
import org.lwjglx.opengl.Display;

public class AsyncContextUtils {

    public static long createSubWindow() {
        long subWindow = GLFW.glfwCreateWindow(1, 1, "SubWindow", MemoryUtil.NULL, Display.getWindow());
        return subWindow;
    }

}
