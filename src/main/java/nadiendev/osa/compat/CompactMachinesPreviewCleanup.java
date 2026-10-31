package nadiendev.osa.compat;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import net.minecraft.client.renderer.SectionBufferBuilderPack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * El preview 3D de una sala de Compact Machines arma una copia renderizada de la sala
 * (buffers de vertices en la GPU y buffers nativos fuera del heap de Java) y nunca la libera.
 * Cada vez que se abre un preview se pierden cientos de MB.
 *
 * Aca, al cerrar o reemplazar el preview, se recorre el renderer de la pantalla y se cierra todo
 * recurso grafico que se encuentre: VertexBuffer, SectionBufferBuilderPack, RenderTarget y
 * cualquier otro objeto cerrable que cuelgue de esos mapas. Todo por reflexion, para no depender
 * de la version exacta del mod: si algo no existe, simplemente no se hace nada.
 */
public final class CompactMachinesPreviewCleanup {
    /** Profundidad maxima al recorrer mapas y colecciones (evita recorrer medio juego por error). */
    private static final int MAX_DEPTH = 4;

    private CompactMachinesPreviewCleanup() {}

    public static void disposeScreenRenderer(Object screen) {
        if (screen == null) return;
        if (OsaConfig.clientReady() && !OsaConfig.CLIENT.compactMachinesPreviewFix.get()) return;

        Object renderer = Reflect.get(screen, "renderer");
        if (renderer == null) return;
        Object scene = Reflect.get(renderer, "bakedLevel");
        Object pipeline = Reflect.get(renderer, "state");
        if (scene == null && pipeline == null) return;

        Runnable job = () -> {
            Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            try {
                if (scene != null) releaseScene(scene, seen);
            } catch (Throwable t) {
                OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo liberar la escena del preview de Compact Machines", t);
            }
            try {
                if (pipeline != null) releasePipeline(pipeline, seen);
            } catch (Throwable t) {
                OptimizerSodiumAddon.LOG.warn("[OSA] No se pudo liberar el estado de render del preview de Compact Machines", t);
            }
        };

        // Los recursos de OpenGL solo se pueden tocar desde el hilo de render.
        if (RenderSystem.isOnRenderThread()) job.run();
        else RenderSystem.recordRenderCall(job::run);
    }

    /** La escena guarda sus secciones en un mapa; cada seccion tiene buffers de bloques y fluidos. */
    private static void releaseScene(Object scene, Set<Object> seen) {
        Object sections = Reflect.call(scene, "sections");
        if (!(sections instanceof Map<?, ?> map)) return;
        for (Object section : map.values()) {
            if (section == null) continue;
            for (String part : new String[]{"blockBuffers", "fluidBuffers", "blockBuilders", "fluidBuilders"}) {
                release(Reflect.call(section, part), seen, 0);
            }
        }
    }

    /** El estado del pipeline guarda framebuffers y cadenas de translucidez en un mapa de propiedades. */
    private static void releasePipeline(Object pipeline, Set<Object> seen) {
        Object props = Reflect.get(pipeline, "properties");
        if (props instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                if (value == null) continue;
                // Las cadenas de translucidez tienen un framebuffer intermedio que su close() no libera.
                release(Reflect.get(value, "intermediaryCopyTarget"), seen, 0);
                Reflect.call(value, "dispose");
                release(value, seen, 0);
            }
        }
        Reflect.call(pipeline, "clear");
    }

    private static void release(Object o, Set<Object> seen, int depth) {
        if (o == null || depth > MAX_DEPTH || !seen.add(o)) return;

        if (o instanceof VertexBuffer vb) {
            quietly(vb::close);
        } else if (o instanceof SectionBufferBuilderPack pack) {
            quietly(pack::close);
        } else if (o instanceof RenderTarget target) {
            quietly(target::destroyBuffers);
        } else if (o instanceof Map<?, ?> map) {
            for (Object v : map.values()) release(v, seen, depth + 1);
        } else if (o instanceof Collection<?> col) {
            for (Object v : col) release(v, seen, depth + 1);
        } else if (o instanceof AutoCloseable closeable) {
            quietly(closeable::close);
        }
    }

    private interface Action {
        void run() throws Exception;
    }

    private static void quietly(Action a) {
        try {
            a.run();
        } catch (Throwable ignored) {
            // ya estaba cerrado o no aplica
        }
    }

    /** Acceso por nombre a campos y metodos sin argumentos, buscando tambien en las superclases. */
    private static final class Reflect {
        private Reflect() {}

        static Object get(Object target, String fieldName) {
            for (Class<?> c = target.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                try {
                    Field f = c.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    return f.get(target);
                } catch (NoSuchFieldException ignored) {
                    // seguir con la superclase
                } catch (Throwable t) {
                    return null;
                }
            }
            return null;
        }

        static Object call(Object target, String methodName) {
            for (Class<?> c = target.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                try {
                    Method m = c.getDeclaredMethod(methodName);
                    m.setAccessible(true);
                    return m.invoke(target);
                } catch (NoSuchMethodException ignored) {
                    // seguir con la superclase
                } catch (Throwable t) {
                    return null;
                }
            }
            return null;
        }
    }
}
