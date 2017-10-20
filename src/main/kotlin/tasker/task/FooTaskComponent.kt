package tasker.task

import mu.KLogging
import org.springframework.stereotype.Component
import java.lang.RuntimeException
import java.util.concurrent.ThreadLocalRandom

@Component
open class FooTaskComponent: TaskComponent {
	companion object: KLogging()

	override fun task(params: Map<String, Any>) {
		logger.info { "foo task executed with params: $params" }

		val random = ThreadLocalRandom.current()
		val pause = random.nextLong(1000, 10000)

		logger.info { "task paused for $pause ms" }
		Thread.sleep(pause)

		if (ThreadLocalRandom.current().nextBoolean()) {
			throw RuntimeException("foo task exception")
		}
	}
}
