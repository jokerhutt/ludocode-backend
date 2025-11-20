package com.ludocode.ludocodebackend.support

import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.response.ValidatableResponse
import org.hamcrest.CoreMatchers.equalTo
import java.util.UUID

object TestRestClient {

    fun <T : Any?> postOk(
        url: String,
        userId: UUID,
        body: Any?,
        responseType: Class<T>,
    ): T {

        val req = given()
            .header("X-Test-User-Id", userId.toString())
            .contentType(ContentType.JSON)

        if (body != null) {
            req.body(body)
        }

        return req.`when`()
            .post(url)
            .then()
            .statusCode(200)
            .extract()
            .`as`(responseType)
    }

    fun <T : Any> getOk(
        url: String,
        userId: UUID,
        responseType: Class<T>,
    ): T {
        return given()
            .header("X-Test-User-Id", userId.toString())
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .`as`(responseType)
    }

    fun <T : Any> getOk(
        url: String,
        userId: UUID,
        responseType: Class<T>,
        queryParams: Map<String, Any?>
    ): T {
        val req = given()
            .header("X-Test-User-Id", userId.toString())

        queryParams.forEach { (k, v) ->
            when (v) {
                is Array<*> -> req.queryParam(k, *v)
                is Iterable<*> -> req.queryParam(k, *(v.toList().toTypedArray()))
                else -> req.queryParam(k, v)
            }
        }

        return req
            .`when`()
            .get(url)
            .then()
            .statusCode(200)
            .extract()
            .`as`(responseType)
    }

    fun assertError(
        method: String,
        url: String,
        userId: UUID,
        body: Any? = null,
        expected: ErrorCode
    ): ValidatableResponse {
        val spec = given()
            .header("X-Test-User-Id", userId.toString())
            .contentType(ContentType.JSON)

        if (body != null) spec.body(body)

        val response = when (method.uppercase()) {
            "GET" -> spec.`when`().get(url)
            "POST" -> spec.`when`().post(url)
            "PUT" -> spec.`when`().put(url)
            "DELETE" -> spec.`when`().delete(url)
            else -> error("Unsupported method: $method")
        }

        return response.then()
            .statusCode(expected.status.value())
            .body("code", equalTo(expected.name))
            .body("title", equalTo(expected.name))
    }
}