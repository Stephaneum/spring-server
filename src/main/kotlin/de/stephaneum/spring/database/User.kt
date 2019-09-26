package de.stephaneum.spring.database

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import javax.persistence.*

@Entity
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
                var gender: Int? = null,

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
                var createPosts: Boolean? = false,

                @Column(nullable = true, name = "rubrik_erstellen")
                var createCategories: Boolean? = false,

                @Column(nullable = true, name = "vertretungsplan")
                var managePlans: Boolean? = false,

                @Column(nullable = true, name = "lehrerchat_datum")
                var teacherChatLastOnline: Timestamp = Timestamp(0))

@Repository
interface UserRepo: CrudRepository<User, Int>