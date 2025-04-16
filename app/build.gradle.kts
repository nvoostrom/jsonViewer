plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.jsonviewer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.jsonviewer"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Configure Compose Compiler
composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {

    dependencies {
        // Core Android dependencies
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.activity.compose)

        // Compose dependencies
        implementation(platform(libs.compose.bom))
        implementation(libs.compose.ui)
        implementation(libs.compose.ui.graphics)
        implementation(libs.compose.ui.tooling.preview)
        implementation(libs.compose.material3)
        implementation(libs.androidx.material.icons.extended)
        implementation(libs.androidx.animation)
        implementation(libs.navigation.compose)

        // JSON parsing
        implementation(libs.moshi)
        implementation(libs.moshi.kotlin)
        ksp(libs.moshi.kotlin.codegen)

        // Testing
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.compose.bom))
        androidTestImplementation(libs.compose.ui.test.junit4)

        // Debug
        debugImplementation(libs.compose.ui.tooling)
        debugImplementation(libs.compose.ui.test.manifest)
    }
}