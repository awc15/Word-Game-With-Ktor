package com.awcology

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*

class WordsServiceImpl(
    private val client: HttpClient
) : WordsService {

    override suspend fun getWords(number: Int, length: Int): List<String> {

        return try {
            client.get {
                url(HttpRoutes.WORDS)
                parameter("number", number)
                parameter("length", length)
            }
        }catch (e: RedirectResponseException){
            //3xx - responses - some kind of redirect issue
            println("Error: ${e.response.status.description}")
            emptyList()
        }catch (e: ClientRequestException){
            //4xx - responses - Post some sort of data that server doesn't know how to handle
            println("Error: ${e.response.status.description}")
            emptyList()
        }catch (e: ServerResponseException){
            //5xx - responses - Something on the server side went wrong
            println("Error: ${e.response.status.description}")
            emptyList()
        }catch (e:Exception){
            println(e.message)
            emptyList()
        }

        return emptyList()
    }
}