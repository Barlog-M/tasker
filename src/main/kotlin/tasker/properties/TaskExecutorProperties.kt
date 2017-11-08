package tasker.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "tasks")
open class TaskExecutorProperties {
	open var threads = 1
	/** seconds */
	open var timeout = 10L
	open var priority = Thread.NORM_PRIORITY - 2
	open var sleep = 300L
}
