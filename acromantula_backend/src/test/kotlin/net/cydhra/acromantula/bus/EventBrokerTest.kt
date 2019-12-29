package net.cydhra.acromantula.bus

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class EventBrokerTest {

    /**
     * Test event without actual implementation
     */
    class EventTest : Event {
        override val channel = RootEventChannel("test")
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
}