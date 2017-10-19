package tasker.config

import org.springframework.amqp.rabbit.test.RabbitListenerTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.util.EnvironmentTestUtils
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import tasker.App
import javax.annotation.PostConstruct

@Configuration
@EnableAutoConfiguration
@ComponentScan(
	basePackages = arrayOf("tasker"),
	excludeFilters = arrayOf(
		ComponentScan.Filter(
			type = FilterType.ASSIGNABLE_TYPE,
			classes = arrayOf(
				App::class,
				SchedulerConfig::class
			)
		)
	)
)
@RabbitListenerTest(capture = true)
open class ITConfig {
	@Value("\${spring.datasource.username}")
	private val datasourceUsername = ""

	@Value("\${spring.datasource.password}")
	private val datasourcePassword = ""

	@Value("\${spring.rabbitmq.username}")
	private val rabbitmqUsername = ""

	@Value("\${spring.rabbitmq.password}")
	private val rabbitmqPassword = ""

	@Autowired
	private lateinit var context: ConfigurableApplicationContext

	@Autowired
	private lateinit var rabbitContainer: KGenericContainer

	@Autowired
	private lateinit var postgresContainer: KPostgreSQLContainer

	class KPostgreSQLContainer(imageName: String) :
		PostgreSQLContainer<KPostgreSQLContainer>(imageName)

	class KGenericContainer(imageName: String) :
		GenericContainer<KGenericContainer>(imageName)

	@Bean(initMethod = "start", destroyMethod = "stop")
	open fun postgresContainer(): KPostgreSQLContainer =
		KPostgreSQLContainer("postgres:alpine")
			.withDatabaseName("postgres")
			.withUsername(datasourceUsername)
			.withPassword(datasourcePassword)

	@Bean(initMethod = "start", destroyMethod = "stop")
	open fun rabbitContainer(): KGenericContainer =
		KGenericContainer("rabbitmq:management-alpine")
			.withExposedPorts(5672)
			.withEnv("RABBITMQ_DEFAULT_USER", rabbitmqUsername)
			.withEnv("RABBITMQ_DEFAULT_PASS", rabbitmqPassword)

	@PostConstruct
	open fun init() {
		EnvironmentTestUtils.addEnvironment(
			context,
			"spring.rabbitmq.port=${rabbitContainer.getMappedPort(5672)}",
			"spring.datasource.url=jdbc:postgresql://localhost:${postgresContainer.getMappedPort(5432)}/postgres"
		)
	}
}
