package tasker.settings

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "tasks")
open class TaskExecutorSettings {
	open var threads = 1
}
