package tasker.service

import com.rabbitmq.client.Channel
import mu.KLogging
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import tasker.model.Task
import tasker.task.TaskComponent
import java.util.concurrent.Executors

@Service
open class TaskExecutorService(
	private val context: ApplicationContext,
	private val batchService: BatchService
) {
	companion object : KLogging()

	private val executor = Executors.newSingleThreadExecutor {
		Thread(it, "task-executor")
	}

	open fun execute(task: Task, channel: Channel, tag: Long) {
		executor.submit({ executeTask(task, channel, tag) })
	}

	private fun executeTask(task: Task, channel: Channel, tag: Long) {
		logger.info { "begin execute task $task" }

		val beanRaw = try {
			val clazz = Class.forName(task.className)
			context.getBean(clazz)
		} catch (e: RuntimeException) {
			logger.warn { "Wrong type of task. Not found bean of class: ${task.className}" }
			positiveFinal(task, channel, tag)
			return
		}

		val bean: TaskComponent = try {
			beanRaw as TaskComponent
		} catch (e: RuntimeException) {
			logger.warn { "Task bean type is not TaskComponent" }
			positiveFinal(task, channel, tag)
			return
		}

		logger.debug { "bean: $bean" }

		try {
			bean.task(task.params)
		} catch (e: RuntimeException) {
			logger.error { "Task execute error: ${e.message} $task" }
			negativeFinal(channel, tag)
			return
		}

		positiveFinal(task, channel, tag)
	}

	private fun negativeFinal(channel: Channel, tag: Long) {
		channel.basicNack(tag, false, true)
	}

	private fun positiveFinal(task: Task, channel: Channel, tag: Long) {
		logger.info { "final task: $task" }

		batchService.decrement(task.batchId)
		val batch = batchService.status(task.batchId)
		channel.basicAck(tag, false)
		logger.info { "batch status: $batch" }
	}
}