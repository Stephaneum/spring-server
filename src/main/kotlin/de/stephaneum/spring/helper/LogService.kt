package de.stephaneum.spring.helper

import de.stephaneum.spring.database.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LogService {

    @Autowired
    private lateinit var logRepo: LogRepo

    fun log(type: EventType, user: User, info: String = "") {
        logRepo.save(Log(0, now(), type.id, "${user.firstName} ${user.lastName} (${user.code.getRoleString()}), $info"))
    }

    fun log(type: EventType, subType: String, user: User, info: String = "") {
        logRepo.save(Log(0, now(), type.id, "[$subType] ${user.firstName} ${user.lastName} (${user.code.getRoleString()}), $info"))
    }

    fun log(type: EventType, user: List<User>, info: String = "") {
        logRepo.saveAll(user.map { u -> Log(0, now(), type.id, "${u.firstName} ${u.lastName} (${u.code.getRoleString()}), $info") })
    }

    fun log(type: EventType, user: List<User>, info: List<String>) {
        assert(user.size == info.size)
        logRepo.saveAll(user.mapIndexed { i, u -> Log(0, now(), type.id, "${u.firstName} ${u.lastName} (${u.code.getRoleString()}), ${info[i]}") })
    }
}