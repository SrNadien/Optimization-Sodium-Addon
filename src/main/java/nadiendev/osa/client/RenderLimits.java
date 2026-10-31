package nadiendev.osa.client;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import nadiendev.osa.config.OsaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class RenderLimits {
    private static final int REFRESH_TICKS = 5;

    private static final double ALWAYS_VISIBLE_SQ = 8 * 8;

    private static Set<Entity> allowedStands = new ReferenceOpenHashSet<>();
    private static Set<Entity> allowedItems = new ReferenceOpenHashSet<>();
    private static Set<Entity> allowedXp = new ReferenceOpenHashSet<>();
    private static int ticks;

    private RenderLimits() {}

    public static void clear() {
        allowedStands = new ReferenceOpenHashSet<>();
        allowedItems = new ReferenceOpenHashSet<>();
        allowedXp = new ReferenceOpenHashSet<>();
    }

    public static void tick(Minecraft mc) {
        if (++ticks % REFRESH_TICKS != 0) return;
        ClientLevel level = mc.level;
        if (level == null || !OsaConfig.clientReady()) {
            clear();
            return;
        }
        Vec3 cam = mc.gameRenderer.getMainCamera().position();

        List<Entity> stands = new ArrayList<>();
        List<Entity> items = new ArrayList<>();
        List<Entity> orbs = new ArrayList<>();
        for (Entity e : level.entitiesForRendering()) {
            if (e instanceof ArmorStand s) {
                if (!(s.hasCustomName() && s.isCustomNameVisible())) stands.add(e);
            }
            else if (e instanceof ItemEntity) items.add(e);
            else if (e instanceof ExperienceOrb) orbs.add(e);
        }

        allowedStands = pick(stands, cam, OsaConfig.CLIENT.armorStands.maxRendered, OsaConfig.CLIENT.armorStands.renderDistance);
        allowedItems = pick(items, cam, OsaConfig.CLIENT.items.maxRendered, OsaConfig.CLIENT.items.renderDistance);
        allowedXp = pick(orbs, cam, OsaConfig.CLIENT.xp.maxRendered, OsaConfig.CLIENT.xp.renderDistance);
    }

    private static Set<Entity> pick(List<Entity> list, Vec3 cam, int max, int distance) {
        double maxSq = (double) distance * distance;
        list.removeIf(e -> e.distanceToSqr(cam) > maxSq);
        if (max > 0 && list.size() > max) {
            list.sort(Comparator.comparingDouble(e -> e.distanceToSqr(cam)));
            list = list.subList(0, max);
        }
        return new ReferenceOpenHashSet<>(list);
    }

    public static boolean shouldRender(Entity e, double camX, double camY, double camZ) {
        if (!OsaConfig.clientReady()) return true;
        Set<Entity> allowed;
        if (e instanceof ArmorStand stand) {
            if (stand.hasCustomName() && stand.isCustomNameVisible()) return true;
            if (OsaConfig.CLIENT.armorStands.hideInvisibleEmpty && stand.isInvisible() && isEmpty(stand)) return false;
            allowed = allowedStands;
        } else if (e instanceof ItemEntity) {
            allowed = allowedItems;
        } else if (e instanceof ExperienceOrb) {
            allowed = allowedXp;
        } else {
            return true;
        }
        if (allowed.contains(e)) return true;
        return e.distanceToSqr(camX, camY, camZ) <= ALWAYS_VISIBLE_SQ;
    }

    private static boolean isEmpty(ArmorStand stand) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!stand.getItemBySlot(slot).isEmpty()) return false;
        }
        return true;
    }
}
