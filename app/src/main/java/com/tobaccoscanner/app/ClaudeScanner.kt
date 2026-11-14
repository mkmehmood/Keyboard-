package com.tobaccoscanner.app

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class ScanResult(
    val found: Boolean,
    val what: String,
    val form: String,
    val confidence: Int,
    val score: Int?,
    val color: String,
    val curing: String,
    val moisture: String,
    val defects: List<String>,
    val positives: List<String>,
    val advice: List<String>,
    val photoTips: List<String>,
)

enum class FailKind { INVALID_KEY, RATE_LIMIT, NETWORK, SERVER, PARSE, OTHER }

class ScanException(val kind: FailKind, message: String? = null) : Exception(message)

/** Sends one photo to the Anthropic Messages API and parses the quality report. */
object ClaudeScanner {
    private const val ENDPOINT = "https://api.anthropic.com/v1/messages"

    private fun prompt(language: String): String =
        "You are an experienced tobacco quality inspector. Look at the attached photo. " +
            "Tobacco can appear in any situation: growing on a plant in a field, hanging in a curing barn, " +
            "in a bale or pile, in a hand, on a table or floor, with a cluttered background or poor lighting, " +
            "or as ground powder, snuff or naswar. Find any tobacco in the photo and judge only what is visible. " +
            "Reply with JSON only, no markdown, in exactly this shape: " +
            "{\"found\":boolean,\"what\":\"one short sentence on what the photo shows\",\"form\":\"short\"," +
            "\"confidence\":0-100,\"score\":0-100 or null,\"color\":\"short\"," +
            "\"curing\":\"short, e.g. well cured, under-cured, over-cured, unclear\"," +
            "\"moisture\":\"short, e.g. dry, slightly damp, damp, unclear\"," +
            "\"defects\":[\"short sentence that includes how serious it is\"],\"positives\":[\"short\"]," +
            "\"advice\":[\"short, practical\"],\"photo_tips\":[\"how to retake the photo for a better read\"]}. " +
            "IMPORTANT: write every string value in clear, simple $language. Keep the JSON keys and the " +
            "found, confidence and score values exactly as specified. " +
            "Rules: if there is no tobacco, or you cannot tell tobacco from similar leaves, set found to false " +
            "and explain in what. Score from color and evenness of cure, absence of mold, rot, pests and damage, " +
            "leaf wholeness and cleanliness (for powder: no filler, dye, mold specks or grit). " +
            "Be honest about uncertainty and lighting or white-balance problems. You cannot smell or touch the " +
            "sample, so never claim to judge smell, moisture content or chemicals precisely. " +
            "Do not invent details you cannot see. Keep every string short."

    suspend fun scan(bitmap: Bitmap, apiKey: String, model: String, language: String): ScanResult =
        withContext(Dispatchers.IO) {
            val jpeg = ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            val b64 = Base64.encodeToString(jpeg.toByteArray(), Base64.NO_WRAP)

            val image = JSONObject()
                .put("type", "image")
                .put(
                    "source",
                    JSONObject()
                        .put("type", "base64")
                        .put("media_type", "image/jpeg")
                        .put("data", b64),
                )
            val text = JSONObject().put("type", "text").put("text", prompt(language))
            val body = JSONObject()
                .put("model", model)
                .put("max_tokens", 1500)
                .put(
                    "messages",
                    JSONArray().put(
                        JSONObject()
                            .put("role", "user")
                            .put("content", JSONArray().put(image).put(text)),
                    ),
                )

            val conn = try {
                (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 20_000
                    readTimeout = 90_000
                    doOutput = true
                    setRequestProperty("content-type", "application/json")
                    setRequestProperty("x-api-key", apiKey)
                    setRequestProperty("anthropic-version", "2023-06-01")
                }
            } catch (e: IOException) {
                throw ScanException(FailKind.NETWORK, e.message)
            }

            val code: Int
            val response: String
            try {
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            } catch (e: IOException) {
                throw ScanException(FailKind.NETWORK, e.message)
            } finally {
                conn.disconnect()
            }

            when {
                code in 200..299 -> Unit
                code == 401 || code == 403 -> throw ScanException(FailKind.INVALID_KEY)
                code == 429 -> throw ScanException(FailKind.RATE_LIMIT)
                code >= 500 -> throw ScanException(FailKind.SERVER)
                else -> throw ScanException(FailKind.OTHER, apiMessage(response))
            }

            parse(response)
        }

    private fun apiMessage(response: String): String? = try {
        JSONObject(response).optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        null
    }

    private fun parse(response: String): ScanResult {
        try {
            val content = JSONObject(response).getJSONArray("content")
            val sb = StringBuilder()
            for (i in 0 until content.length()) {
                val block = content.getJSONObject(i)
                if (block.optString("type") == "text") sb.append(block.optString("text"))
            }
            val raw = sb.toString()
            val start = raw.indexOf('{')
            val end = raw.lastIndexOf('}')
            if (start < 0 || end <= start) throw ScanException(FailKind.PARSE)
            val o = JSONObject(raw.substring(start, end + 1))

            return ScanResult(
                found = o.optBoolean("found", false),
                what = o.optString("what"),
                form = o.optString("form"),
                confidence = o.optInt("confidence", 0).coerceIn(0, 100),
                score = if (o.isNull("score") || !o.has("score")) null else o.optInt("score").coerceIn(0, 100),
                color = o.optString("color"),
                curing = o.optString("curing"),
                moisture = o.optString("moisture"),
                defects = o.strings("defects"),
                positives = o.strings("positives"),
                advice = o.strings("advice"),
                photoTips = o.strings("photo_tips"),
            )
        } catch (e: ScanException) {
            throw e
        } catch (e: Exception) {
            throw ScanException(FailKind.PARSE, e.message)
        }
    }

    private fun JSONObject.strings(name: String): List<String> {
        val arr = optJSONArray(name) ?: return emptyList()
        val out = ArrayList<String>()
        for (i in 0 until arr.length()) {
            val s = arr.optString(i)
            if (s.isNotBlank() && s != "null") out.add(s)
        }
        return out
    }
}
