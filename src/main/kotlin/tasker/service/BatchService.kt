package tasker.service

import mu.KLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tasker.model.Task
import tasker.model.jooq.enums.BatchType
import tasker.repository.BatchRepository
import java.util.UUID

@Service
open class BatchService(
	private val batchRepository: BatchRepository,
	private val rabbitTemplate: RabbitTemplate
) {
	companion object : KLogging()

	@Value("#{taskQueue.getName()}")
	private val taskQueueName: String = ""

	@Transactional
	open fun start(batchType: BatchType, tasks: List<(UUID) -> Task>) {
		val id = UUID.randomUUID()
		logger.info { "start batchId: $id tasks: $tasks" }

		batchRepository.insert(
			id = id,
			type = batchType,
			total = tasks.size
		)

		tasks.forEach {
			rabbitTemplate.convertAndSend(taskQueueName, it(id))
		}
	}

	open fun decrement(id: UUID) {
		logger.info { "decrement for id: $id" }
		try {
			batchRepository.decrement(id)
		} catch (e: RuntimeException) {
			logger.error { "db decrement error: $e" }
		}
	}

	open fun status(id: UUID) =
		try {
			batchRepository.selectBy(id)
		} catch (e: RuntimeException) {
			logger.error { "db decrement error: $e" }
		}
}
