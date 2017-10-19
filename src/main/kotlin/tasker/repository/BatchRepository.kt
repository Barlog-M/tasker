package tasker.repository

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import tasker.model.jooq.tables.Batch.BATCH
import tasker.model.Batch
import tasker.model.jooq.enums.BatchType
import java.util.UUID

@Repository
open class BatchRepository(
	private val context: DSLContext
) {
	open fun selectBy(id: UUID): Batch = context
		.select()
		.from(BATCH)
		.where(BATCH.ID.eq(id))
		.fetchOne {
			Batch(
				id = it[BATCH.ID],
				type = it[BATCH.TYPE],
				total = it[BATCH.TOTAL],
				remain = it[BATCH.REMAIN]
			)
		}

	open fun new(id: UUID, type: BatchType, total: Int) = context
		.insertInto(BATCH)
		.set(BATCH.ID, id)
		.set(BATCH.TYPE, type)
		.set(BATCH.TOTAL, total)
		.set(BATCH.REMAIN, total)
		.execute()

	open fun decrement(id: UUID) = context
		.update(BATCH)
		.set(BATCH.REMAIN, BATCH.REMAIN.minus(1))
		.where(BATCH.ID.eq(id).and(BATCH.REMAIN.gt(0)))
		.execute()
}
