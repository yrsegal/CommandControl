package wiresegal.cmdctrl.common.core

import com.teamwizardry.librarianlib.LibrarianLib
import net.minecraft.command.CommandException
import net.minecraft.command.WrongUsageException

/**
 * @author WireSegal
 * Created at 6:20 PM on 2/13/17.
 */
class CTRLException(key: String, vararg args: Any?) : CommandException("translation.test.args", LibrarianLib.PROXY.translate(key).format(*args), "")
class CTRLUsageException(key: String, vararg args: Any?) : WrongUsageException("translation.test.args", LibrarianLib.PROXY.translate(key).format(*args), "")
