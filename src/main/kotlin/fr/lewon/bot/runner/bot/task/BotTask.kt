package fr.lewon.bot.runner.bot.task

import fr.lewon.bot.runner.Bot
import fr.lewon.bot.runner.lifecycle.task.TaskState
import org.slf4j.LoggerFactory
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.TriggerContext
import java.util.*

abstract class BotTask @JvmOverloads constructor(val name: String, val bot: Bot, val initialDelayMillis: Long = 0) : Trigger, Runnable {
    var state = TaskState.PENDING
        private set
    private var taskResult: TaskResult? = null

    override fun run() {
        try {
            this.taskResult = this.doExecute(this.bot)
        } catch (e: Exception) {
            LOGGER.error("An error occurred while processing [${this.javaClass.canonicalName}]", e)
            bot.crash(e.message)
        }

    }

    /**
     * Executes the bot task and returns the delay until next execution in millis
     *
     * @param bot
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    protected abstract fun doExecute(bot: Bot): TaskResult

    override fun nextExecutionTime(triggerContext: TriggerContext): Date? {
        if (this.state == TaskState.CRASHED) {
            return null
        }
        try {
            if (this.state == TaskState.PENDING) {
                this.state = TaskState.ACTIVE
                return Date(System.currentTimeMillis() + initialDelayMillis)
            }
            this.taskResult?.tasksToCreate
                    ?.let { bot.startTasks(it) }
            
            this.taskResult?.delay?.getDelayMillis()
                    ?.takeIf { it > 0 }
                    ?.let { triggerContext.lastCompletionTime()?.time?.plus(it) }
                    ?.let { return Date(it) }
        } catch (e: Exception) {
            LOGGER.error("An error occurred while fetching next [${this.javaClass.canonicalName}] execution time", e)
            bot.crash(e.message)
        }

        return null
    }

    fun crash() {
        this.state = TaskState.CRASHED
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BotTask::class.java)
    }
}
