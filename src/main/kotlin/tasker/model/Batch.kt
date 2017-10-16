package tasker.model

import tasker.model.jooq.enums.BatchType

data class Batch(
	val id: String,
	val type: BatchType,
	val total: Int,
	val remain: Int
)
