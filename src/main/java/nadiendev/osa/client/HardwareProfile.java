package nadiendev.osa.client;

import com.mojang.blaze3d.systems.DeviceInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sun.management.OperatingSystemMXBean;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;

import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record HardwareProfile(int cores, double ramGb, String gpu, String glVersion, int vramMb,
                              int width, int height, int refreshRate) {
    private static final int NVX_TOTAL_AVAILABLE_KB = 0x9048;
    private static final Pattern NVIDIA = Pattern.compile("geforce\\s+(gts|gtx|gt|rtx)?\\s*(\\d{3,4})");
    private static final Pattern RADEON_RX = Pattern.compile("radeon\\s+(?:\\(tm\\)\\s+)?rx\\s*(\\d{3,4})");
    private static final int LEGACY = -1;
    private static final double LOW_MEMORY_LIMIT_GB = 10.0;

    public static HardwareProfile detect() {
        int cores = Runtime.getRuntime().availableProcessors();

        double ramGb = 0;
        if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean os) {
            ramGb = os.getTotalMemorySize() / 1_000_000_000.0;
        }

        String gpu = "";
        String glVersion = "";
        int vramMb = 0;
        try {
            DeviceInfo device = RenderSystem.getDevice().getDeviceInfo();
            gpu = String.valueOf(device.name());
            glVersion = device.backendName() + " " + device.driverInfo();
            if (GL.getCapabilities().GL_NVX_gpu_memory_info) {
                vramMb = GL11C.glGetInteger(NVX_TOTAL_AVAILABLE_KB) / 1024;
            }
        } catch (Throwable ignored) {
        }

        int width = 0;
        int height = 0;
        int refresh = 60;
        try {
            Minecraft minecraft = Minecraft.getInstance();
            width = minecraft.getWindow().getScreenWidth();
            height = minecraft.getWindow().getScreenHeight();
            GLFWVidMode mode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            if (mode != null) refresh = mode.refreshRate();
        } catch (Throwable ignored) {
        }

        return new HardwareProfile(cores, ramGb, gpu, glVersion, vramMb, width, height, refresh);
    }

    public boolean detectionFailed() {
        return cores <= 0 || ramGb <= 0 || gpu.isEmpty();
    }

    public TuneProfile tier() {
        if (detectionFailed() || ramGb < LOW_MEMORY_LIMIT_GB) return TuneProfile.LOW;
        int gpuPoints = gpuPoints();
        if (gpuPoints == LEGACY) return TuneProfile.LOW;
        int score = cpuPoints() + ramPoints() + gpuPoints;
        if (score >= 6) return TuneProfile.HIGH;
        if (score >= 3) return TuneProfile.MEDIUM;
        return TuneProfile.LOW;
    }

    public String describe() {
        StringBuilder text = new StringBuilder();
        text.append("CPU ").append(cores).append(" nucleos, RAM ").append(String.format("%.1f", ramGb)).append(" GB, GPU ")
                .append(gpu.isEmpty() ? "desconocida" : gpu);
        if (vramMb > 0) text.append(" (").append(vramMb).append(" MB)");
        text.append(", pantalla ").append(width).append("x").append(height).append(" a ").append(refreshRate).append(" Hz");
        return text.toString();
    }

    private int cpuPoints() {
        if (cores >= 12) return 3;
        if (cores >= 8) return 2;
        if (cores >= 6) return 1;
        return 0;
    }

    private int ramPoints() {
        if (ramGb >= 31) return 3;
        if (ramGb >= 15) return 2;
        if (ramGb >= 11) return 1;
        return 0;
    }

    private int gpuPoints() {
        String name = gpu.toLowerCase(Locale.ROOT);
        if (name.isEmpty()) return 2;
        if (name.contains("llvmpipe") || name.contains("swrast") || name.contains("software") || name.contains("basic render")) {
            return LEGACY;
        }

        Matcher nvidia = NVIDIA.matcher(name);
        if (nvidia.find()) {
            String series = nvidia.group(1) == null ? "" : nvidia.group(1);
            int model = Integer.parseInt(nvidia.group(2));
            if (series.equals("rtx")) return 3;
            if (series.equals("gt") || series.equals("gts")) return model < 1000 ? LEGACY : 0;
            if (series.isEmpty() && model >= 8000) return LEGACY;
            if (model < 700) return 0;
            if (model < 1000) return 2;
            return 3;
        }

        if (name.contains("radeon")) {
            if (name.contains("radeon hd") || name.contains("radeon(tm) hd")) return LEGACY;
            Matcher rx = RADEON_RX.matcher(name);
            if (rx.find()) return Integer.parseInt(rx.group(1)) >= 5000 ? 3 : 2;
            if (name.contains("vega")) return 1;
            return 1;
        }

        if (name.contains("intel")) {
            if (name.contains("arc")) return 3;
            if (name.contains("iris")) return 1;
            return 0;
        }

        if (name.contains("apple")) return 3;
        return 2;
    }
}
