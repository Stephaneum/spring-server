package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import javax.persistence.*

val EMPTY_USER = User(-1, Code(-1, "", ROLE_NO_LOGIN))

const val SEX_MALE = 0
const val SEX_FEMALE = 1
const val SEX_UNKNOWN = 2

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class User(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                var id: Int = 0,

                @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                var code: Code = Code(),

                @Column(nullable = false, length = 100)
                var firstName: String = "",

                @Column(nullable = false, length = 100)
                var lastName: String = "",

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.NO_ACTION)
                var schoolClass: SchoolClass? = null,

                @Column(nullable = false, length = 4)
                var gender: Int = SEX_UNKNOWN,

                @Column(nullable = false, length = 100)
                var email: String = "",

                @Column(nullable = false, length = 255)
                var password: String = "",

                @Column(nullable = true, length = 32)
                var passwordForgotCode: String? = null,

                @Column(nullable = false)
                var storage: Int = 0,

                @Column(nullable = false)
                var banned: Boolean = false,

                @Column(nullable = true)
                var managePosts: Boolean = false,

                // TODO: rename this
                @Column(nullable = true, name = "create_groups")
                var createProjects: Boolean = false,

                @Column(nullable = true)
                var managePlans: Boolean = false,

                // used for teacher chat in the past, now it is a generic "last online" stamp
                // TODO: implement this
                @Column(nullable = true, name = "last_online")
                var lastOnline: Timestamp? = null)

data class SimpleUser(val id: Int, val firstName: String, val lastName: String, val email: String, val schoolClass: String?, val gender: Int, val storage: Int, val role: Int, val banned: Boolean)
data class MiniUser(val id: Int, val firstName: String, val lastName: String, val schoolClass: String?, val gender: Int?, val role: Int)

@Repository
interface UserRepo: CrudRepository<User, Int> {

    fun findByIdIn(id: List<Int>): List<User>
    fun findByEmail(email: String): User?
    fun findByCodeRole(role: Int): List<User>
    fun findBySchoolClassGrade(grade: Int): List<User>
    fun existsByEmail(email: String): Boolean

    fun countByCodeRoleAndCodeUsed(role: Int, used: Boolean): Int

    // search
    fun findByOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(): List<User>
    fun findByCodeRoleOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(role: Int): List<User>
    fun findBySchoolClassGradeAndSchoolClassSuffixContainingIgnoreCaseOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(grade: Int, suffix: String): List<User>
    fun findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCaseOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(firstName: String, lastName: String): List<User>
    fun findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCaseAndCodeRoleOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(firstName: String, lastName: String, role: Int): List<User>
}