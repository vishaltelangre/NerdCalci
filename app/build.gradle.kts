plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.vishaltelangre.nerdcalci"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vishaltelangre.nerdcalci"
        minSdk = 23
        versionCode = 360
        versionName = "3.6.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.testLogging {
                showStandardStreams = true
                events("passed", "skipped", "failed", "standardOut", "standardError")
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            }
        }
    }

    // Disable dependency metadata for F-Droid compatibility
    // See: https://forum.f-droid.org/t/build-fails-with-found-extra-signing-block/29220
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("androidTest") {
            assets.setSrcDirs(listOf(file("$projectDir/schemas")))
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")

    // Room (Local database)
    implementation("androidx.room:room-runtime:2.7.0")
    ksp("androidx.room:room-compiler:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Markdown rendering for Help Screen
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:ext-tables:4.6.2")

    testImplementation(libs.junit)
    implementation(libs.json)
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation("androidx.room:room-testing:2.7.0")
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Automatically bundle the documentation from the project root into APK assets
// so that the HelpScreen and ChangelogScreen can render them dynamically
tasks.register<Copy>("copyReferenceDocsToAssets") {
    from("$rootDir/REFERENCE.md")
    into("$projectDir/src/main/assets")
}
tasks.register<Copy>("copyChangelogToAssets") {
    from("$rootDir/CHANGELOG.md")
    into("$projectDir/src/main/assets")
}
tasks.named("preBuild") {
    dependsOn("copyReferenceDocsToAssets", "copyChangelogToAssets")
}
