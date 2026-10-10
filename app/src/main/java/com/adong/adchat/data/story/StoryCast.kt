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

/** Confirmed {{char}} card and {{user}} persona sent with later story requests. */
data class StoryCastContext(
    val card: StoryCharacterCard? = null,
    val userPersona: String = ""
) {
    fun toPromptBlock(): String = buildString {
        card?.let { card ->
            append("[已确认的对方角色卡。这是故事里的对方，不是用户]\n")
            append("名字：").append(card.name)
            if (card.description.isNotBlank()) append("\n这个人：").append(card.description)
            if (card.personality.isNotBlank()) append("\n性格：").append(card.personality)
            if (card.scenario.isNotBlank()) append("\n这部故事：").append(card.scenario)
        }
        if (userPersona.isNotBlank()) {
            if (isNotEmpty()) append("\n\n")
            append("[已确认的用户身份。这是用户在故事里的位置，不是对方]\n")
            append(userPersona.trim())
        }
    }.trim()

    companion object {
        fun fromStory(story: Story): StoryCastContext = StoryCastContext(
            card = StoryCharacterCard.fromStored(story.charCardJson),
            userPersona = story.userPersona
        )
    }
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
    "根据讨论，只输出一个 JSON 对象，不要解释，不要写开场白，不要编造讨论里没说定的内容。这是故事里的对方（{{char}}），不是用户。" +
        "name 是对方的名字。description 只写这个人是谁，不要写整部作品。personality 是性格。" +
        "scenario 必须是这部作品本身：哪部原作的同人、世界观、局面、已商定的剧情，以及这个人在其中的位置。不要把人物小传再抄一遍。讨论若是一部同人，scenario 不能空。"

internal const val STORY_PERSONA_PROMPT =
    "根据讨论和用户要担任的身份，只输出一个 JSON 对象，不要解释。字段只有 persona：说明用户（{{user}}）是谁，以及在这部已商定的作品里处于什么位置。不要写开场白，不要把对方的人物小传再写一遍。"
