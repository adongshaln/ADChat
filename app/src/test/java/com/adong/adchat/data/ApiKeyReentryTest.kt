package com.adong.adchat.data

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiKeyReentryTest {
    private fun stored(vararg profiles: JSONObject): JSONObject =
        JSONObject().put("profiles", JSONArray().apply { profiles.forEach(::put) })

    @Test
    fun undecryptableKeysAreFlaggedForReentry() {
        val stored = stored(
            JSONObject().put("id", "a").put("apiKey", "enc:v1:iv:ciphertext"),
            JSONObject().put("id", "b").put("apiKey", "plain-key")
        )
        val config = AppConfig(
            profiles = listOf(ApiProfile(id = "a", apiKey = ""), ApiProfile(id = "b", apiKey = "plain-key")),
            activeChatProfileId = "a",
            activeImageProfileId = "a"
        )
        assertEquals(setOf("a"), undecryptableKeyProfileIds(stored, config))
    }

    @Test
    fun profilesWithWorkingKeysAndMissingProfilesAreNotFlagged() {
        val stored = stored(
            JSONObject().put("id", "a").put("apiKey", "enc:v1:iv:ciphertext"),
            JSONObject().put("id", "removed").put("apiKey", "enc:v1:iv:ciphertext"),
            JSONObject().put("id", "local").put("apiKey", "")
        )
        val config = AppConfig(
            profiles = listOf(ApiProfile(id = "a", apiKey = "decrypted-key"), ApiProfile(id = "local")),
            activeChatProfileId = "a",
            activeImageProfileId = "a"
        )
        assertEquals(emptySet<String>(), undecryptableKeyProfileIds(stored, config))
    }

    @Test
    fun missingProfilesArrayIsHandled() {
        assertEquals(emptySet<String>(), undecryptableKeyProfileIds(JSONObject(), AppConfig(emptyList(), "x", "x")))
    }
}
