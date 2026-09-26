package com.example.util

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log

object JarvisAppLauncher {

    private const val TAG = "JarvisAppLauncher"

    data class LaunchResult(
        val success: Boolean,
        val jarvisResponse: String
    )

    fun handleAppOrWebDirective(context: Context, input: String): LaunchResult? {
        val original = input.trim()
        val lower = original.lowercase()

        // 1. Camera
        if (
            lower.contains("open camera") ||
            lower.contains("launch camera") ||
            lower.contains("take a picture") ||
            lower == "camera"
        ) {
            return launchCamera(context)
        }

        // 2. Settings
        if (
            lower.contains("open settings") ||
            lower.contains("go to settings") ||
            lower.contains("device settings") ||
            lower.contains("system settings") ||
            lower == "settings"
        ) {
            return launchSettings(context)
        }

        // 3. YouTube directives (Search, Channel, Play, Click, Watch, Go to)
        if (lower.contains("youtube") || lower.contains("yt ")) {
            val cleanTarget = extractYouTubeTarget(original)
            return if (cleanTarget.isBlank()) {
                launchYouTube(context)
            } else {
                openYouTubeSearch(context, cleanTarget)
            }
        }

        // 5. Google Web Search
        if (
            lower.contains("search google") ||
            lower.startsWith("search for ") ||
            lower.startsWith("google ")
        ) {
            val query = extractSearchQuery(
                original,
                listOf(
                    "search google for",
                    "search google",
                    "search for",
                    "google search",
                    "google"
                )
            )
            return if (query.isNotBlank()) {
                val url = "https://www.google.com/search?q=" + Uri.encode(query)
                openUrl(context, url)
                LaunchResult(true, "Searching Google for \"$query\", Sir.")
            } else {
                LaunchResult(true, "What shall I search for, Sir?")
            }
        }

        // 6. Chrome / Web Browser
        if (
            lower.contains("open chrome") ||
            lower.contains("go to chrome") ||
            lower.contains("launch chrome") ||
            lower.contains("open browser") ||
            lower.contains("open internet")
        ) {
            return launchChrome(context)
        }

        // 7. Phone / Dialer
        if (
            lower.contains("open phone") ||
            lower.contains("open dialer") ||
            lower.contains("open dial pad") ||
            lower == "phone" ||
            lower == "dialer"
        ) {
            return launchPhoneDialer(context)
        }

        // 8. Google Maps
        if (
            lower.contains("open maps") ||
            lower.contains("open google maps") ||
            lower.contains("navigation") ||
            lower == "maps"
        ) {
            return launchMaps(context)
        }

        // 9. WhatsApp
        if (lower.contains("open whatsapp") || lower == "whatsapp") {
            return launchWhatsApp(context)
        }

        // 10. Calculator
        if (
            lower.contains("open calculator") ||
            lower.contains("launch calculator") ||
            lower == "calculator"
        ) {
            return launchCalculator(context)
        }

        // 11. Open specific website or URL
        if (lower.startsWith("open website") || lower.startsWith("open web page")) {
            val domain = lower.removePrefix("open website").removePrefix("open web page").trim()
            return if (domain.isNotBlank()) {
                launchWebsite(context, domain)
            } else {
                LaunchResult(true, "Please tell me the website address or domain you wish to open, Sir.")
            }
        }

        if (lower.startsWith("open ") || lower.startsWith("go to ") || lower.startsWith("navigate to ")) {
            val target = lower.removePrefix("open ").removePrefix("go to ").removePrefix("navigate to ").trim()

            // Domain/URL check
            if (target.contains(".") || target.startsWith("http://") || target.startsWith("https://")) {
                return launchWebsite(context, target)
            }

            // Check if target matches any installed app
            val appResult = launchAppByName(context, target)
            if (appResult != null) {
                return appResult
            }
        }

        // 12. "What can you do" / capabilities query
        if (
            lower.contains("what can you do") ||
            lower.contains("what are your capabilities") ||
            lower == "help" ||
            lower == "commands"
        ) {
            return LaunchResult(
                true,
                "I can open applications, search YouTube and Google, activate optical camera sensors, access phone dialer, system settings, WhatsApp, and Google Maps. What is your directive, Sir?"
            )
        }

        return null
    }

    private fun extractYouTubeTarget(command: String): String {
        var text = command.trim()

        val removePatterns = listOf(
            "go to the youtube and click the",
            "go to the youtube and click",
            "go to the youtube and open the",
            "go to the youtube and open",
            "go to the youtube and search for",
            "go to the youtube and search",
            "go to the youtube and find",
            "go to the youtube and play",
            "go to the youtube and watch",
            "go to the youtube and",
            "go to youtube and click the",
            "go to youtube and click",
            "go to youtube and open the",
            "go to youtube and open",
            "go to youtube and search for",
            "go to youtube and search",
            "go to youtube and find",
            "go to youtube and play",
            "go to youtube and watch",
            "go to youtube and",
            "open the youtube and",
            "open youtube and",
            "search on youtube for",
            "search youtube for",
            "search on youtube",
            "search youtube",
            "play on youtube",
            "watch on youtube",
            "find on youtube",
            "click on youtube",
            "open on youtube",
            "go to the youtube",
            "go to youtube",
            "open the youtube",
            "open youtube",
            "on youtube",
            "youtube search for",
            "youtube search",
            "youtube pe",
            "youtube par",
            "youtube"
        )

        for (pattern in removePatterns) {
            text = text.replace(pattern, " ", ignoreCase = true)
        }

        // Clean up action filler words
        text = text.replace(Regex("(?i)\\bclick the\\b"), " ")
        text = text.replace(Regex("(?i)\\bclick\\b"), " ")
        text = text.replace(Regex("(?i)\\bopen the\\b"), " ")
        text = text.replace(Regex("(?i)\\bshow me\\b"), " ")
        text = text.replace(Regex("(?i)\\bplay\\b"), " ")
        text = text.replace(Regex("(?i)\\band channel\\b"), " channel ")
        text = text.replace(Regex("(?i)\\band\\b"), " ")

        return text.trim().replace(Regex("\\s+"), " ")
    }

