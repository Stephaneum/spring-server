package de.stephaneum.spring.security

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.CodeRepo
import de.stephaneum.spring.database.ROLE_STUDENT
import de.stephaneum.spring.database.User
import de.stephaneum.spring.database.UserRepo
import de.stephaneum.spring.helper.CodeService
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

@Service
public class CustomOidcUserService (
    private val userRepo: UserRepo,
    private val codeRepo: CodeRepo,
    private val codeService: CodeService,
) : OidcUserService() {


    @Override
    public override fun loadUser(userRequest: OidcUserRequest): OidcUser {

        var oidcUser = super.loadUser(userRequest);

        processOidcUser(userRequest, oidcUser);

        return oidcUser;
    }

    private fun processOidcUser(userRequest: OidcUserRequest, oidcUser: OidcUser) {

        // create user in database
        if (!userRepo.existsBySub(oidcUser.subject)) {

            // generare new code
            //TODO: get right role!
            var code = codeRepo.save(
                codeService.generateCode(ROLE_STUDENT))



            val user = userRepo.save(User(
                code = code,
                firstName = oidcUser.givenName,
                lastName = oidcUser.familyName,
                email = oidcUser.email,
                sub = oidcUser.subject,
                isOidc = true,
            ))
            Session.get().user = user
        } else {
            var user = userRepo.findBySub(oidcUser.subject)
            Session.get().user = user
        }

    }

}