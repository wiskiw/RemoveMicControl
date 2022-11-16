plugins {
    id("com.android.application")
    id("com.google.devtools.ksp").version("1.7.21-1.0.8")
    kotlin("android")
}

android {
    compileSdk = ProjectConfig.compileSdkVersion

    defaultConfig {
        applicationId = "dev.wiskiw.bluetoothmiccontrol"
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

    buildFeatures {
        viewBinding = true
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
    implementation(ProjectDependency.androidFragmentKtx.notation)

    implementation(ProjectDependency.viewbindingpropertydelegate.notation)

    implementation(ProjectDependency.koinAndroid.notation)
    implementation(ProjectDependency.koinAndroidNavigation.notation)
}
