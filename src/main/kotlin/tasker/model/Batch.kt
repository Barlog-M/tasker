package tasker.model

import tasker.model.jooq.enums.BatchType
import java.util.UUID

data class Batch(
	val id: UUID,
	val type: BatchType,
	val total: Int,
	val remain: Int
)
