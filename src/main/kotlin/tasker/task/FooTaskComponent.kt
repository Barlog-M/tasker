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

		if (ThreadLocalRandom.current().nextBoolean()) {
			logger.info { "foo task error" }
			throw RuntimeException("foo task exception")
		}
	}
}
