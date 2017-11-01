package tasker.task

import mu.KLogging
import org.springframework.stereotype.Component
import java.util.concurrent.ThreadLocalRandom

@Component
open class FooTaskComponent: TaskComponent() {
	companion object: KLogging()

	final override fun task(params: Map<String, Any>) {
		super.reset()

		logger.info { "foo task executed with params: $params" }

		val random = ThreadLocalRandom.current()

		var i = random.nextInt(10, 99)
		while(--i != 0) {
			try {
				Thread.sleep(100)
			} catch (e: InterruptedException) {
				logger.error("task interrupted", e)
			}

			if (canceled) {
				logger.info { "task canceled" }
				return
			}
		}
	}
}
