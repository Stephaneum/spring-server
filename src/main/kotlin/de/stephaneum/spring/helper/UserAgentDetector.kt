package de.stephaneum.spring.helper

import org.springframework.stereotype.Service

enum class Browser (val repr: String) {
    FIREFOX("Firefox"),
    CHROME("Google Chrome"),
    IE("Internet Explorer"),
    EDGE("Edge"),
    SAFARI("Safari"),
    OTHER("andere")
}

enum class OS (val repr: String) {
    WINDOWS_XP("Windows XP"),
    WINDOWS_7("Windows 7"),
    WINDOWS_10("Windows 10"),
    MAC("Mac OS"),
    LINUX("Linux"),
    IOS("iOS"),
    ANDROID("Android"),
    OTHER("andere")
}

@Service
class UserAgentDetector {

    /**
     * userAgent must be in lowercase beforehand!
     */
    fun getBrowser(userAgent: String): Browser {
        return when {
            userAgent.contains("edge") -> Browser.EDGE
            userAgent.contains("firefox") -> Browser.FIREFOX
            userAgent.contains("chrome") -> Browser.CHROME
            userAgent.contains("msie") || userAgent.contains("trident") -> Browser.IE
            userAgent.contains("safari") || userAgent.contains("applewebkit") -> Browser.SAFARI
            else -> Browser.OTHER
        }
    }

    fun getOS(userAgent: String): OS {
        return when {
            userAgent.contains("windows nt 5.1") || userAgent.contains("windows nt 5.2") -> OS.WINDOWS_XP
            userAgent.contains("windows nt 6.1") -> OS.WINDOWS_7
            userAgent.contains("windows nt 10.0") -> OS.WINDOWS_10
            userAgent.contains("macintosh") -> OS.MAC
            userAgent.contains("android") -> OS.ANDROID
            userAgent.contains("linux") -> OS.LINUX
            userAgent.contains("iphone") || userAgent.contains("ipad") -> OS.IOS
            else -> OS.OTHER
        }
    }

    fun isMobile(userAgent: String): Boolean {
        return ((userAgent.contains("android") ||
                userAgent.contains("iphone") ||
                userAgent.contains("phone") ||  //windows phone
                userAgent.contains("blackberry") ||
                userAgent.contains("mobile")) //andere
                && !userAgent.contains("ipad"))
    }

    fun isBot(userAgent: String): Boolean {
        return userAgent.contains("liebaofast") ||
                userAgent.contains("mb2345browser") ||
                userAgent.contains("googlebot") ||
                userAgent.contains("bingbot") ||
                userAgent.contains("dotbot") ||
                userAgent.contains("mj12bot") ||
                userAgent.contains("slurp") ||
                userAgent.contains("baiduspider") ||
                userAgent.contains("yandexbot") ||
                userAgent.contains("xovibot") ||
                userAgent.contains("ahrefsbot") ||
                userAgent.contains("semrushbot") ||
                userAgent.contains("lyncautodiscover") ||
                userAgent.contains("zoominfobot")
    }
}