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
@Table(name="nutzer")
data class User(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                var id: Int = 0,

                @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                var code: Code = Code(),

                @Column(nullable = false, name = "vorname", length = 100)
                var firstName: String = "",

                @Column(nullable = false, name = "nachname", length = 100)
                var lastName: String = "",

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.NO_ACTION)
                @JoinColumn(name = "klasse_id")
                var schoolClass: SchoolClass? = null,

                @Column(nullable = true, name = "geschlecht", length = 4)
                var gender: Int? = SEX_UNKNOWN,

                @Column(nullable = false, length = 100)
                var email: String = "",

                @Column(nullable = false, name = "passwort", length = 255)
                var password: String = "",

                @Column(nullable = true, name = "code_passwort_vergessen", length = 32)
                var passwordForgotCode: String? = null,

                @Column(nullable = false)
                var storage: Int = 0,

                @Column(nullable = true, name = "gesperrt")
                var banned: Boolean? = false,

                @Column(nullable = true, name = "beitrag_manager")
                var managePosts: Boolean? = false,

                @Column(nullable = true, name = "projekt_erstellen")
                var createProjects: Boolean? = false,

                @Column(nullable = true, name = "rubrik_erstellen")
                var createCategories: Boolean? = false,

                @Column(nullable = true, name = "vertretungsplan")
                var managePlans: Boolean? = false,

                // used for teacher chat in the past, now it is a generic "last online" stamp
                @Column(nullable = true, name = "lehrerchat_datum")
                var teacherChatLastOnline: Timestamp = Timestamp(0))

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
    fun findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCaseOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(firstName: String, lastName: String): List<User>
    fun findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCaseAndCodeRoleOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(firstName: String, lastName: String, role: Int): List<User>
}