plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = ProjectConfig.compileSdkVersion

    defaultConfig {
        applicationId = "dev.wiskiw.bluetoothmiccontol"
        minSdk = ProjectConfig.minSdkVersion
        targetSdk = ProjectConfig.targetSdkVersion
        versionCode = ProjectConfig.versionCode
        versionName = ProjectConfig.versionName
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

dependencies {
    implementation(ProjectDependency.kotlinCoreKtx.notation)

    implementation(ProjectDependency.androidAppCompat.notation)
    implementation(ProjectDependency.androidMaterial.notation)
    implementation(ProjectDependency.androidConstraintLayout.notation)
    implementation(ProjectDependency.androidLivedata.notation)
    implementation(ProjectDependency.androidViewModel.notation)
}
