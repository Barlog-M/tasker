package tasker.service

import com.rabbitmq.client.Channel
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when` as wh
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.support.GenericApplicationContext
import tasker.model.Batch
import tasker.model.Task
import tasker.model.jooq.enums.BatchType
import tasker.properties.TaskExecutorProperties
import tasker.task.TaskComponent
import java.util.UUID

class TaskExecutorServiceTest {
	private val batchId = UUID.randomUUID()
	private val batch = Batch(
		id = batchId,
		type = BatchType.foo,
		remain = 0,
		total = 1
	)
	private val batchService = mock(BatchService::class.java).also {
		wh(it.status(batchId)).thenReturn(batch)
	}
	private val properties = TaskExecutorProperties()
	private val context = GenericApplicationContext().apply {
		registerBeanDefinition("testTaskComponent", RootBeanDefinition(TestTaskComponent::class.java))
	}.also {
		it.refresh()
		it.start()
	}
	private val tag = 1L
	private val channel = mock(Channel::class.java)
	private val task = Task(
		batchId = batchId,
		className = TestTaskComponent::class.qualifiedName!!,
		params = mapOf("a" to "b")
	)

	@Test
	fun execute() {
		val service = TaskExecutorService(context, batchService, properties)
		service.execute(task, channel, tag, {
			verify(batchService, times(1)).decrement(batchId)
			verify(batchService, times(1)).status(batchId)
		})
	}
}

class TestTaskComponent : TaskComponent {
	override fun task(params: Map<String, Any>) {}
}
