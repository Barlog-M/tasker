package tasker.component

import org.springframework.amqp.support.ConsumerTagStrategy
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class CustomConsumerTagStrategy(
	private val applicationContext: ApplicationContext,
	private val environment: Environment
) : ConsumerTagStrategy {
	override fun createConsumerTag(queue: String): String {
		return "${name()}_$queue"
	}

	private fun name() = "${applicationContext.id}_${profile()}"

	private fun profile() = if (environment.activeProfiles.isEmpty()) {
		"default"
	} else {
		environment.activeProfiles.first()
	}
}
