package tasker.service

import com.rabbitmq.client.Channel
import mu.KLogging
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import tasker.model.Batch
import tasker.model.Task

@Service
open class TaskListener(
	private val taskExecutorService: TaskExecutorService
) {
	companion object : KLogging()

	val notificator by lazy {
		{ batch: Batch ->
			logger.info { "task end: $batch" }
		}
	}

	@RabbitListener(
		queues = arrayOf("#{taskQueue}"),
		containerFactory = "rabbitListenerContainerFactoryWithManualAck",
		id = "task-listener"
	)
	open fun listener(@Payload task: Task,
					  @Header(AmqpHeaders.DELIVERY_TAG) tag: Long,
					  message: Message,
					  channel: Channel) {
		logger.trace { "delivery tag: $tag" }
		logger.trace { "received message: $message" }
		logger.debug { "received task: $task" }

		taskExecutorService.execute(task, channel, tag, notificator)
	}
}
