package tasker.service

import org.junit.Test
import org.mockito.Matchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.amqp.rabbit.core.RabbitTemplate
import tasker.model.Task
import tasker.model.jooq.enums.BatchType
import tasker.repository.BatchRepository
import java.util.UUID

class BatchServiceTest {
	private val batchRepository = mock(BatchRepository::class.java)
	private val rabbitTemplate = mock(RabbitTemplate::class.java)
	private val batchId = UUID.randomUUID()
	private val testBatchIdGenerator: () -> UUID = { batchId }
	private val service = BatchService(batchRepository, rabbitTemplate)
	private val tasks = listOf({batchId: UUID ->
		Task(
			batchId = batchId,
			className = "foo",
			params = mapOf("a" to "b")
		)
	})
	private val type = BatchType.foo

	@Test
	fun start() {
		service.start(type, tasks, testBatchIdGenerator)
		verify(batchRepository, times(1)).new(batchId, type, tasks.size)
		verify(rabbitTemplate, times(1)).convertAndSend(any<String>(), any<Task>())
	}

	@Test
	fun decrement() {
		service.decrement(batchId)
		verify(batchRepository, times(1)).decrement(batchId)
	}

	@Test
	fun status() {
		service.status(batchId)
		verify(batchRepository, times(1)).selectBy(batchId)
	}
}
