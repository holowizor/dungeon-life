package dungeonlife

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage

class Logo(x: Float, y: Float, s: Stage) : Actor() {
    val tex = TextureHelper.loadTexture("welcome-logo.png")
    init {
        this.x = x
        this.y = y
        width = 640f
        height = 480f

        s.addActor(this)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(tex, x, y)
        super.draw(batch, parentAlpha)
    }
}

class Tile(val tex: TextureRegion, gridX: Int, gridY: Int, tileWidth: Int, z: Int, s: Stage) : Actor() {
    init {
        x = gridX.toFloat() * 16
        y = gridY.toFloat() * 16
        width = tileWidth.toFloat()
        height = tileWidth.toFloat()
        // FIXME does not work
        zIndex = z

        s.addActor(this)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(tex, x, y)
        super.draw(batch, parentAlpha)
    }
}

class Blood(val tex: TextureRegion, x: Float, y: Float, bloodHeight: Int, bloodWidth: Int, s: Stage, z: Int) : Actor() {
    init {
        this.x = x
        this.y = y
        width = bloodWidth.toFloat()
        height = bloodHeight.toFloat()

        s.addActor(this)
        this.setZIndex(z)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(tex, x, y)
        super.draw(batch, parentAlpha)
    }
}

object BloodBucket {
    var z = 75000

    val blood = arrayOf(TextureHelper.loadTexture("blood-1.png"),
            TextureHelper.loadTexture("blood-2.png"),
            TextureHelper.loadTexture("blood-3.png"),
            TextureHelper.loadTexture("blood-4.png"),
            TextureHelper.loadTexture("blood-5.png"),
            TextureHelper.loadTexture("blood-6.png"))

    fun freshBlood(pos: Vector2, s: Stage) = Blood(blood[(Math.random() * 5f).toInt()], -16f + pos.x + (8.0 - Math.random() * 16.0).toFloat(), -16f + pos.y + (8.0 - Math.random() * 16.0).toFloat(), 32, 32, s, z++)
}

class Weapon(s: Stage, val owner: AnimatedActor, val hitTimeMs: Long, val offX: Float,
             val animIdleRight: Animation<TextureRegion>,
             val animIdleLeft: Animation<TextureRegion>,
             val animHitRight: Animation<TextureRegion>,
             val animHitLeft: Animation<TextureRegion>) : BaseActor(owner.x, owner.y, s) {

    private var hitAnim = true
    private var hitStart = 0L

    val range = 16f

    init {
        animation = animIdleRight
        width = 30f
        height = 30f
    }

    fun hit() {
        hitAnim = true
        hitStart = System.currentTimeMillis()
    }

    override fun act(dt: Float) {
        super.act(dt)
        this.x = owner.x + if (owner.moveRight) offX else -offX
        this.y = owner.y
        this.zIndex = if (owner.zIndex > 0) owner.zIndex - 1 else 0
        setActiveAnimation()
    }

    private fun setActiveAnimation() {
        if (System.currentTimeMillis() - hitStart > hitTimeMs) {
            hitAnim = false
        }

        val faceRight = owner.moveRight
        if (hitAnim) {
            super.animation = if (faceRight) animHitRight else animHitLeft
        } else {
            super.animation = if (faceRight) animIdleRight else animIdleLeft
        }
    }
}
