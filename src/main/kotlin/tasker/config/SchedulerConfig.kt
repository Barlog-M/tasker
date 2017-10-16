package tasker.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@EnableAsync
@EnableScheduling
@Configuration
open class SchedulerConfig {
	@Bean
	open fun taskScheduler() = ThreadPoolTaskScheduler()
}
