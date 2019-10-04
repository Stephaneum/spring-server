package de.stephaneum.spring.helper

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

object RestClient {

    private val rest = RestTemplate()
    private val headers = HttpHeaders()

    init {
        headers.add("Content-Type", "application/json")
        headers.add("Accept", "*/*")
    }

    fun get(url: String): Pair<HttpStatus, String?> {
        val requestEntity = HttpEntity("", headers)
        val responseEntity = rest.exchange(url, HttpMethod.GET, requestEntity, String::class.java)
        return Pair(responseEntity.statusCode, responseEntity.body)
    }

    fun post(url: String, json: String = ""): Pair<HttpStatus, String?> {
        val requestEntity = HttpEntity(json, headers)
        val responseEntity = rest.exchange(url, HttpMethod.POST, requestEntity, String::class.java)
        return Pair(responseEntity.statusCode, responseEntity.body)
    }

    fun put(url: String, json: String = ""): HttpStatus {
        val requestEntity = HttpEntity(json, headers)
        val responseEntity = rest.exchange(url, HttpMethod.PUT, requestEntity, String::class.java)
        return responseEntity.statusCode
    }

    fun delete(url: String): HttpStatus {
        val requestEntity = HttpEntity("", headers)
        val responseEntity = rest.exchange(url, HttpMethod.DELETE, requestEntity, String::class.java)
        return responseEntity.statusCode
    }
}