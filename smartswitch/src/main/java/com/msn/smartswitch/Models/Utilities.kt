package com.msn.smartswitch.Models


class Utilities {

    companion object {
        val PORT = 8080

        fun formatSize(v: Long): String {
            if (v <= 0) return "0 B" // Ensure proper return for zero values
            val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB") // Proper unit sequence
            val digitGroups = (Math.log10(v.toDouble()) / Math.log10(1024.0)).toInt()
            return String.format("%.1f %s", v / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
        }
    }
}