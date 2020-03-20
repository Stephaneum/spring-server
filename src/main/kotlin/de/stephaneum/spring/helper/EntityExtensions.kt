package de.stephaneum.spring.helper

import de.stephaneum.spring.database.Folder
import de.stephaneum.spring.database.Group
import de.stephaneum.spring.database.SimpleUser
import de.stephaneum.spring.database.User

/**
 * @return entity with id
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
inline fun <reified T> Int?.obj(): T {
    val id = this ?: 0 // defaulting to zero, if null
    return when(T::class) {
        Group::class -> Group(id)
        User::class -> User(id)
        Folder::class -> Folder(id)
        else -> throw IllegalArgumentException("Invalid Type")
    } as T
}

fun User.toSimpleUser(): SimpleUser {
    return SimpleUser(id, firstName, lastName, schoolClass?.let { schoolClass -> schoolClass.grade.toString() + schoolClass.suffix }, gender, code.role)
}