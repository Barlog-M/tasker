package tasker.service

import com.rabbitmq.client.Channel
import mu.KLogging
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import tasker.model.Batch
import tasker.model.Task
import tasker.properties.TaskExecutorProperties
import tasker.task.TaskComponent
import java.io.IOException
import java.util.Optional
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.PreDestroy

@Service
open class TaskExecutorService(
	private val context: ApplicationContext,
	private val batchService: BatchService,
	private val properties: TaskExecutorProperties
) {
	companion object : KLogging()

	private val executor by lazy {
		Executors.newFixedThreadPool(properties.threads, object : ThreadFactory {
			private val threadNumber = AtomicInteger(1)

			override fun newThread(r: Runnable) =
				Thread(r, "task-executor-${threadNumber.getAndIncrement()}")
		})
	}

	@PreDestroy
	open fun destroy() {
		executor.shutdown()
	}

	open fun execute(task: Task,
					 channel: Channel,
					 tag: Long,
					 notify: (Batch) -> Unit) {
		executor.submit({ executeTask(task, channel, tag, notify) })
	}

	private fun executeTask(task: Task,
							channel: Channel,
							tag: Long,
							notify: (Batch) -> Unit) {
		logger.info { "begin execute task $task" }

		val clazz = try {
			Class.forName(task.className)
		} catch (e: ClassNotFoundException) {
			logger.warn { "Wrong type of task. Not found class: ${task.className}" }
			positiveFinal(channel, tag)
			saveState(task)
			return
		}

		val beanRaw = try {
			context.getBean(clazz)
		} catch (e: RuntimeException) {
			logger.warn { "Wrong type of task. Not found bean of class: ${task.className}" }
			positiveFinal(channel, tag)
			saveState(task)
			return
		}

		val bean: TaskComponent = try {
			beanRaw as TaskComponent
		} catch (e: RuntimeException) {
			logger.warn { "task bean type is not TaskComponent" }
			positiveFinal(channel, tag)
			saveState(task)
			return
		}

		logger.debug { "bean: $bean" }

		try {
			bean.task(task.params)
		} catch (e: RuntimeException) {
			logger.error { "task execute error: ${e.message} $task" }
			negativeFinal(channel, tag)
			return
		}

		positiveFinal(channel, tag)
		val batchOptional = saveState(task)

		try {
			if (batchOptional.isPresent) {
				notify(batchOptional.get())
			}
		} catch (e: RuntimeException) {
			logger.error("error on call saveState lambda for task: $task", e)
		}
	}

	private fun negativeFinal(channel: Channel, tag: Long) {
		try {
			channel.basicNack(tag, false, true)
		} catch (e: IOException) {
			logger.error("error basic negative ack with tag $tag and channel: $channel", e)
		}
	}

	private fun positiveFinal(channel: Channel, tag: Long) {
		try {
			channel.basicAck(tag, false)
		} catch (e: IOException) {
			logger.error("error basic ack with tag $tag and channel: $channel", e)
		}
	}

	private fun saveState(task: Task): Optional<Batch> =
		try {
			batchService.decrement(task.batchId)
			Optional.of(batchService.status(task.batchId))
		} catch (e: RuntimeException) {
			logger.error("error saveState final task: $task", e)
			Optional.empty()
		}
}
