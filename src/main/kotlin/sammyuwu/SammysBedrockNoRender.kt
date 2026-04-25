package sammyuwu

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object SammysBedrockNoRender : ModInitializer {
    private val logger = LoggerFactory.getLogger("sammysbedrocknorender")

	override fun onInitialize() {
		logger.info("Hello Fabric world!")
	}
}
