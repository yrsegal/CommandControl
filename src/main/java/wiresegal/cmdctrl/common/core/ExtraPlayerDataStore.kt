package wiresegal.cmdctrl.common.core

import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * @author WireSegal
 * Created at 6:20 PM on 12/21/16.
 */
object ExtraPlayerDataStore {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onPlayerTick(e: LivingEvent.LivingUpdateEvent) {
        if (e.entity.worldObj.isRemote || e.entityLiving !is EntityPlayer) return

        val player = e.entityLiving as EntityPlayer

        player.entityData.setBoolean("IsSneaking", player.isSneaking)
    }

    @SubscribeEvent
    fun onPlayerChat(e: ServerChatEvent) {
        e.player.entityData.setString("LastChat", e.message)
    }
}
