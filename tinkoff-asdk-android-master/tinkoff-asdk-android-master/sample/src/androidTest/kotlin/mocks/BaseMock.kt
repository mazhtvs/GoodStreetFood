package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.google.common.net.HttpHeaders
import com.google.gson.Gson

/**
 *  Abstract class for any endpoint mock-builder
 **/
abstract class BaseMock {

    /**
     *  Matcher for the request that wil allow Wiremock to understand what request to stub
     **/
    abstract val matcher: MappingBuilder

    /**
     * Will return Wiremock based response for stub with your response json body, delay and HTTP code
     * @param jsonBody object to be serialised into equivalent JSON representation . Note: the specified object must not be a generic type, but the object fields can be generic type
     * @param delayInMilliseconds desired response delay in ms, equals 0 ms by default
     **/
    protected fun <T> respondWith(
        jsonBody: T,
        delayInMilliseconds: Int = 0,
    ): ResponseDefinitionBuilder {
        val stringBody: String = if (jsonBody is String) jsonBody else Gson().toJson(jsonBody)

        return ok()
            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .withBody(stringBody)
            .withFixedDelay(delayInMilliseconds)
    }

    protected fun respondWithHtml(
        stringBody: String,
        delayInMilliseconds: Int = 0,
    ): ResponseDefinitionBuilder {
        return ok()
            .withHeader(HttpHeaders.CONTENT_TYPE, "text/html")
            .withBody(stringBody)

            .withFixedDelay(delayInMilliseconds)
    }

    /**
     * Will return Wiremock based response for stub with your response file body, delay and HTTP code
     * @param fileName is a string of the file name and relative path to it. This file will be the body of your response. Note: file must be stored in src/test/resources/__files and the relative path starts from there
     * @param contentType is the value of Сontent-Type header of your response. Need for successfully reading the file
     * @param delayInMilliseconds desired response delay in ms, equals 0 ms by default
     **/
    protected fun respondWith(
        fileName: String,
        contentType: String,
        delayInMilliseconds: Int = 0,
    ): ResponseDefinitionBuilder = ok()
        .withHeader(HttpHeaders.CONTENT_TYPE, contentType)
        .withBodyFile(fileName)
        .withFixedDelay(delayInMilliseconds)

}
