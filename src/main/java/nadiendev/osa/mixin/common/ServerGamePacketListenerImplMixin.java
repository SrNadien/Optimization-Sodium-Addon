package nadiendev.osa.mixin.common;

import nadiendev.osa.config.OsaConfig;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @ModifyConstant(method = "handlePlayerPositionChange", constant = @Constant(floatValue = 100.0F), require = 0)
    private float osa$playerSpeedLimit(float original) {
        return OsaConfig.ignoreMoveSpeedLimit() ? Float.MAX_VALUE : original;
    }

    @ModifyConstant(method = "handlePlayerPositionChange", constant = @Constant(floatValue = 300.0F), require = 0)
    private float osa$elytraSpeedLimit(float original) {
        return OsaConfig.ignoreMoveSpeedLimit() ? Float.MAX_VALUE : original;
    }

    @ModifyConstant(method = "handleMoveVehicle", constant = @Constant(doubleValue = 100.0), require = 0)
    private double osa$vehicleSpeedLimit(double original) {
        return OsaConfig.ignoreMoveSpeedLimit() ? Double.MAX_VALUE : original;
    }
}
