package dungeonlife

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue

object MapReader {

    fun readMap(mapAsset: String): LevelMap {
        val json = JsonReader()
        val base = json.parse(Gdx.files.internal(mapAsset))

        val tileWidth = base.getInt("tilewidth", 16)
        val textures = base.get("tilesets").asIterable().map { MapTexture(it.getLong("firstgid"), it.getString("source")) }
        val tiles = parseLayers(base.get("layers").asIterable())
        val objects = parseObjects(base.get("layers").asIterable())

        return LevelMap(tileWidth, textures, tiles, objects)
    }

    private fun parseLayers(layers: Iterable<JsonValue>): Map<TileCoordinates, MapTile> {
        val tileMap = HashMap<TileCoordinates, MapTile>()
        layers.forEach { layer ->
            if (layer.getString("type") == "tilelayer")
                layer.get("chunks").asIterable().forEach { chunk -> parseChunk(chunk, tileMap) }
        }
        return tileMap
    }

    private fun parseChunk(chunk: JsonValue, tileMap: java.util.HashMap<TileCoordinates, MapTile>) {
        val width = chunk.getInt("width")
        val x = chunk.getInt("x")
        val y = chunk.getInt("y")
        val data = chunk.get("data").asLongArray()

        data.forEachIndexed { index, gid -> if (gid > 0) createOrUpdateTile(x, y, width, index, gid, tileMap) }
    }

    private fun createOrUpdateTile(chunkStartX: Int, chunkStartY: Int, chunkWidth: Int, index: Int, gid: Long, tileMap: java.util.HashMap<TileCoordinates, MapTile>) {
        val cx = index % chunkWidth
        val cy = index / chunkWidth
        val tc = TileCoordinates(chunkStartX + cx, chunkStartY + cy)
        tileMap.getOrPut(tc) { MapTile() }.gids.add(gid)
    }

    private fun parseObjects(layers: Iterable<JsonValue>): Map<String, MutableList<MapObject>> {
        val objectMap = HashMap<String, MutableList<MapObject>>()
        layers.forEach { layer ->
            if (layer.getString("type") == "objectgroup")
                layer.get("objects").asIterable().forEach { obj -> parseObject(obj, objectMap) }
        }
        return objectMap
    }

    private fun parseObject(obj: JsonValue, objMap: MutableMap<String, MutableList<MapObject>>) {
        val x = obj.getFloat("x")
        val y = obj.getFloat("y")
        val name = obj.getString("name")
        val type = obj.getString("type")

        objMap.getOrPut(type, { ArrayList<MapObject>() }).add(MapObject(x, y, type, name))
    }
}

class LevelMap(val tileWidth: Int = 16,
               val textures: List<MapTexture> = ArrayList(),
               val tileMap: Map<TileCoordinates, MapTile> = HashMap(),
               val objectMap: Map<String, List<MapObject>> = HashMap())

class MapTexture(val firstGid: Long, val source: String)

class MapTile(val gids: MutableList<Long> = ArrayList())

data class TileCoordinates(val x: Int, val y: Int)

class MapObject(val x: Float, val y: Float, val type: String, val name: String)