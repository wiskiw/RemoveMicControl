// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply{
        set("kotlinVersion", "1.5.0")
    }
    val supportLibraryVersion = extra.get("kotlinVersion") as String

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:7.2.2")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:$supportLibraryVersion")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}