    private fun openYouTubeSearch(context: Context, query: String): LaunchResult {
        val cleanQuery = query.trim()
        val uri = Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(cleanQuery))

        // Direct YouTube app intent
        val appIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.youtube")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(appIntent)
            LaunchResult(true, "Opening YouTube and navigating to \"$cleanQuery\", Sir.")
        } catch (_: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                LaunchResult(true, "Navigating to \"$cleanQuery\" on YouTube, Sir.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch YouTube for $cleanQuery", e)
                LaunchResult(false, "Unable to access YouTube at this moment, Sir.")
            }
        }
    }

    private fun extractSearchQuery(command: String, phrases: List<String>): String {
        var result = command
        for (phrase in phrases) {
            result = result.replace(phrase, "", ignoreCase = true)
        }
        return result.trim().replace(Regex("\\s+"), " ")
    }

    private fun launchCamera(context: Context): LaunchResult {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            LaunchResult(true, "Activating optical sensors and camera systems, Sir.")
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                LaunchResult(true, "Opening camera, Sir.")
            } catch (ex: ActivityNotFoundException) {
                Log.e(TAG, "Camera not found", ex)
                LaunchResult(false, "Optical sensors are currently inaccessible on this device, Sir.")
            }
        }
    }

    private fun launchSettings(context: Context): LaunchResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            LaunchResult(true, "Opening device system configuration, Sir.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch settings", e)
            LaunchResult(false, "Unable to access device settings console, Sir.")
        }
    }

    private fun launchPhoneDialer(context: Context): LaunchResult {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            LaunchResult(true, "Opening communications phone dialer, Sir.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch dialer", e)
            LaunchResult(false, "Communications dialer is not accessible, Sir.")
        }
    }

    private fun launchYouTube(context: Context): LaunchResult {
        return try {
            val pkgIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
            if (pkgIntent != null) {
                pkgIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(pkgIntent)
                LaunchResult(true, "Accessing YouTube terminal right away, Sir.")
            } else {
                openUrl(context, "https://www.youtube.com/")
                LaunchResult(true, "Opening YouTube, Sir.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch YouTube", e)
            openUrl(context, "https://www.youtube.com/")
            LaunchResult(true, "Opening YouTube, Sir.")
        }
    }

    private fun launchChrome(context: Context): LaunchResult {
        return try {
            val chromeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/")).apply {
                setPackage("com.android.chrome")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chromeIntent)
            LaunchResult(true, "Chrome browser online, Sir.")
        } catch (e: Exception) {
            openUrl(context, "https://www.google.com/")
            LaunchResult(true, "Launching browser interface, Sir.")
        }
    }

    private fun launchWhatsApp(context: Context): LaunchResult {
        return try {
            val pkgIntent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
            if (pkgIntent != null) {
                pkgIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(pkgIntent)
                LaunchResult(true, "Opening WhatsApp communications channel, Sir.")
            } else {
                openUrl(context, "https://web.whatsapp.com/")
                LaunchResult(true, "Opening WhatsApp web interface, Sir.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch WhatsApp", e)
            LaunchResult(false, "WhatsApp is not available, Sir.")
        }
    }

    private fun launchMaps(context: Context): LaunchResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            LaunchResult(true, "Opening Google Maps navigation, Sir.")
        } catch (e: Exception) {
            openUrl(context, "https://maps.google.com/")
            LaunchResult(true, "Opening Google Maps, Sir.")
        }
    }

    private fun launchCalculator(context: Context): LaunchResult {
        val calculatorPackages = listOf(
            "com.google.android.calculator",
            "com.android.calculator2",
            "com.sec.android.app.popupcalculator"
        )
        for (pkg in calculatorPackages) {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    return LaunchResult(true, "Computational calculator array activated, Sir.")
                }
            } catch (_: Exception) {}
        }
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            LaunchResult(true, "Opening calculator, Sir.")
        } catch (e: Exception) {
            LaunchResult(false, "Calculator unit is not available on this device, Sir.")
        }
    }

    private fun launchWebsite(context: Context, rawUrl: String): LaunchResult {
        val formattedUrl = when {
            rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> rawUrl
            else -> "https://$rawUrl"
        }
        return try {
            openUrl(context, formattedUrl)
            LaunchResult(true, "Navigating to $rawUrl, Sir.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch website: $formattedUrl", e)
            LaunchResult(false, "Unable to establish uplink to $rawUrl, Sir.")
        }
    }

    private fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun launchAppByName(context: Context, appName: String): LaunchResult? {
        val pm = context.packageManager
        val cleanName = appName.lowercase().trim()

        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val apps = pm.queryIntentActivities(mainIntent, 0)

            for (resolveInfo in apps) {
                val label = resolveInfo.loadLabel(pm).toString().lowercase()
                if (label == cleanName || label.contains(cleanName)) {
                    val launchIntent = pm.getLaunchIntentForPackage(resolveInfo.activityInfo.packageName)
                    if (launchIntent != null) {
                        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(launchIntent)
                        val officialName = resolveInfo.loadLabel(pm).toString()
                        return LaunchResult(true, "Opening $officialName right away, Sir.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up app $appName", e)
        }
        return null
    }
}
