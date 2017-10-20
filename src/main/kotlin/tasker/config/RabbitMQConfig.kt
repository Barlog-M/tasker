package tasker.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.AcknowledgeMode
import tasker.component.CustomConsumerTagStrategy
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Exchange
import org.springframework.amqp.core.ExchangeBuilder
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.support.RetryTemplate
import org.springframework.util.backoff.ExponentialBackOff

@Configuration
open class RabbitMQConfig {
	companion object {
		private const val TTL = 5000L
	}

	@Bean
	open fun messageConverter(mapper: ObjectMapper) = Jackson2JsonMessageConverter(mapper)

	@Bean
	open fun taskExchange(): Exchange = ExchangeBuilder
		.directExchange("task.exchange").durable(true).build()

	@Bean
	open fun taskQueue(taskDlExchange: Exchange): Queue = QueueBuilder
		.durable("task.queue")
		//.withArgument("x-message-ttl", TTL)
		.withArgument("x-dead-letter-exchange", taskDlExchange.name)
		.build()

	@Bean
	open fun taskBinding(taskExchange: Exchange,
						 taskQueue: Queue): Binding = BindingBuilder
		.bind(taskQueue).to(taskExchange).with("").noargs()

	@Bean
	open fun taskDlExchange(): Exchange = ExchangeBuilder
		.directExchange("task.dl.exchange").durable(true).build()

	@Bean
	open fun taskDlQueue(taskQueue: Queue): Queue = QueueBuilder
		.durable("${taskQueue.name}.dl")
		.build()

	@Bean
	open fun taskDlBinding(taskDlExchange: Exchange,
						   taskDlQueue: Queue,
						   taskQueue: Queue): Binding = BindingBuilder
		.bind(taskDlQueue).to(taskDlExchange).with(taskQueue.name).noargs()

	@Bean
	open fun rabbitTemplate(connectionFactory: ConnectionFactory): AmqpTemplate {
		val template = RabbitTemplate(connectionFactory)
		template.isChannelTransacted = true
		val retryTemplate = RetryTemplate()
		retryTemplate.setBackOffPolicy(ExponentialBackOffPolicy())
		template.setRetryTemplate(retryTemplate)
		return template
	}

	@Bean
	open fun rabbitListenerContainerFactory(
		configurer: SimpleRabbitListenerContainerFactoryConfigurer,
		connectionFactory: ConnectionFactory,
		customConsumerTagStrategy: CustomConsumerTagStrategy
	) = rabbitListenerFactory(
		configurer,
		connectionFactory,
		customConsumerTagStrategy
	)

	@Bean
	open fun rabbitListenerContainerFactoryWithManualAck(
		configurer: SimpleRabbitListenerContainerFactoryConfigurer,
		connectionFactory: ConnectionFactory,
		customConsumerTagStrategy: CustomConsumerTagStrategy
	) = rabbitListenerFactory(
		configurer,
		connectionFactory,
		customConsumerTagStrategy
	).apply {
		setAcknowledgeMode(AcknowledgeMode.MANUAL)
	}

	private fun rabbitListenerFactory(
		configurer: SimpleRabbitListenerContainerFactoryConfigurer,
		connectionFactory: ConnectionFactory,
		customConsumerTagStrategy: CustomConsumerTagStrategy
	) = SimpleRabbitListenerContainerFactory().also {
		configurer.configure(it, connectionFactory)
		it.setRecoveryBackOff(ExponentialBackOff())
		it.setConsumerTagStrategy(customConsumerTagStrategy)
	}
}
