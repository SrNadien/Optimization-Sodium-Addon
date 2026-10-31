package nadiendev.osa.client;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_DONT_CARE;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwGetMonitorPos;
import static org.lwjgl.glfw.GLFW.glfwGetMonitors;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowAttrib;
import static org.lwjgl.glfw.GLFW.glfwGetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwSetWindowAttrib;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMonitor;

public final class BorderlessWindow {
    private static int savedX = 100;
    private static int savedY = 100;
    private static int savedWidth = 1280;
    private static int savedHeight = 720;

    private BorderlessWindow() {}

    public static boolean toggle(long handle) {
        if (glfwGetWindowMonitor(handle) != 0L) return false;
        if (glfwGetWindowAttrib(handle, GLFW_DECORATED) == GLFW_FALSE) {
            leave(handle);
            return true;
        }
        return enter(handle);
    }

    private static boolean enter(long handle) {
        long monitor = findMonitor(handle);
        GLFWVidMode mode = glfwGetVideoMode(monitor);
        if (mode == null) return false;

        int[] x = new int[1];
        int[] y = new int[1];
        int[] w = new int[1];
        int[] h = new int[1];
        glfwGetWindowPos(handle, x, y);
        glfwGetWindowSize(handle, w, h);
        savedX = x[0];
        savedY = y[0];
        savedWidth = w[0];
        savedHeight = h[0];

        int[] mx = new int[1];
        int[] my = new int[1];
        glfwGetMonitorPos(monitor, mx, my);
        glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_FALSE);
        glfwSetWindowMonitor(handle, 0L, mx[0], my[0], mode.width(), mode.height() + 1, GLFW_DONT_CARE);
        return true;
    }

    private static void leave(long handle) {
        glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_TRUE);
        glfwSetWindowMonitor(handle, 0L, savedX, savedY, savedWidth, savedHeight, GLFW_DONT_CARE);
    }

    private static long findMonitor(long handle) {
        int[] wx = new int[1];
        int[] wy = new int[1];
        int[] ww = new int[1];
        int[] wh = new int[1];
        glfwGetWindowPos(handle, wx, wy);
        glfwGetWindowSize(handle, ww, wh);
        int cx = wx[0] + ww[0] / 2;
        int cy = wy[0] + wh[0] / 2;

        PointerBuffer monitors = glfwGetMonitors();
        if (monitors != null) {
            for (int i = 0; i < monitors.limit(); i++) {
                long monitor = monitors.get(i);
                GLFWVidMode mode = glfwGetVideoMode(monitor);
                if (mode == null) continue;
                int[] mx = new int[1];
                int[] my = new int[1];
                glfwGetMonitorPos(monitor, mx, my);
                if (cx >= mx[0] && cx < mx[0] + mode.width() && cy >= my[0] && cy < my[0] + mode.height()) {
                    return monitor;
                }
            }
        }
        return glfwGetPrimaryMonitor();
    }
}
