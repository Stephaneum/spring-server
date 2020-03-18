package de.stephaneum.spring.helper

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.DispatcherServlet
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest

class ErrorCode(code: Int, message: String) : ResponseStatusException(when(code) {
    400 -> HttpStatus.BAD_REQUEST
    403 -> HttpStatus.FORBIDDEN
    404 -> HttpStatus.NOT_FOUND
    409 -> HttpStatus.CONFLICT
    410 -> HttpStatus.GONE
    412 -> HttpStatus.PRECONDITION_FAILED
    417 -> HttpStatus.EXPECTATION_FAILED
    418 -> HttpStatus.I_AM_A_TEAPOT
    423 -> HttpStatus.LOCKED
    500 -> HttpStatus.INTERNAL_SERVER_ERROR
    else -> HttpStatus.INTERNAL_SERVER_ERROR
}, message)

@RestController
class ErrorResponse : ErrorController {

    // 401 Unauthorized (jwt errors) are handled in security.Config

    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest): Response.Feedback {
        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)?.toInt() ?: 500
        var message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE)?.toString()
        if(message.isNullOrBlank())
            message = request.getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE)?.toString()

        if(status == 404 && message.isNullOrEmpty())
            message = "Not Found"

        return Response.Feedback(success = false, message = message)
    }

    override fun getErrorPath(): String {
        return "/error"
    }

    private fun Any.toInt(): Int {
        return this as Int
    }

}