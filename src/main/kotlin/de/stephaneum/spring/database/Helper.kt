package de.stephaneum.spring.database

import java.sql.Timestamp

fun now() = Timestamp(System.currentTimeMillis())
fun now(delta: Int) = Timestamp(System.currentTimeMillis() + delta)