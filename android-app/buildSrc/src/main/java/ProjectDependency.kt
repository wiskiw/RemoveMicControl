object ProjectDependency {

    val kotlinCoreKtx = LibraryDependency("androidx.core", "core-ktx", "1.7.0")

    val androidAppCompat = LibraryDependency("androidx.appcompat", "appcompat", "1.5.1")
    val androidMaterial = LibraryDependency("com.google.android.material", "material", "1.7.0")
    val androidConstraintLayout = LibraryDependency("androidx.constraintlayout", "constraintlayout", "2.1.4")
    val androidLivedata = LibraryDependency("androidx.lifecycle", "lifecycle-livedata-ktx", "2.5.1")
    val androidViewModel = LibraryDependency("androidx.lifecycle", "lifecycle-viewmodel-ktx", "2.5.1")
    val androidFragmentKtx = LibraryDependency("androidx.fragment", "fragment-ktx", "1.3.2")

    val viewbindingpropertydelegate = LibraryDependency(
        "com.github.kirich1409",
        "viewbindingpropertydelegate-noreflection",
        "1.5.6",
    )


    private const val koinAndroidVersion = "3.3.0"
    val koinAndroid = LibraryDependency("io.insert-koin", "koin-android", koinAndroidVersion)
    val koinAndroidNavigation = LibraryDependency("io.insert-koin", "koin-androidx-navigation", koinAndroidVersion)

    val xxx = LibraryDependency("group", "module", "000")
}