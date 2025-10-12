plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    alias(libs.plugins.hilt.android)}

android {
    namespace = "com.example.teamup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.teamup"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // ================ CORE ANDROID ================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-splashscreen:1.0.0")

    // ================ COMPOSE ================
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
    implementation("androidx.compose.material3:material3:1.2.1")

    // ================ MATERIAL DESIGN ================
    implementation("com.google.android.material:material:1.11.0")

    // ================ FIREBASE BOM (MUST BE FIRST) ================
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))

    // ================ FIREBASE SERVICES ================
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-appcheck-debug:17.0.0")

    // ================ GOOGLE PLAY SERVICES ================
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // ================ COROUTINES ================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ================ BIOMETRIC ================
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // ================ UI ENHANCEMENTS ================
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.google.accompanist:accompanist-flowlayout:0.27.1")
    implementation("com.airbnb.android:lottie-compose:6.4.0")

    // ================ TESTING ================
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.functions.ktx)
//    implementation(libs.androidx.ui.test.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation(libs.androidx.navigation.compose)
    implementation ("com.google.accompanist:accompanist-flowlayout:0.27.1")
    implementation ("androidx.compose.material:material-icons-extended:1.3.1")
    implementation ("androidx.core:core-splashscreen:1.0.0")
    implementation("com.airbnb.android:lottie-compose:6.4.0")

    // Coroutines for Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.1")
    implementation ("com.google.android.material:material:1.9.0") // atau versi terbaru
    implementation ("androidx.compose.material3:material3:1.2.1") // atau versi terbaru

    // Firebase Firestore
    implementation ("com.google.firebase:firebase-firestore-ktx")

    // Firebase Authentication (jika diperlukan)
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore")
    //app check untuk keperluan verifikasi
    implementation ("com.google.firebase:firebase-appcheck-debug:17.0.0")

    // Firebase Storage (jika diperlukan)
    implementation ("com.google.firebase:firebase-storage-ktx")

    // Implementasi Fingerprint
    implementation ("androidx.biometric:biometric:1.2.0-alpha05")

    // Google Sign-In and Drive
    implementation ("com.google.android.gms:play-services-auth:21.0.0")
    implementation ("com.google.api-client:google-api-client-android:2.2.0")
    implementation ("com.google.apis:google-api-services-drive:v3-rev20231128-2.0.0")
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation ("com.google.http-client:google-http-client-gson:1.43.3")
    implementation ("com.google.http-client:google-http-client-android:1.43.3")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation ("androidx.compose.material:material:1.5.4")

    implementation ("com.google.accompanist:accompanist-pager:0.32.0")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.32.0")

    implementation ("com.google.accompanist:accompanist-swiperefresh:0.32.0")
    implementation ("com.google.accompanist:accompanist-placeholder-material:0.32.0")
}