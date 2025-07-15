package org.easybangumi.next.shared.source.rhino

import kotlinx.coroutines.CoroutineDispatcher
import org.apache.logging.log4j.Logger
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.rhino.RhinoService

class RhinoServiceImpl : RhinoService {
    override fun getSingletonDispatcher(): CoroutineDispatcher {
        return coroutineProvider.newSingle()
    }

    override fun getLogger(tag: String): org.slf4j.Logger {
        return logger(tag).getSlf4jLogger()
    }
}