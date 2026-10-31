package nadiendev.osa.server;

import nadiendev.osa.config.OsaConfig;
import nadiendev.osa.loader.Platform;
import nadiendev.osa.mixin.common.ExperienceOrbAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class EntityRules {
    public static final TagKey<EntityType<?>> BOSSES =
            TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("c", "bosses"));

    private static final boolean CLUMPS_LOADED = Platform.isModLoaded("clumps");

    private EntityRules() {}

    public static boolean shouldCancelJoin(Level level, Entity entity) {
        if (level.isClientSide() || !OsaConfig.serverReady()) return false;
        if (!(entity instanceof ExperienceOrb orb) || !OsaConfig.SERVER.xp.merge || CLUMPS_LOADED) return false;

        double r = OsaConfig.SERVER.xp.mergeRadius;
        List<ExperienceOrb> near = level.getEntitiesOfClass(ExperienceOrb.class,
                orb.getBoundingBox().inflate(r), o -> o != orb && o.isAlive());
        if (near.isEmpty()) return false;

        ExperienceOrb target = near.get(0);
        ExperienceOrbAccessor t = (ExperienceOrbAccessor) target;
        ExperienceOrbAccessor n = (ExperienceOrbAccessor) orb;
        long total = (long) t.osa$getValue() * Math.max(1, t.osa$getCount())
                + (long) n.osa$getValue() * Math.max(1, n.osa$getCount());
        t.osa$setValue((int) Math.min(Integer.MAX_VALUE, total));
        t.osa$setCount(1);
        t.osa$setAge(0);
        return true;
    }

    public static boolean shouldSkipTick(Entity entity) {
        Level level = entity.level();
        if (level.isClientSide() || !OsaConfig.serverReady()) return false;

        boolean isMob = entity instanceof Mob;
        boolean isStand = entity instanceof ArmorStand;
        if (!isMob && !isStand) return false;
        if (isMob && !OsaConfig.SERVER.ai.throttleFarMobs) return false;
        if (isStand && !OsaConfig.SERVER.armorStands.throttleWhenFar) return false;

        int interval = OsaConfig.SERVER.ai.tickInterval;
        if (ThreadLocalRandom.current().nextInt(interval) == 0) return false;

        if (isMob && !canThrottle((Mob) entity)) return false;
        if (entity.isPassenger() || entity.isVehicle()) return false;

        double range = OsaConfig.SERVER.ai.activationRange;
        return !level.hasNearbyAlivePlayer(entity.getX(), entity.getY(), entity.getZ(), range);
    }

    public static int itemLifespan(int vanilla) {
        if (!OsaConfig.serverReady()) return vanilla;
        int life = OsaConfig.SERVER.items.lifespanTicks;
        return life < vanilla ? life : vanilla;
    }

    private static boolean canThrottle(Mob mob) {
        if (mob.hasCustomName() || mob.isLeashed()) return false;
        if (mob.getType().is(BOSSES)) return false;
        if (mob instanceof OwnableEntity own && own.getOwnerUUID() != null) return false;
        return !OsaConfig.isAiWhitelisted(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()));
    }
}
