package tasker.model

data class Task(
	val batchId: String,
	val className: String,
	val params: Map<String, Any>
)
