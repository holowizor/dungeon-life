package dungeonlife

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue

object MapReader {

    fun readMap(mapAsset: String): LevelMap {
        val json = JsonReader()
        val base = json.parse(Gdx.files.internal(mapAsset))

        val tileWidth = base.getInt("tilewidth", 16)
        val textures = base.get("tilesets").asIterable().map { MapTexture(it.getInt("firstgid"), it.getString("source")) }
        val tiles = parseLayers(base.get("layers").asIterable())

        return LevelMap(tileWidth, textures, tiles)
    }

    private fun parseLayers(layers: Iterable<JsonValue>): Map<TileCoordinates, MapTile> {
        val tileMap = HashMap<TileCoordinates, MapTile>()
        layers.forEach { layer -> layer.get("chunks").asIterable().forEach { chunk -> parseChunk(chunk, tileMap) } }
        return tileMap
    }

    private fun parseChunk(chunk: JsonValue, tileMap: java.util.HashMap<TileCoordinates, MapTile>) {
        val width = chunk.getInt("width")
        val x = chunk.getInt("x")
        val y = chunk.getInt("y")
        val data = chunk.get("data").asIntArray()

        data.forEachIndexed { index, gid -> if (gid > 0) createOrUpdateTile(x, y, width, index, gid, tileMap) }

    }

    private fun createOrUpdateTile(chunkStartX: Int, chunkStartY: Int, chunkWidth: Int, index: Int, gid: Int, tileMap: java.util.HashMap<TileCoordinates, MapTile>) {
        val cx = index % chunkWidth
        val cy = index / chunkWidth
        val tc = TileCoordinates(chunkStartX + cx, chunkStartY + cy)
        tileMap.getOrPut(tc) { MapTile() }.gids.add(gid)
    }
}

class LevelMap(val tileWidth: Int = 16, val textures: List<MapTexture> = ArrayList(), val tileMap: Map<TileCoordinates, MapTile> = HashMap())

class MapTexture(val firstGid: Int, source: String)

class MapTile(val gids: MutableList<Int> = ArrayList())

data class TileCoordinates(val x: Int, val y: Int)