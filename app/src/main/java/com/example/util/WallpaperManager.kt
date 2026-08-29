package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.model.AppCustomSettings
import com.example.model.TextColorAccent
import com.example.model.ThemePreset
import com.example.model.WallpaperStyle
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object WallpaperManager {

    private const val PREFS_NAME = "a23_wallpaper_settings_prefs"
    private const val KEY_WALLPAPER_ENABLED = "key_wp_enabled"
    private const val KEY_WALLPAPER_STYLE = "key_wp_style"
    private const val KEY_WALLPAPER_DIM = "key_wp_dim"
    private const val KEY_CUSTOM_WP_URI = "key_custom_wp_uri"
    private const val KEY_CUSTOM_WP_LIST = "key_custom_wp_list"
    private const val KEY_TEXT_ACCENT = "key_text_accent"
    private const val KEY_THEME_PRESET = "key_theme_preset"
    private const val KEY_GLASS_BLUR = "key_glass_blur"

    /**
     * Copies selected avatar image from Gallery Uri into internal app files directory
     * so it permanently persists for the admin/user profile.
     */
    fun saveAvatarFromUri(context: Context, uri: Uri): String? {
        return try {
            val avatarsDir = File(context.filesDir, "avatars")
            if (!avatarsDir.exists()) {
                avatarsDir.mkdirs()
            }

            val fileName = "admin_avatar_${System.currentTimeMillis()}.jpg"
            val destFile = File(avatarsDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input: InputStream ->
                val bitmap = BitmapFactory.decodeStream(input)
                if (bitmap != null) {
                    FileOutputStream(destFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    }
                    destFile.absolutePath
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copies selected image from Gallery Uri into internal app files directory
     * so it permanently persists and doesn't expire or lose permissions.
     */
    fun saveWallpaperFromUri(context: Context, uri: Uri): String? {
        return try {
            val wallpapersDir = File(context.filesDir, "wallpapers")
            if (!wallpapersDir.exists()) {
                wallpapersDir.mkdirs()
            }

            val fileName = "wp_${System.currentTimeMillis()}.jpg"
            val destFile = File(wallpapersDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input: InputStream ->
                // Decode sampled bitmap to prevent out of memory and write optimized JPEG
                val bitmap = BitmapFactory.decodeStream(input)
                if (bitmap != null) {
                    FileOutputStream(destFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    destFile.absolutePath
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Retrieves all saved custom wallpaper file paths in the internal directory.
     */
    fun getSavedCustomWallpapers(context: Context): List<String> {
        val wallpapersDir = File(context.filesDir, "wallpapers")
        if (!wallpapersDir.exists()) return emptyList()
        val files = wallpapersDir.listFiles { file ->
            file.isFile && (file.name.endsWith(".jpg", ignoreCase = true) || file.name.endsWith(".png", ignoreCase = true))
        } ?: return emptyList()
        return files.sortedByDescending { it.lastModified() }.map { it.absolutePath }
    }

    /**
     * Deletes a custom wallpaper file from internal storage.
     */
    fun deleteCustomWallpaper(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Persists wallpaper and custom visual settings in SharedPreferences.
     */
    fun saveVisualSettings(context: Context, settings: AppCustomSettings) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val listString = settings.customWallpaperList.joinToString(";")
            prefs.edit()
                .putBoolean(KEY_WALLPAPER_ENABLED, settings.isWallpaperEnabled)
                .putString(KEY_WALLPAPER_STYLE, settings.wallpaperStyle.name)
                .putFloat(KEY_WALLPAPER_DIM, settings.wallpaperDim)
                .putString(KEY_CUSTOM_WP_URI, settings.customWallpaperUri ?: "")
                .putString(KEY_CUSTOM_WP_LIST, listString)
                .putString(KEY_TEXT_ACCENT, settings.textColorAccent.name)
                .putString(KEY_THEME_PRESET, settings.themePreset.name)
                .putFloat(KEY_GLASS_BLUR, settings.glassBlurIntensity)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Loads persisted visual settings and merges with saved custom wallpapers.
     */
    fun loadVisualSettings(context: Context, defaultSettings: AppCustomSettings): AppCustomSettings {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean(KEY_WALLPAPER_ENABLED, defaultSettings.isWallpaperEnabled)
            val styleName = prefs.getString(KEY_WALLPAPER_STYLE, defaultSettings.wallpaperStyle.name)
            val style = try {
                WallpaperStyle.valueOf(styleName ?: defaultSettings.wallpaperStyle.name)
            } catch (e: Exception) {
                defaultSettings.wallpaperStyle
            }
            val dim = prefs.getFloat(KEY_WALLPAPER_DIM, defaultSettings.wallpaperDim)
            val customUri = prefs.getString(KEY_CUSTOM_WP_URI, null)?.takeIf { it.isNotBlank() }
            val listRaw = prefs.getString(KEY_CUSTOM_WP_LIST, "") ?: ""
            val list = if (listRaw.isNotBlank()) {
                listRaw.split(";").filter { it.isNotBlank() }
            } else {
                getSavedCustomWallpapers(context)
            }
            val accentName = prefs.getString(KEY_TEXT_ACCENT, defaultSettings.textColorAccent.name)
            val accent = try {
                TextColorAccent.valueOf(accentName ?: defaultSettings.textColorAccent.name)
            } catch (e: Exception) {
                defaultSettings.textColorAccent
            }
            val themeName = prefs.getString(KEY_THEME_PRESET, defaultSettings.themePreset.name)
            val theme = try {
                ThemePreset.valueOf(themeName ?: defaultSettings.themePreset.name)
            } catch (e: Exception) {
                defaultSettings.themePreset
            }
            val blur = prefs.getFloat(KEY_GLASS_BLUR, defaultSettings.glassBlurIntensity)

            defaultSettings.copy(
                isWallpaperEnabled = enabled,
                wallpaperStyle = style,
                wallpaperDim = dim,
                customWallpaperUri = customUri,
                customWallpaperList = list,
                textColorAccent = accent,
                themePreset = theme,
                glassBlurIntensity = blur
            )
        } catch (e: Exception) {
            defaultSettings
        }
    }

    private const val KEY_USER_ID = "key_user_id"
    private const val KEY_USER_NAME = "key_user_name"
    private const val KEY_USER_PHONE = "key_user_phone"
    private const val KEY_USER_CITY = "key_user_city"
    private const val KEY_USER_ROLE = "key_user_role"
    private const val KEY_USER_EMAIL = "key_user_email"
    private const val KEY_USER_STATUS = "key_user_status"
    private const val KEY_USER_PHOTO_URI = "key_user_photo_uri"
    private const val KEY_USER_IS_AUTH = "key_user_is_auth"

    fun saveUserProfile(context: Context, profile: com.example.model.UserProfile) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_USER_ID, profile.userId)
                .putString(KEY_USER_NAME, profile.userName)
                .putString(KEY_USER_PHONE, profile.phoneNumber)
                .putString(KEY_USER_CITY, profile.city)
                .putString(KEY_USER_ROLE, profile.role)
                .putString(KEY_USER_EMAIL, profile.email)
                .putString(KEY_USER_STATUS, profile.status)
                .putString(KEY_USER_PHOTO_URI, profile.profilePhotoUri ?: "")
                .putBoolean(KEY_USER_IS_AUTH, profile.isAuthenticated)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadUserProfile(context: Context, defaultProfile: com.example.model.UserProfile): com.example.model.UserProfile {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val id = prefs.getString(KEY_USER_ID, defaultProfile.userId) ?: defaultProfile.userId
            val name = prefs.getString(KEY_USER_NAME, defaultProfile.userName) ?: defaultProfile.userName
            val phone = prefs.getString(KEY_USER_PHONE, defaultProfile.phoneNumber) ?: defaultProfile.phoneNumber
            val city = prefs.getString(KEY_USER_CITY, defaultProfile.city) ?: defaultProfile.city
            val role = prefs.getString(KEY_USER_ROLE, defaultProfile.role) ?: defaultProfile.role
            val email = prefs.getString(KEY_USER_EMAIL, defaultProfile.email) ?: defaultProfile.email
            val status = prefs.getString(KEY_USER_STATUS, defaultProfile.status) ?: defaultProfile.status
            val photoUri = prefs.getString(KEY_USER_PHOTO_URI, null)?.takeIf { it.isNotBlank() }
            val isAuth = prefs.getBoolean(KEY_USER_IS_AUTH, false)

            defaultProfile.copy(
                userId = id,
                userName = name,
                phoneNumber = phone,
                city = city,
                role = role,
                email = email,
                status = status,
                profilePhotoUri = photoUri ?: defaultProfile.profilePhotoUri,
                isAuthenticated = isAuth
            )
        } catch (e: Exception) {
            defaultProfile
        }
    }

    private const val KEY_AI_PROVIDER = "key_ai_provider"
    private const val KEY_AI_GEMINI_KEY = "key_ai_gemini_key"
    private const val KEY_AI_OPENAI_KEY = "key_ai_openai_key"
    private const val KEY_AI_ZEN_KEY = "key_ai_zen_key"
    private const val KEY_AI_ENDPOINT = "key_ai_endpoint"
    private const val KEY_AI_MODEL = "key_ai_model"

    fun saveAiSettings(context: Context, settings: com.example.model.AiEngineSettings) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_AI_PROVIDER, settings.selectedProvider.name)
                .putString(KEY_AI_GEMINI_KEY, settings.geminiApiKey)
                .putString(KEY_AI_OPENAI_KEY, settings.openAiApiKey)
                .putString(KEY_AI_ZEN_KEY, settings.zenCloudApiKey)
                .putString(KEY_AI_ENDPOINT, settings.customEndpointUrl)
                .putString(KEY_AI_MODEL, settings.customModelName)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAiSettings(context: Context): com.example.model.AiEngineSettings {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val provName = prefs.getString(KEY_AI_PROVIDER, com.example.model.AiProvider.GEMINI.name)
            val provider = try {
                com.example.model.AiProvider.valueOf(provName ?: com.example.model.AiProvider.GEMINI.name)
            } catch (e: Exception) {
                com.example.model.AiProvider.GEMINI
            }
            val gemini = prefs.getString(KEY_AI_GEMINI_KEY, "") ?: ""
            val openai = prefs.getString(KEY_AI_OPENAI_KEY, "") ?: ""
            val zen = prefs.getString(KEY_AI_ZEN_KEY, "") ?: ""
            val endpoint = prefs.getString(KEY_AI_ENDPOINT, "https://openrouter.ai/api/v1/chat/completions") ?: "https://openrouter.ai/api/v1/chat/completions"
            val model = prefs.getString(KEY_AI_MODEL, "deepseek/deepseek-chat") ?: "deepseek/deepseek-chat"

            com.example.model.AiEngineSettings(
                selectedProvider = provider,
                geminiApiKey = gemini,
                openAiApiKey = openai,
                zenCloudApiKey = zen,
                customEndpointUrl = endpoint,
                customModelName = model
            )
        } catch (e: Exception) {
            com.example.model.AiEngineSettings()
        }
    }

    private const val KEY_ACTIVE_FORMULA_ID = "key_active_formula_id"
    private const val KEY_ACTIVE_FORMULA_NAME = "key_active_formula_name"
    private const val KEY_ACTIVE_FORMULA_MODE = "key_active_formula_mode"
    private const val KEY_ACTIVE_FORMULA_DIVISOR = "key_active_formula_divisor"
    private const val KEY_ACTIVE_FORMULA_MULTIPLIER = "key_active_formula_multiplier"
    private const val KEY_ACTIVE_FORMULA_OFFSET = "key_active_formula_offset"
    private const val KEY_ACTIVE_FORMULA_CUT = "key_active_formula_cut"
    private const val KEY_ACTIVE_FORMULA_OTC_COUNT = "key_active_formula_otc_count"
    private const val KEY_SAVED_FORMULAS_JSON = "key_saved_formulas_json"

    fun saveFormulaSettings(
        context: Context,
        activeFormula: com.example.model.FormulaConfig,
        savedFormulas: List<com.example.model.FormulaConfig>
    ) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
                .putString(KEY_ACTIVE_FORMULA_ID, activeFormula.id)
                .putString(KEY_ACTIVE_FORMULA_NAME, activeFormula.name)
                .putString(KEY_ACTIVE_FORMULA_MODE, activeFormula.mode.name)
                .putInt(KEY_ACTIVE_FORMULA_DIVISOR, activeFormula.divisor)
                .putInt(KEY_ACTIVE_FORMULA_MULTIPLIER, activeFormula.multiplierFactor)
                .putInt(KEY_ACTIVE_FORMULA_OFFSET, activeFormula.additionOffset)
                .putBoolean(KEY_ACTIVE_FORMULA_CUT, activeFormula.includeCutDigits)
                .putInt(KEY_ACTIVE_FORMULA_OTC_COUNT, activeFormula.targetOtcCount)

            // Serialize saved formulas to simple format: id|name|mode|divisor|multiplier|offset|cut|count
            val serializedList = savedFormulas.map { f ->
                "${f.id}::${f.name}::${f.mode.name}::${f.divisor}::${f.multiplierFactor}::${f.additionOffset}::${f.includeCutDigits}::${f.targetOtcCount}::${f.customNotes}"
            }.joinToString("###")

            editor.putString(KEY_SAVED_FORMULAS_JSON, serializedList)
            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadFormulaSettings(context: Context): Pair<com.example.model.FormulaConfig, List<com.example.model.FormulaConfig>> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val id = prefs.getString(KEY_ACTIVE_FORMULA_ID, "a23_classic") ?: "a23_classic"
            val name = prefs.getString(KEY_ACTIVE_FORMULA_NAME, "A23 MAX Standard Formula") ?: "A23 MAX Standard Formula"
            val modeStr = prefs.getString(KEY_ACTIVE_FORMULA_MODE, com.example.model.FormulaEngineMode.A23_CLASSIC.name)
            val mode = try {
                com.example.model.FormulaEngineMode.valueOf(modeStr ?: com.example.model.FormulaEngineMode.A23_CLASSIC.name)
            } catch (e: Exception) {
                com.example.model.FormulaEngineMode.A23_CLASSIC
            }
            val divisor = prefs.getInt(KEY_ACTIVE_FORMULA_DIVISOR, 9)
            val multiplier = prefs.getInt(KEY_ACTIVE_FORMULA_MULTIPLIER, 1)
            val offset = prefs.getInt(KEY_ACTIVE_FORMULA_OFFSET, 0)
            val cut = prefs.getBoolean(KEY_ACTIVE_FORMULA_CUT, false)
            val count = prefs.getInt(KEY_ACTIVE_FORMULA_OTC_COUNT, 4)

            val active = com.example.model.FormulaConfig(
                id = id,
                name = name,
                mode = mode,
                divisor = divisor,
                multiplierFactor = multiplier,
                additionOffset = offset,
                includeCutDigits = cut,
                targetOtcCount = count,
                isCustom = id.startsWith("custom_")
            )

            val savedJson = prefs.getString(KEY_SAVED_FORMULAS_JSON, "") ?: ""
            val savedList = mutableListOf<com.example.model.FormulaConfig>()
            if (savedJson.isNotBlank()) {
                val items = savedJson.split("###")
                for (item in items) {
                    val parts = item.split("::")
                    if (parts.size >= 8) {
                        try {
                            val fMode = try {
                                com.example.model.FormulaEngineMode.valueOf(parts[2])
                            } catch (e: Exception) {
                                com.example.model.FormulaEngineMode.A23_CLASSIC
                            }
                            savedList.add(
                                com.example.model.FormulaConfig(
                                    id = parts[0],
                                    name = parts[1],
                                    mode = fMode,
                                    divisor = parts[3].toIntOrNull() ?: 9,
                                    multiplierFactor = parts[4].toIntOrNull() ?: 1,
                                    additionOffset = parts[5].toIntOrNull() ?: 0,
                                    includeCutDigits = parts[6].toBooleanStrictOrNull() ?: false,
                                    targetOtcCount = parts[7].toIntOrNull() ?: 4,
                                    customNotes = if (parts.size > 8) parts[8] else "",
                                    isCustom = true
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            Pair(active, savedList)
        } catch (e: Exception) {
            Pair(com.example.model.FormulaConfig(), emptyList())
        }
    }
}
