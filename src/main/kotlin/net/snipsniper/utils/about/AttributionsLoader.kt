package net.snipsniper.utils.about

import org.capturecoop.defaultdepot.files.FileHandle
import org.json.JSONObject

class AttributionsData(
    val thirdParty: List<AttributionData>
) {
    companion object {
        fun fromJSON(json: JSONObject): AttributionsData = AttributionsData(
            thirdParty = json.getJSONArray("thirdparty").map { json ->
                AttributionData.fromJSON((json as JSONObject))
            }
        )
    }
}

class AttributionData(
    val name: String,
    val url: String,
    val license: AttributionLicenseData
) {
    companion object {
        fun fromJSON(json: JSONObject) = AttributionData(
            name = json.getString("name"),
            url = json.getString("url"),
            license = AttributionLicenseData.fromJSON(json.getJSONObject("license"))
        )
    }
}

class AttributionLicenseData(
    val short: String,
    val file: String
) {
    //This loads the full license text lazily, it will only do so when requested, which in the case of
    //the AttributionWindow will only happen when the user expands the License.
    val full: String by lazy {
        FileHandle.internal("$ATTRIBUTIONS_DIR$file").readText()
    }

    companion object {
        private const val ATTRIBUTIONS_DIR = "/net/snipsniper/resources/attributions/"
        fun fromJSON(json: JSONObject) = AttributionLicenseData(short = json.getString("short"), file = json.getString("file"))
    }
}

object AttributionsLoader {
    private const val ATTRIBUTIONS_FILE = "/net/snipsniper/resources/attributions/attributions.json"

    fun load(): AttributionsData {
        val json = JSONObject(FileHandle.internal(ATTRIBUTIONS_FILE).readText())
        return AttributionsData.fromJSON(json)
    }
}