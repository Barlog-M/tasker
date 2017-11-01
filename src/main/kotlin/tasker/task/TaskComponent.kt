package tasker.task

abstract class TaskComponent {
	@Volatile
	protected var canceled = false

	fun isCancelled() = canceled

	fun cancel() {
		canceled = true
	}

	protected fun reset() {
		canceled = false
	}

	open fun task(params: Map<String, Any>) {
		canceled = false
	}
}
