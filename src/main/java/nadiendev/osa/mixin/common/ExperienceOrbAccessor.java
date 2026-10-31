package nadiendev.osa.mixin.common;

import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ExperienceOrb.class)
public interface ExperienceOrbAccessor {
    @Invoker("setValue")
    void osa$setValue(int value);

    @Accessor("count")
    int osa$getCount();

    @Accessor("count")
    void osa$setCount(int count);

    @Accessor("age")
    void osa$setAge(int age);
}
