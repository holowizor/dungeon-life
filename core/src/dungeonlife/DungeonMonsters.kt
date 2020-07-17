package dungeonlife

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage

class WhitePx(x: Float, y: Float, s: Stage, anim: Animation<TextureRegion>, val follows: Actor) :
        BaseActor(x, y, s) {
    init {
        animation = anim
        width = 1f
        height = 1f
    }

    override fun act(dt: Float) {
        super.act(dt)
        this.x = follows.x
        this.y = follows.y
    }
}

class Knight(x: Float, y: Float, s: Stage) :
        AnimatedActor(x, y, s,
                TextureHelper.loadAnimation("elite-knight-walk-right.png", 32, 32, 0, 4),
                TextureHelper.loadAnimation("elite-knight-walk-left.png", 32, 32, 0, 4),
                TextureHelper.loadAnimation("elite-knight-idle-right.png", 32, 32, 0, 1),
                TextureHelper.loadAnimation("elite-knight-idle-left.png", 32, 32, 0, 1),
                TextureHelper.loadAnimation("elite-knight-idle-right.png", 32, 32, 0, 1, loop = false),
                TextureHelper.loadAnimation("elite-knight-idle-left.png", 32, 32, 0, 1, loop = false)) {

    val heroState = HeroState()
    val stunnedMillis: Long
    var stunned = false
    var stunTime = 0L

    init {
        width = 32f
        height = 32f

        maxSpeed = 75f
        deceleration = 100f
        midPoint = Vector2(17f, 8f)

        stunnedMillis = 500L
    }

    fun stun() {
        stunTime = System.currentTimeMillis()
        stunned = true
    }

    override fun boundaryVerticles(): FloatArray = floatArrayOf(10f, 0f, 22f, 0f, 22f, 8f, 10f, 8f)

    override fun act(dt: Float) {
        super.act(dt)
        alignCamera()

        if (stunned && System.currentTimeMillis() - stunTime > stunnedMillis) {
            stunned = false
        }
    }
}

open class Monster(x: Float, y: Float, s: Stage, health: Float, killReward: Float, attack: Float,
                   animRight: Animation<TextureRegion>, animLeft: Animation<TextureRegion>,
                   animIdleRight: Animation<TextureRegion>, animIdleLeft: Animation<TextureRegion>,
                   animDeadRight: Animation<TextureRegion>, animDeadLeft: Animation<TextureRegion>) :
        AnimatedActor(x, y, s, animRight, animLeft, animIdleRight, animIdleLeft, animDeadRight, animDeadLeft) {

    val state = MonsterState(health, killReward, attack)
    var sight = 5f * 16f
    var range = 10f

    var attackCooldown = 500L
    var attacked = 0L

    var stunnedMillis = 500L
    var stunned = false
    var stunTime = 0L

    init {
        maxSpeed = 25f
        deceleration = 100f
    }

    fun stun() {
        stunTime = System.currentTimeMillis()
        stunned = true
    }

    fun attack() {
        attacked = System.currentTimeMillis()
    }

    fun canAttack() = System.currentTimeMillis() - attacked > attackCooldown

    override fun isDead() = !state.isAlive()

    override fun act(dt: Float) {
        super.act(dt)
        if (stunned && System.currentTimeMillis() - stunTime > stunnedMillis) {
            stunned = false
        }
    }
}

class Orc(x: Float, y: Float, s: Stage) : Monster(x, y, s, 20f, 5f, 10f,
        TextureHelper.loadAnimation("orc-warrior-walk-right.png", 16, 20, 0, 4),
        TextureHelper.loadAnimation("orc-warrior-walk-left.png", 16, 20, 0, 4),
        TextureHelper.loadAnimation("orc-warrior-idle-right.png", 16, 20, 0, 4),
        TextureHelper.loadAnimation("orc-warrior-idle-left.png", 16, 20, 0, 4),
        TextureHelper.loadAnimation("orc-warrior-idle-right.png", 16, 20, 0, 4, loop = false),
        TextureHelper.loadAnimation("orc-warrior-idle-left.png", 16, 20, 0, 4, loop = false)) {

    init {
        width = 16f
        height = 20f

        midPoint = Vector2(6f, 6f)
    }

    override fun boundaryVerticles(): FloatArray = floatArrayOf(2f, 0f, 13f, 0f, 13f, 5f, 2f, 5f)
}
