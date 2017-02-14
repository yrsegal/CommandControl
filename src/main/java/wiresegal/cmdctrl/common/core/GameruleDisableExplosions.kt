package wiresegal.cmdctrl.common.core

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.world.GameRules
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.ExplosionEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


/**
 * @author WireSegal
 * Created at 10:45 AM on 2/14/17.
 */
object GameruleDisableExplosions {
    val GAME_RULE_BLOCK = "explosionBlockDamage"
    val GAME_RULE_LIVING = "explosionLivingDamage"
    val GAME_RULE_NONLIVING = "explosionNonlivingDamage"

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun GameRules.addRule(name: String, default: String, type: GameRules.ValueType) {
        val value = if (hasRule(name)) getString(name) else default
        addGameRule(name, value, type)
    }

    fun World.getRuleValue(name: String): String = gameRules.getString(name)

    @SubscribeEvent
    fun worldLoad(event: WorldEvent.Load) {
        val rules = event.world.gameRules
        rules.addRule(GAME_RULE_BLOCK, "true", GameRules.ValueType.BOOLEAN_VALUE)
        rules.addRule(GAME_RULE_LIVING, "true", GameRules.ValueType.BOOLEAN_VALUE)
        rules.addRule(GAME_RULE_NONLIVING, "true", GameRules.ValueType.BOOLEAN_VALUE)
    }

    @SubscribeEvent
    fun explosions(e: ExplosionEvent.Detonate) {
        if (e.world.getRuleValue(GAME_RULE_BLOCK) == "false")
            e.affectedBlocks.clear()
        if (e.world.getRuleValue(GAME_RULE_LIVING) == "false")
            e.affectedEntities.removeAll { it is EntityLivingBase && it !is EntityArmorStand }
        if (e.world.getRuleValue(GAME_RULE_NONLIVING) == "false")
            e.affectedEntities.removeAll { it !is EntityLivingBase || it is EntityArmorStand }
    }
}
