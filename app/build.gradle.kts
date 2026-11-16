plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.rideshare.malawi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rideshare.malawi"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1-ui-milestone"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    configurations.configureEach {
        resolutionStrategy {
            force("androidx.test.espresso:espresso-core:3.7.0")
            force("androidx.test:core:1.7.0")
            force("androidx.test:runner:1.6.1")
            force("androidx.test:rules:1.6.1")
            force("androidx.test.ext:junit:1.3.0")
            force("androidx.tracing:tracing:1.1.0")
            force("androidx.concurrent:concurrent-futures:1.2.0")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.1")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-test-jvm:1.8.1")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-functions")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
