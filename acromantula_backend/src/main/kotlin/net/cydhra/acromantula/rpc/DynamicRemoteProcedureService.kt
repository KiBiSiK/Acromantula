package net.cydhra.acromantula.rpc

import net.cydhra.acromantula.bus.Service
import org.apache.logging.log4j.LogManager

object DynamicRemoteProcedureService : Service {
    override val name: String = "drpc service"

    private val logger = LogManager.getLogger()


    override suspend fun initialize() {
        logger.info("running RPC server...")


//        EventBroker.registerEventListener(ApplicationStartupEvent::class, this::onStartUp)

//        server = ServerBuilder.forPort(26666).addService(this).build()
    }
}