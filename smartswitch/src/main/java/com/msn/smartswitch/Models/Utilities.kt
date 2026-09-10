package com.msn.smartswitch.Models



import kotlin.math.ln
import kotlin.math.pow
import java.util.Locale


class Utilities {

    companion object {
        val PORT = 8080

        fun formatSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
            val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(units.indices)
            val size = bytes / 1024.0.pow(digitGroups.toDouble())
            return when {
                units[digitGroups] == "MB" -> String.format(Locale.ROOT, "%.0f MB", size)
                digitGroups >= 3 -> String.format(Locale.ROOT, "%.3f %s", size, units[digitGroups])
                else -> String.format(Locale.ROOT, "%.0f %s", size, units[digitGroups])
            }
        }



    }
}
