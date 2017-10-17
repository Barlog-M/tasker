package tasker.model

import java.util.UUID

data class Task(
	val batchId: UUID,
	val className: String,
	val params: Map<String, Any>
)
