package de.stephaneum.spring.features.general

import com.fasterxml.jackson.annotation.JsonInclude
import de.stephaneum.spring.database.User

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Info(val user: User, val copyright: String?, val plan: Plan, val unapproved: Int?)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Plan(val exists: Boolean, val info: String?)