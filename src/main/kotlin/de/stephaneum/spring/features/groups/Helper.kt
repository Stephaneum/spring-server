package de.stephaneum.spring.features.groups

import de.stephaneum.spring.database.SimpleUser

data class CreateGroup(val name: String?, val teachers: List<Int>?)

data class GroupInfo(val id: Int, val name: String, val leader: SimpleUser, val accepted: Boolean, val chat: Boolean, val members: Int)
data class GroupInfoDetailed(val id: Int, val name: String, val leader: SimpleUser, val accepted: Boolean, val chat: Boolean, val members: List<SimpleUser>, val teachers: List<SimpleUser>)