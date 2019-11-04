package net.cydhra.acromantula.bus

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.cydhra.acromantula.bus.event.Event
import net.cydhra.acromantula.bus.event.Request
import net.cydhra.acromantula.bus.service.Service
import org.junit.jupiter.api.Test

internal class EventBrokerTest {

    /**
     * Test event without actual implementation
     */
    class EventTest : Event

    /**
     * Test request without actual implementation
     */
    class RequestTest : Request<String> {
        override fun fulfil(data: String) {
            TODO("not implemented")
        }
    }

    @Test
    fun testServices() {
        val service = mockk<Service>(relaxed = true)

        runBlocking {
            EventBroker.registerService(service)
        }

        coVerify { service.initialize() }
    }

    @Test
    fun testEventHandling() {
        val handler = mockk<suspend (EventTest) -> Unit>(relaxed = true)
        val event = mockk<EventTest>(relaxed = true)

        runBlocking {
            EventBroker.registerEventListener(EventTest::class, handler)
            EventBroker.fireEvent(event)
            delay(10)
        }

        coVerify { handler.invoke(allAny()) }
    }

    @Test
    fun testRequestHandling() {
        val handler = mockk<suspend (RequestTest) -> Unit>()
        val event = mockk<RequestTest>(relaxed = true)

        coEvery { handler.invoke(allAny()) } returns Unit

        runBlocking {
            EventBroker.registerRequestHandler(RequestTest::class, handler)
            EventBroker.handleRequest(event)
        }

        coVerify { handler.invoke(allAny()) }
    }
}