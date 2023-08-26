package util

fun String.removedDoubleSpaces(): String {
    return this.replace("\\s{2,}".toRegex(), " ")
}

fun StringBuilder.removedDoubleSpaces(): String {
    return this.replace("\\s{2,}".toRegex(), " ")
}