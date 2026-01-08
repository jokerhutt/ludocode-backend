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

    fun <T : Any?> putOk(
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
            .put(url)
            .then()
            .statusCode(200)
            .extract()
            .`as`(responseType)
    }

    fun deleteNoContent(
        url: String,
        userId: UUID,
    ) {
        given()
            .header("X-Test-User-Id", userId.toString())
            .`when`()
            .delete(url)
            .then()
            .statusCode(204)
    }

    fun postNoContent(
        url: String,
        userId: UUID,
        body: Any? = null,
    ) {
        val req = given()
            .header("X-Test-User-Id", userId.toString())
            .contentType(ContentType.JSON)

        if (body != null) {
            req.body(body)
        }

        req.`when`()
            .post(url)
            .then()
            .statusCode(204)
    }

    fun <T : Any> getOk(
        url: String,
        userId: UUID? = null,
        responseType: Class<T>,
        jwt: String? = null,
    ): T {
        val req = given().contentType(ContentType.JSON)

        when {
            jwt != null -> req.cookie("AUTH", jwt)
            userId != null -> req.header("X-Test-User-Id", userId.toString())
            else -> error("Must provide either jwt or userId for test request")
        }

        return req.`when`()
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