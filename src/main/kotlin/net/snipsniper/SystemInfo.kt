package net.snipsniper

import com.sun.management.OperatingSystemMXBean
import net.snipsniper.utils.Utils
import java.lang.management.ManagementFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class SystemInfo {
    companion object {
        private val os = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        private val r = Runtime.getRuntime()

        fun getName(): String = System.getProperty("os.name")
        fun getVersion(): String = Utils.getSystemVersion()
        fun getArch(): String = System.getProperty("os.arch")
        fun getTimeAndDate(): String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
        fun getTimeZone(): String = TimeZone.getDefault().id
        fun getPhysicalMemory(): Long = os.totalPhysicalMemorySize
        fun getFreePhysicalMemory(): Long = os.freePhysicalMemorySize
        fun getSwapMemory(): Long = os.totalSwapSpaceSize
        fun getFreeSwapMemory(): Long = os.freeSwapSpaceSize
        fun getAvailableProcessors(): Int = os.availableProcessors

        fun getMaxJavaMemory(): Long = r.maxMemory()
        fun getTotalJavaMemory(): Long = r.totalMemory()
        fun getFreeJavaMemory(): Long = r.freeMemory()

        fun getJavaVendor(): String = System.getProperty("java.vendor")
        fun getJavaVersion(): String = System.getProperty("java.version")
    }
}