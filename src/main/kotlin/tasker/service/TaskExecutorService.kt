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
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
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
		try {
			val done = executor.awaitTermination(1, TimeUnit.MINUTES)
			if (!done) {
				logger.warn { "not all task done during thread pool shutdown" }
			}
		} catch (e: InterruptedException) {
			throw RuntimeException("task executor pool await termination error", e)
		}
	}

	open fun execute(task: Task,
					 channel: Channel,
					 tag: Long,
					 notify: (Batch) -> Unit) {
		val optionalComponent = getTaskComponent(task, channel, tag, notify)
		if (!optionalComponent.isPresent) return
		val component = optionalComponent.get()

		val future: Future<*> = executor.submit({
			executeTask(component, task, channel, tag, notify)
		})

		try {
			future.get(properties.timeout, TimeUnit.SECONDS)
		} catch (e: TimeoutException) {
			logger.error("task execution failed by timeout: $task", e)
			negativeFinal(component, channel, tag)
		} catch (e: ExecutionException) {
			logger.error("task execution failed: $task", e)
			negativeFinal(component, channel, tag)
		} catch (e: InterruptedException) {
			logger.error("task execution interrupted: $task", e)
			negativeFinal(component, channel, tag)
		}
	}

	private fun getTaskComponent(task: Task,
								 channel: Channel,
								 tag: Long,
								 notify: (Batch) -> Unit): Optional<TaskComponent> {
		val clazz = try {
			Class.forName(task.className)
		} catch (e: ClassNotFoundException) {
			logger.warn { "Wrong type of task. Not found class: ${task.className}" }
			positiveFinal(task, channel, tag, notify)
			return Optional.empty()
		}

		val bean = try {
			context.getBean(clazz)
		} catch (e: RuntimeException) {
			logger.warn { "Wrong type of task. Not found bean of class: ${task.className}" }
			positiveFinal(task, channel, tag, notify)
			return Optional.empty()
		}

		if (bean !is TaskComponent) {
			logger.warn { "task bean type is not TaskComponent $task" }
			positiveFinal(task, channel, tag, notify)
			return Optional.empty()
		}

		logger.info { "bean: $bean" }
		return Optional.of(bean)
	}

	private fun executeTask(component: TaskComponent,
							task: Task,
							channel: Channel,
							tag: Long,
							notify: (Batch) -> Unit) {
		logger.info { "begin execute task $task" }

		try {
			component.task(task.params)
		} catch (e: RuntimeException) {
			logger.error { "task execute error: ${e.message} $task" }
			negativeFinal(component, channel, tag)
			return
		}

		if (!component.isCancelled()) positiveFinal(task, channel, tag, notify)
	}

	private fun negativeFinal(component: TaskComponent,
							  channel: Channel,
							  tag: Long) {
		logger.info { "tag: $tag" }
		try {
			channel.basicNack(tag, false, true)
			component.cancel()
		} catch (e: IOException) {
			logger.error("error basic negative ack with tag $tag and channel: $channel", e)
		}
	}

	private fun positiveFinal(task: Task,
							  channel: Channel,
							  tag: Long,
							  notify: (Batch) -> Unit) {
		logger.info { "tag: $tag" }
		try {
			channel.basicAck(tag, false)

			val batchOptional = saveState(task)

			if (batchOptional.isPresent) {
				notify(batchOptional.get())
			}
		} catch (e: IOException) {
			logger.error("error basic ack with tag $tag and channel: $channel", e)
		} catch (e: RuntimeException) {
			logger.error("error on call saveState lambda for task: $task", e)
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
