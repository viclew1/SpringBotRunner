package fr.lewon.bot.runner

import fr.lewon.bot.runner.bot.operation.BotOperation
import fr.lewon.bot.runner.bot.props.BotPropertyDescriptor
import fr.lewon.bot.runner.bot.props.BotPropertyType
import fr.lewon.bot.runner.bot.task.BotTask
import fr.lewon.bot.runner.errors.InvalidBotPropertyValueException
import fr.lewon.bot.runner.errors.MissingBotPropertyException
import fr.lewon.bot.runner.session.AbstractSessionManager
import fr.lewon.bot.runner.util.BeanUtil
import fr.lewon.bot.runner.util.BotPropertyParser

abstract class AbstractBotBuilder(private val botName: String, botPropertyDescriptors: List<BotPropertyDescriptor>) {

    constructor(botName: String, vararg botPropertyDescriptors: BotPropertyDescriptor) : this(botName, listOf(*botPropertyDescriptors))

    private val botPropertyDescriptors: List<BotPropertyDescriptor> = listOf(
            BotPropertyDescriptor(key = "auto_restart", type = BotPropertyType.INTEGER, defaultValue = null, description = "Amount of minutes before restarting a bot on crash. If null, the bot doesn't restart", isNeeded = false, isNullable = true),
            *botPropertyDescriptors.toTypedArray()
    )

    @Throws(InvalidBotPropertyValueException::class, MissingBotPropertyException::class)
    fun buildBot(login: String, password: String, properties: Map<String, String?>): Bot {
        val botPropertyStore = botPropertyParser.parseParams(properties, this.botPropertyDescriptors)
        return Bot(botPropertyStore, { b -> this.getInitialTasks(b) }, this.getBotOperations(), buildSessionManager(login, password))
    }

    protected abstract fun buildSessionManager(login: String, password: String): AbstractSessionManager

    protected abstract fun getBotOperations(): List<BotOperation>

    protected abstract fun getInitialTasks(bot: Bot): List<BotTask>

    companion object {
        private val botPropertyParser = BeanUtil.getBean(BotPropertyParser::class.java)
    }

}