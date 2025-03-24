package com.msn.smartswitch.Models



import kotlin.math.ln
import kotlin.math.pow


class Utilities {

    companion object {
        val PORT = 8080

//        fun formatSize(v: Long): String {
//            if (v <= 0) return "0 B" // Ensure proper return for zero values
//            val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB") // Proper unit sequence
//            val digitGroups = (Math.log10(v.toDouble()) / Math.log10(1024.0)).toInt()
//            return String.format("%.1f %s", v / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
//        }

        fun formatSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"

            val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
            val digitGroups = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(units.indices)

            val size = bytes / 1024.0.pow(digitGroups.toDouble())

            return when {
                units[digitGroups] == "MB" -> String.format("%.0f MB", size) // No decimal for MB
                digitGroups >= 3 -> String.format("%.3f %s", size, units[digitGroups]) // 3 decimals for GB+
                else -> String.format("%.0f %s", size, units[digitGroups]) // Default format
            }
        }



    }
}