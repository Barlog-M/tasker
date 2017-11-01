package tasker.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "tasks")
open class TaskExecutorProperties {
	open var threads = 1
	/** seconds */
	open var timeout = 5L
}
