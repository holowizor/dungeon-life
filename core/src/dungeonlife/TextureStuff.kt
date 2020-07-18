package dungeonlife

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue

object TextureMapReader {

    fun readTextureMap(initGid: Long, textureMapAsset: String): Map<Long, TypedTexture> {
        val textureMap = HashMap<Long, TypedTexture>()

        val json = JsonReader()
        val base = json.parse(Gdx.files.internal(textureMapAsset))

        val textureFile = base.getString("image")
        val cols = base.getInt("columns")
        val tileWidth = base.getInt("tilewidth")

        val tileTypesMap = parseTiles(base.get("tiles").asIterable())

        val texture = Texture(Gdx.files.internal(textureFile))
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        val temp = TextureRegion.split(texture, tileWidth, tileWidth)

        temp.forEachIndexed { row, arrayOfTextureRegions ->
            arrayOfTextureRegions.forEachIndexed { col, textureRegion ->
                val gid = row * cols + col
                textureMap[gid + initGid] = TypedTexture(textureRegion, tileTypesMap.getOrDefault(gid, "none"))
            }
        }

        return textureMap
    }

    private fun parseTiles(tiles: Iterable<JsonValue>): Map<Int, String> {
        val tileTypesMap = HashMap<Int, String>()
        tiles.forEach { tileTypesMap[it.getInt("id")] = it.getString("type") }
        return tileTypesMap
    }
}

class TypedTexture(val textureRegion: TextureRegion, val type: String)

object TextureHelper {

    fun loadTexture(path: String) = TextureRegion(Texture(Gdx.files.internal(path)))

    fun loadAnimation(paths: Array<String>, frameDuration: Float = .1f, loop: Boolean = true): Animation<TextureRegion> {
        val arr = com.badlogic.gdx.utils.Array<TextureRegion>()
        paths.forEach {
            val texture = Texture(Gdx.files.internal(it))
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            arr.add(TextureRegion(texture))
        }

        val anim = Animation(frameDuration, arr)
        if (loop) {
            anim.playMode = Animation.PlayMode.LOOP
        } else {
            anim.setPlayMode(Animation.PlayMode.NORMAL)
        }

        return anim
    }

    fun loadAnimation(path: String, frameWidth: Int, frameHeight: Int, startCol: Int, endCol: Int, frameDuration: Float = .1f, loop: Boolean = true): Animation<TextureRegion> {
        val texture = Texture(Gdx.files.internal(path))
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        val temp = TextureRegion.split(texture, frameWidth, frameHeight)
        val arr = com.badlogic.gdx.utils.Array<TextureRegion>()

        for (c in startCol until endCol) arr.add(temp[0][c])

        val anim = Animation(frameDuration, arr)
        if (loop) {
            anim.setPlayMode(Animation.PlayMode.LOOP)
        } else {
            anim.setPlayMode(Animation.PlayMode.NORMAL)
        }

        return anim
    }

    fun boundCameraToLayout(cam: Camera, worldWidth: Float, worldHeight: Float) {
        cam.position.x = MathUtils.clamp(cam.position.x, cam.viewportWidth / 2, worldWidth - cam.viewportWidth / 2)
        cam.position.y = MathUtils.clamp(cam.position.y, cam.viewportHeight / 2, worldHeight - cam.viewportHeight / 2)
    }
}
