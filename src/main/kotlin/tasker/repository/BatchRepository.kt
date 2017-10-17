package tasker.repository

import org.jooq.DSLContext
import org.jooq.impl.DSL.sql
import org.springframework.stereotype.Repository
import tasker.model.jooq.tables.Batch.BATCH
import tasker.model.Batch
import tasker.model.jooq.enums.BatchType

@Repository
open class BatchRepository(
	private val context: DSLContext
) {
	open fun selectBy(id: String): Batch = context
		.select()
		.from(BATCH)
		.where(BATCH.ID.eq(id))
		.fetchOne { Batch(
			id = it[BATCH.ID],
			type = it[BATCH.TYPE],
			total = it[BATCH.TOTAL],
			remain = it[BATCH.REMAIN]
		) }

	open fun insert(id: String,
					type: BatchType,
					total: Int
	): Int = context
		.insertInto(BATCH)
		.set(BATCH.ID, id)
		.set(BATCH.TYPE, type)
		.set(BATCH.TOTAL, total)
		.set(BATCH.REMAIN, total)
		.execute()

	open fun update(id: String,
					remain: Int
	): Int = context
		.update(BATCH)
		.set(BATCH.ID, id)
		.set(BATCH.REMAIN, remain)
		.execute()

	open fun decrement(id: String) {
		context
			.update(BATCH)
			.set(BATCH.REMAIN, BATCH.REMAIN.minus(1))
			.where(BATCH.ID.eq(id).and(BATCH.REMAIN.gt(0)))
			.execute()
	}
}
