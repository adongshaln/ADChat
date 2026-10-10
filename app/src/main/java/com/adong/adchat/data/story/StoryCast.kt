package com.adong.adchat.data.story

import org.json.JSONObject

/** The {{char}} card produced from the opening discussion. Opening text stays empty. */
data class StoryCharacterCard(
    val name: String,
    val description: String,
    val personality: String,
    val scenario: String
) {
    fun toJson(): String = JSONObject()
        .put("name", name)
        .put("description", description)
        .put("personality", personality)
        .put("scenario", scenario)
        .toString()

    companion object {
        fun parse(raw: String): StoryCharacterCard {
            val json = JSONObject(extractJsonObject(raw))
            return StoryCharacterCard(
                name = json.optString("name").trim(),
                description = json.optString("description").trim(),
                personality = json.optString("personality").trim(),
                scenario = json.optString("scenario").trim()
            ).also { require(it.name.isNotBlank() && it.description.isNotBlank()) { "角色卡缺少名字或描述" } }
        }

        fun fromStored(raw: String): StoryCharacterCard? =
            raw.takeIf { it.isNotBlank() }?.let { runCatching { parse(it) }.getOrNull() }
    }
}

fun parseUserPersona(raw: String): String {
    val json = JSONObject(extractJsonObject(raw))
    return json.optString("persona").trim().also { require(it.isNotBlank()) { "身份卡是空的" } }
}

internal fun extractJsonObject(raw: String): String {
    val fenced = Regex("```(?:json)?\\s*([\\s\\S]*?)```").find(raw)?.groupValues?.get(1)
    val source = fenced ?: raw
    val start = source.indexOf('{')
    val end = source.lastIndexOf('}')
    require(start >= 0 && end > start) { "模型没有返回可解析的设定" }
    return source.substring(start, end + 1)
}

internal const val STORY_CHAR_CARD_PROMPT =
    "根据讨论，只输出一个 JSON 对象，不要解释。字段：name、description、personality、scenario。这是故事里的对方（{{char}}），不是用户。不要写开场白。讨论里没说定的内容留空字符串，不要编造。"

internal const val STORY_PERSONA_PROMPT =
    "根据讨论和用户要担任的身份，只输出一个 JSON 对象，不要解释。字段只有 persona：用几段话说明用户（{{user}}）是谁、和故事的关系。不要写开场白，不要描写对方。"
