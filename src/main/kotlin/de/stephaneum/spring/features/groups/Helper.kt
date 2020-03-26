package de.stephaneum.spring.features.groups

import de.stephaneum.spring.database.SimpleUser

data class UpdateGroup(val name: String?, val teachers: List<Int>?, val parent: Int?, val members: List<Int>?)

data class GroupInfo(val id: Int, val name: String, val leader: SimpleUser, val accepted: Boolean, val chat: Boolean, val members: Int)
data class GroupUser(val id: Int, val firstName: String, val lastName: String, val teacher: Boolean, val chat: Boolean)
data class GroupInfoDetailed(val id: Int, val name: String, val leader: SimpleUser, val accepted: Boolean, val chat: Boolean, val members: List<GroupUser>, val children: List<GroupInfoDetailed>)