package net.snipsniper.utils

fun interface IFunction {
    fun run(vararg args: String)
}

fun interface CustomWindowListener {
    fun windowClosed()
}