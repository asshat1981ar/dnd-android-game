package com.orion.dndgame.network

import com.orion.dndgame.data.models.Character
import com.orion.dndgame.data.models.NPC
import com.orion.dndgame.data.models.Quest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("characters")
    suspend fun getCharacters(): Response<List<Character>>
    
    @POST("characters")
    suspend fun createCharacter(@Body character: Character): Response<Character>
    
    @GET("npcs")
    suspend fun getNPCs(): Response<List<NPC>>
    
    @GET("quests")
    suspend fun getQuests(): Response<List<Quest>>
}