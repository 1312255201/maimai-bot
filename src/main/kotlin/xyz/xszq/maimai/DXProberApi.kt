package xyz.xszq.maimai

import com.soywiz.kds.iterators.fastForEach
import com.soywiz.korio.async.async
import com.soywiz.korio.async.launch
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.file.std.toVfs
import com.soywiz.korio.file.writeToFile
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.event.GlobalEventChannel
import xyz.xszq.MaimaiBot
import xyz.xszq.xyz.xszq.maimai.MaimaiImage
import xyz.xszq.xyz.xszq.maimai.MaimaiImage.imgDir


@Serializable
data class MaimaiPlayerData(val nickname: String, val rating: Int, val additional_rating: Int, val username: String,
                 val charts: Map<String, List<MaimaiPlayScore>>)

@Serializable
open class MaimaiPlayScore(val achievements: Double, val ds: Double, var dxScore: Int, val fc: String, val fs: String,
                     val level: String, val level_index: Int, val level_label: String, val ra: Int,
                     val rate: String, val song_id: Int, val title: String, val type: String)
object EmptyMaimaiPlayRecord: MaimaiPlayScore(0.0, .0, 0, "", "", "",
    0, "", 0, "", -1, "", "")
fun List<MaimaiPlayScore>.fillEmpty(target: Int): List<MaimaiPlayScore> {
    val result = toMutableList()
    for (i in (1..(target-size)))
        result.add(EmptyMaimaiPlayRecord)
    return result
}

@Serializable
data class MaimaiMusicInfo(val id: String, val title: String, val type: String, val ds: List<Double>,
                           val level: List<String>, val cids: List<Int>, val charts: List<MaimaiChartInfo>,
                           val basic_info: MaimaiMusicBasicInfo
)
@Serializable
data class MaimaiChartInfo(val notes: List<Int>, val charter: String)
@Serializable
data class MaimaiMusicBasicInfo(val title: String, val artist: String, val genre: String, val bpm: Int,
                                val release_date: String, val from: String, val is_new: Boolean)

object DXProberApi {
    private const val site = "https://www.diving-fish.com"
    private val json = Json { isLenient = true; ignoreUnknownKeys = true }
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }

    }
    suspend fun getMusicList(): Array<MaimaiMusicInfo> {
        kotlin.runCatching {
            return client.get("$site/api/maimaidxprober/music_data")
        }.onFailure {
            it.printStackTrace()
        }
        return emptyArray()
    }
    suspend fun getCovers() {
        val covers = imgDir["covers"]
        if (!covers.exists())
            covers.mkdir()
        MaimaiBot.logger.info("正在缓存歌曲封面中……")
        var cnt = 0
        val semaphore = Semaphore(32)
        coroutineScope {
            MaimaiBot.musics.fastForEach {
                val target = covers["${it.id}.jpg"]
                if (!target.exists()) {
                    launch {
                        semaphore.withPermit {
                            kotlin.runCatching {
                                client.get<ByteArray>("$site/covers/${it.id}.jpg").writeToFile(target)
                                cnt ++
                                MaimaiBot.logger.info("${it.id}. ${it.title} 封面下载完成")
                            }.onFailure { e ->
                                MaimaiBot.logger.verbose(e.stackTraceToString())
                            }
                        }
                    }
                }
            }
        }
        MaimaiBot.logger.info("本次已缓存 $cnt 个歌曲封面。")
        if (cnt > 0)
            MaimaiImage.reloadImages()
    }
    suspend fun getPlayerData(type: String = "qq", id: String,
                              b50: Boolean = false): Pair<HttpStatusCode, MaimaiPlayerData?> {
        val payload = buildJsonObject {
            put(type, id)
            if (b50)
                put("b50", true)
        }
        kotlin.runCatching {
            val result: HttpResponse = client.post("$site/api/maimaidxprober/query/player") {
                contentType(ContentType.Application.Json)
                body = payload
            }
            return Pair(result.status,
                if (result.status == HttpStatusCode.OK) json.decodeFromString(result.readText()) else null)
        }
        return Pair(HttpStatusCode.BadGateway, null)
    }
}