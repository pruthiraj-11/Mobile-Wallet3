buildscript {
    dependencies {
        classpath(libs.google.services)
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
}