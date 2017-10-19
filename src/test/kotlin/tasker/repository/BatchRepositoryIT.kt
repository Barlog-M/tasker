package tasker.repository

import org.junit.Test

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
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
		batchRepository.new(id, BatchType.foo, 1)
		val batch = batchRepository.selectBy(id)
		assertNotNull(batch)
	}

	@Test
	open fun new() {
		val id = UUID.randomUUID()
		batchRepository.new(id, BatchType.foo, 1)
		val batch = batchRepository.selectBy(id)
		assertNotNull(batch)
	}

	@Test
	open fun decrement() {
		val id = UUID.randomUUID()
		batchRepository.new(id, BatchType.foo, 1)
		val batch = batchRepository.selectBy(id)
		assertEquals(1, batch.remain)
		batchRepository.decrement(id)
		val batchDecremented = batchRepository.selectBy(id)
		assertEquals(0, batchDecremented.remain)
	}
}
