package nadiendev.osa.server;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import nadiendev.osa.command.OsaCommands;
import nadiendev.osa.mixin.common.ExperienceOrbAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = OptimizerSodiumAddon.MODID)
public final class ServerEvents {
    private static final int VANILLA_ITEM_LIFESPAN = 6000;
    private static final boolean CLUMPS_LOADED = ModList.get().isLoaded("clumps");

    private ServerEvents() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        OsaCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        LagAlerts.reset();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        LagAlerts.tick(event.getServer());
    }

    // ------------------------------------------------------------ items / xp

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide() || !OsaConfig.serverReady()) return;
        Entity entity = event.getEntity();

        if (entity instanceof ItemEntity item) {
            int life = OsaConfig.SERVER.itemLifespanTicks.get();
            if (!event.loadedFromDisk() && item.lifespan == VANILLA_ITEM_LIFESPAN && life < VANILLA_ITEM_LIFESPAN) {
                item.lifespan = life;
            }
            return;
        }

        if (entity instanceof ExperienceOrb orb && !event.loadedFromDisk() && OsaConfig.SERVER.xpMerge.get()
                && !CLUMPS_LOADED) {
            double r = OsaConfig.SERVER.xpMergeRadius.get();
            List<ExperienceOrb> near = level.getEntitiesOfClass(ExperienceOrb.class,
                    orb.getBoundingBox().inflate(r), o -> o != orb && o.isAlive());
            if (near.isEmpty()) return;

            ExperienceOrb target = near.get(0);
            ExperienceOrbAccessor t = (ExperienceOrbAccessor) target;
            ExperienceOrbAccessor n = (ExperienceOrbAccessor) orb;
            long total = (long) t.osa$getValue() * Math.max(1, t.osa$getCount())
                    + (long) n.osa$getValue() * Math.max(1, n.osa$getCount());
            t.osa$setValue((int) Math.min(Integer.MAX_VALUE, total));
            t.osa$setCount(1);
            t.osa$setAge(0);
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------ IA lejana

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide() || !OsaConfig.serverReady()) return;

        boolean isMob = entity instanceof Mob;
        boolean isStand = entity instanceof ArmorStand;
        if (!isMob && !isStand) return;
        if (isMob && !OsaConfig.SERVER.aiThrottle.get()) return;
        if (isStand && !OsaConfig.SERVER.armorStandThrottle.get()) return;

        int interval = OsaConfig.SERVER.aiTickInterval.get();
        // Al azar 1 de cada 'interval' ticks se procesa igual. Si fuera un patron fijo (cada 4 exactos),
        // la logica vanilla que usa "tickCount % 2" quedaria siempre del mismo lado y algunos mobs
        // nunca elegirian objetivo nuevo. No se usa el RNG del mob para no alterar su secuencia.
        if (ThreadLocalRandom.current().nextInt(interval) == 0) return;

        if (isMob && !canThrottle((Mob) entity)) return;
        if (entity.isPassenger() || entity.isVehicle()) return;

        double range = OsaConfig.SERVER.aiActivationRange.get();
        if (level.hasNearbyAlivePlayer(entity.getX(), entity.getY(), entity.getZ(), range)) return;

        event.setCanceled(true);
    }

    private static boolean canThrottle(Mob mob) {
        if (mob.hasCustomName() || mob.isLeashed()) return false;
        if (mob.getType().is(Tags.EntityTypes.BOSSES)) return false;
        if (mob instanceof OwnableEntity own && own.getOwnerUUID() != null) return false;
        return !OsaConfig.isAiWhitelisted(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()));
    }
}
