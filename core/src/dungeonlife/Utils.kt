package dungeonlife

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion

object textureHelper {

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
}
