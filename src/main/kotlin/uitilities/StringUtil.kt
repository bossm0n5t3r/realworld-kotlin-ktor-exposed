package me.bossm0n5t3r.uitilities

object StringUtil {
    fun String.toSlug() =
        lowercase()
            .replace("\n", " ")
            .replace("[^a-z\\d\\s]".toRegex(), " ")
            .split(" ")
            .filter { it.isNotEmpty() }
            .joinToString("-")
            .replace("-+".toRegex(), "-")
}
