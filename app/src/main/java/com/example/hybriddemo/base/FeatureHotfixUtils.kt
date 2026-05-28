package com.example.hybriddemo.base

import java.io.File

object FeatureHotfixUtils {
    fun needFeatureHotfix(rnBranch: String?): Boolean {
        if (rnBranch.isNullOrBlank()) return false
        if (rnBranch == "default") return false
        return true
    }

    fun getJSBundleFile(rnBranch: String?): String? {
        if (rnBranch.isNullOrBlank()) return null
        return ""
    }
}