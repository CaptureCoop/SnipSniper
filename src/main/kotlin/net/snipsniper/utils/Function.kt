package net.snipsniper.utils

abstract class Function {
    open fun run(): Boolean = true
    open fun run(vararg args: String): Boolean = true
    open fun run(vararg args: Any?): Boolean = true
    open fun run(vararg args: Boolean): Boolean = true
    open fun run(vararg args: Integer): Boolean = true //TODO: Change to Int after portjob is complete
    open fun run(vararg args: ConfigSaveButtonState): Boolean = true
    open fun run(state: ConfigSaveButtonState): Boolean = true
}