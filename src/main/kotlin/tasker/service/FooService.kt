package tasker.service

import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import tasker.model.Task
import tasker.model.jooq.enums.BatchType
import tasker.task.FooTaskComponent
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

@Service
open class FooService(
	private val batchService: BatchService
) {
	companion object: KLogging()

	@Scheduled(fixedRate = 5000L)
	open fun foo() {
		val random = ThreadLocalRandom.current()

		val tasks = IntRange(0, random.nextInt(3, 9))
			.map {
				{ batchId: UUID ->
					Task(
						batchId = batchId,
						className = FooTaskComponent::class.qualifiedName!!,
						params = mapOf("a" to random.nextInt(0, 99))
					)
				}
			}
			.toList()

		logger.info { "created batch of ${tasks.size} tasks" }

		batchService.start(BatchType.foo, tasks)
	}
}
