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
	open fun start(batchType: BatchType,
				   tasks: List<(UUID) -> Task>,
				   idGenerator: () -> UUID = UUID::randomUUID) {
		val id = idGenerator()
		logger.info { "start batch with id: $id and tasks: ${tasks.size}" }

		batchRepository.new(
			id = id,
			type = batchType,
			total = tasks.size
		)

		tasks.forEach {
			rabbitTemplate.convertAndSend(taskQueueName, it(id))
		}
	}

	open fun decrement(id: UUID) = batchRepository.decrement(id)

	open fun status(id: UUID) = batchRepository.selectBy(id)
}
