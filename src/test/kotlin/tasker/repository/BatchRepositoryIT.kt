package tasker.repository

import org.junit.Test

import org.junit.Assert.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import tasker.BaseIT
import tasker.model.jooq.enums.BatchType
import java.util.UUID

@Transactional
open class BatchRepositoryIT : BaseIT() {
	@Autowired
	private lateinit var batchRepository: BatchRepository

	@Test
	open fun selectBy() {
		val id = UUID.randomUUID()
		batchRepository.insert(id, BatchType.foo, 1)
		val batch = batchRepository.selectBy(id)
		assertNotNull(batch)
	}

	@Test
	open fun insert() {
	}

	@Test
	open fun update() {
	}

	@Test
	open fun decrement() {
	}
}
