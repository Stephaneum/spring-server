package de.stephaneum.spring.helper

import org.springframework.stereotype.Service

enum class Browser (val repr: String) {
    FIREFOX("Firefox"),
    CHROME("Google Chrome"),
    EDGE("Edge"),
    SAFARI("Safari"),
    OPERA("Opera"),
    SAMSUNG("Samsung Internet"),
    OTHER("andere")
}

enum class OS (val repr: String) {
    WINDOWS("Windows"),
    MAC("Mac OS"),
    LINUX("Linux"),
    IOS("iOS"),
    ANDROID("Android"),
    OTHER("andere")
}

@Service
class UserAgentDetector {

    /**
     * Crawler words, matched at the end of a token so that compound names like "googlebot" or
     * "bytespider" are caught. Device names such as "CUBOT_X30" are excluded by the trailing class.
     */
    private val botWords = Regex("""(bot|crawler|spider|slurp|scraper|archiver|indexer)([^a-z0-9_]|$)""")

    /** Non-browser HTTP clients, unambiguous enough for plain substring matching. */
    private val botClients = listOf("curl/", "wget", "python-requests", "python-urllib", "go-http-client",
            "java/", "okhttp", "libwww-perl", "apache-httpclient", "headlesschrome", "phantomjs")

    /** Agents without a generic crawler word, plus the browsers we have always filtered out as spam. */
    private val botNames = listOf("facebookexternalhit", "meta-externalagent", "lyncautodiscover",
            "liebaofast", "mb2345browser")

    /**
     * userAgent must be in lowercase beforehand!
     *
     * Order matters: every Chromium derivative also carries a "chrome" token, and the iOS builds of
     * Chrome and Firefox carry Safari's engine tokens instead of their own name.
     */
    fun getBrowser(userAgent: String): Browser {
        return when {
            userAgent.contains("edg/") || userAgent.contains("edga/") ||
                    userAgent.contains("edgios/") || userAgent.contains("edge/") -> Browser.EDGE
            userAgent.contains("opr/") || userAgent.contains("opios/") || userAgent.contains("opera") -> Browser.OPERA
            userAgent.contains("samsungbrowser") -> Browser.SAMSUNG
            userAgent.contains("fxios/") || userAgent.contains("firefox") -> Browser.FIREFOX
            userAgent.contains("crios/") || userAgent.contains("chrome") || userAgent.contains("chromium") -> Browser.CHROME
            userAgent.contains("safari") || userAgent.contains("applewebkit") -> Browser.SAFARI
            else -> Browser.OTHER
        }
    }

    /**
     * userAgent must be in lowercase beforehand!
     *
     * Order matters: Android carries a "linux" token and iOS carries "like mac os x".
     *
     * Windows versions are not distinguished any more: Windows 11 still reports "Windows NT 10.0",
     * and Chrome freezes the platform string regardless of the actual version. An iPad in its
     * default desktop mode is indistinguishable from a Mac here and counts as [MAC].
     */
    fun getOS(userAgent: String): OS {
        return when {
            userAgent.contains("windows") -> OS.WINDOWS
            userAgent.contains("android") -> OS.ANDROID
            userAgent.contains("iphone") || userAgent.contains("ipad") || userAgent.contains("ipod") -> OS.IOS
            userAgent.contains("macintosh") || userAgent.contains("mac os") -> OS.MAC
            userAgent.contains("linux") || userAgent.contains("x11") -> OS.LINUX
            else -> OS.OTHER
        }
    }

    /**
     * userAgent must be in lowercase beforehand!
     *
     * An iPad in its default desktop mode reports as a Mac and is not detected as mobile.
     */
    fun isMobile(userAgent: String): Boolean {
        return userAgent.contains("android") ||
                userAgent.contains("iphone") ||
                userAgent.contains("ipod") ||
                userAgent.contains("phone") || // windows phone
                userAgent.contains("blackberry") ||
                userAgent.contains("mobile") // andere
    }

    /**
     * userAgent must be in lowercase beforehand!
     *
     * Heuristic rather than a list of known crawlers: new ones appear constantly and a missed one
     * is counted as a real visit. A missing user agent is treated as a bot, since browsers always
     * send one and scripted requests often do not.
     */
    fun isBot(userAgent: String): Boolean {
        if (userAgent.isBlank())
            return true

        return botWords.containsMatchIn(userAgent) ||
                botClients.any { userAgent.contains(it) } ||
                botNames.any { userAgent.contains(it) }
    }
}
