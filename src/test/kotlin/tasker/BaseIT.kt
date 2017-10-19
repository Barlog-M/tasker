package tasker

import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import tasker.config.ITConfig

@RunWith(SpringRunner::class)
@SpringBootTest(
	classes = arrayOf(ITConfig::class),
	webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
abstract class BaseIT
