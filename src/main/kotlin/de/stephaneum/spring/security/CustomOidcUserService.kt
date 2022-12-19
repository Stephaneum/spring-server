package de.stephaneum.spring.security

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.CodeService
import de.stephaneum.spring.helper.LogService
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

@Service
class CustomOidcUserService (
    private val userRepo: UserRepo,
    private val codeRepo: CodeRepo,
    private val codeService: CodeService,
    private val logService: LogService,
) : OidcUserService() {

    @Override
    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = super.loadUser(userRequest)
        processOidcUser(oidcUser)
        return oidcUser
    }

    private fun processOidcUser(oidcUser: OidcUser) {
        var user = userRepo.findByEmail(oidcUser.email)
        if (user == null) {
            // register new user
            val code = codeRepo.save(codeService.generateCode(ROLE_STUDENT))
            user = userRepo.save(User(
                code = code,
                firstName = oidcUser.givenName,
                lastName = oidcUser.familyName,
                email = oidcUser.email,
                openIdSubject = oidcUser.subject,
                openId = true,
            ))
            logService.log(EventType.REGISTER, user, "Open ID Login")
        } else {
            // do nothing, just take the user with the same email
            logService.log(EventType.LOGIN, user, "Open ID Login")
        }
        Session.get().user = user
    }
}
