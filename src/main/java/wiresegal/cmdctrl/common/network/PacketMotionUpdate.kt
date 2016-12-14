package wiresegal.cmdctrl.common.network

import com.teamwizardry.librarianlib.common.network.PacketBase
import com.teamwizardry.librarianlib.common.util.saving.Save
import com.teamwizardry.librarianlib.common.util.vec
import net.minecraft.client.Minecraft
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

/**
 * @author WireSegal
 * Created at 5:43 PM on 12/3/16.
 */
class PacketMotionUpdate(@Save var motVec: Vec3d? = null) : PacketBase() {

    constructor(x: Number, y: Number, z: Number) : this(vec(x, y, z))

    override fun handle(ctx: MessageContext) {
        if (ctx.side.isClient) {
            val player = Minecraft.getMinecraft().thePlayer
            val mot = motVec ?: return
            player.motionX = mot.xCoord
            player.motionY = mot.yCoord
            player.motionZ = mot.zCoord
        }
    }
}
