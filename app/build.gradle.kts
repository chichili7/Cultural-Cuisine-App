plugins {
    alias(libs.plugins.android.application)
}

android {
    buildFeatures{
        viewBinding = true;
    }
    namespace = "com.example.culturalcuisineapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.culturalcuisineapp"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)
    implementation(libs.picasso)
    implementation(libs.gson)
    implementation(libs.gms.play.services.maps)
    implementation(libs.gms.play.services.location)
    implementation(libs.google.places)
    implementation(libs.okhttp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}