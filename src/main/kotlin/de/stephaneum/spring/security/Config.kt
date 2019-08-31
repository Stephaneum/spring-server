package de.stephaneum.spring.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import javax.servlet.FilterChain
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest
import org.springframework.web.filter.GenericFilterBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import javax.servlet.http.HttpServletRequest
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletResponse
import javax.annotation.PostConstruct


@Configuration
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var redirectHTTPSFiler: RedirectHTTPSFiler

    @Autowired
    private lateinit var corsFilter: CorsFilter

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
        http.authorizeRequests().anyRequest().permitAll()

        http.addFilterBefore(redirectHTTPSFiler, BasicAuthenticationFilter::class.java)
        http.addFilterAfter(corsFilter, BasicAuthenticationFilter::class.java)
    }
}

@Component
class RedirectHTTPSFiler: GenericFilterBean() {

    // redirect http requests to https

    @Value("\${security.redirect.https}")
    private lateinit var redirectHTTPS: String

    override fun doFilter(req: ServletRequest, res: ServletResponse, filterChain: FilterChain) {

        if(redirectHTTPS != "true") {
            filterChain.doFilter(req, res)
            return
        }

        val httpRequest = req as HttpServletRequest
        val httpResponse = res as HttpServletResponse

        val uri = httpRequest.requestURI
        val protocol = httpRequest.scheme
        val domain = httpRequest.serverName

        if (protocol.toLowerCase() == "http") {
            httpResponse.sendRedirect("https://$domain$uri")
        } else {
            filterChain.doFilter(req, res)
        }
    }
}

@Component
class CorsFilter: GenericFilterBean() {

    // add cors headers

    @Value("\${security.cors.origins}")
    private lateinit var originsRaw: String

    lateinit var origins: List<String>

    @PostConstruct
    fun init() {
        val origins = mutableListOf<String>()
        originsRaw.split(",").forEach { origins.add(it.trim()) }
        this.origins = origins
    }

    override fun doFilter(req: ServletRequest, res: ServletResponse, filterChain: FilterChain?) {

        val httpResponse = res as HttpServletResponse

        if(httpResponse.containsHeader("Access-Control-Allow-Origin")) {
            // cors filter seems to be already called
            filterChain?.doFilter(req, res)
            return
        }

        // check origin
        val httpRequest = req as HttpServletRequest
        val origin = httpRequest.getHeader("Origin").takeIf { origin ->
            origins.any { s -> origin == s }
        }

        if(origin != null) {
            httpResponse.addHeader("Access-Control-Allow-Origin", origin)
            httpResponse.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS")
            httpResponse.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
        }
        filterChain?.doFilter(req, res)
    }
}