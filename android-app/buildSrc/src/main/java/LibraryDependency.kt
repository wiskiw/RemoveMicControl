data class LibraryDependency(
    val group: String,
    val module: String,
    val version: String,
) {
    val notation: String = "$group:$module:$version"
}
