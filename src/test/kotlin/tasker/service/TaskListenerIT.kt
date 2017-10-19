package tasker.service

import com.rabbitmq.client.Channel
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.amqp.core.Message
import tasker.model.Task
import java.util.UUID

class TaskListenerTest {
	private val taskExecutorService = mock(TaskExecutorService::class.java)
	private val message = mock(Message::class.java)
	private val channel = mock(Channel::class.java)
	private val batchId = UUID.randomUUID()
	private val task = Task(
		batchId = batchId,
		className = "foo",
		params = mapOf("a" to "b")
	)

	@Test
	fun listener() {
		val taskListener = TaskListener(taskExecutorService)
		taskListener.listener(task, 1L, message, channel)
		verify(taskExecutorService, times(1))
			.execute(task, channel, 1L, taskListener.notificator)
	}
}
