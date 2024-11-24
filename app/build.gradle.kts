plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.finalproject_mobdev"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.finalproject_mobdev"
        minSdk = 24
        targetSdk = 34
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("com.google.android.exoplayer:exoplayer:2.18.1")
    implementation("com.google.firebase:firebase-auth-ktx:21.0.3")
    implementation ("androidx.navigation:navigation-compose:2.4.2")
    implementation("androidx.navigation:navigation-compose:2.7.0")
    implementation ("com.google.android.gms:play-services-maps:18.0.2")
    implementation ("com.google.maps.android:maps-compose:1.0.0" )// For Jetpack Compose integration with Maps
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.maps.android:maps-compose:2.2.0")
    implementation ("com.google.accompanist:accompanist-permissions:0.31.0-alpha")
    implementation ("com.google.accompanist:accompanist-navigation-material:0.31.0-alpha")
    implementation ("com.google.android.gms:play-services-location:21.0.1") // For GPS location
    implementation ("androidx.activity:activity-compose:1.5.1") // For permission handling in Compose
    implementation ("com.google.accompanist:accompanist-permissions:0.28.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("androidx.compose.ui:ui:1.3.0") // Adjust version if needed
    implementation ("androidx.compose.material3:material3:1.0.0-alpha11") // Material 3 for Compose
    implementation ("com.google.accompanist:accompanist-permissions:0.30.0") // Accompanist for permissions
    implementation ("com.google.firebase:firebase-auth-ktx:21.1.0") // Firebase authentication (optional)
    implementation ("androidx.compose.material:material-icons-extended:1.5.1")

}