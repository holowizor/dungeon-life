package dungeonlife

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import kotlin.random.Random

open class BaseScreen : Screen, InputProcessor {
    val mainStage = Stage()
    val uiStage = Stage()

    override fun hide() {
        val im = Gdx.input.inputProcessor as InputMultiplexer
        im.removeProcessor(this)
        im.removeProcessor(uiStage)
        im.removeProcessor(mainStage)
    }

    override fun show() {
        val im = Gdx.input.inputProcessor as InputMultiplexer
        im.addProcessor(this)
        im.addProcessor(uiStage)
        im.addProcessor(mainStage)
    }

    override fun render(dt: Float) {
        mainStage.act(dt);
        uiStage.act(dt);

        // clear the screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw the graphics
        mainStage.draw();
        uiStage.draw()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun dispose() {
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }
}

abstract class BaseActor : Actor {
    constructor(x: Float, y: Float, s: Stage) {
        this.x = x
        this.y = y
        s.addActor(this)
    }

    lateinit var animation: Animation<TextureRegion>

    private var elapsedTime = 0f
    private val velocityVec: Vector2 = Vector2(0f, 0f)
    private val accelerationVec: Vector2 = Vector2(0f, 0f)
    private var acceleration = 400f
    var maxSpeed = 100f
    var deceleration = 400f
    var moveByPossibleFun: (dx: Float, dy: Float) -> Boolean = { dx, dy -> true }

    fun alignCamera() {
        val cam: Camera = stage.camera

        val bW = cam.viewportWidth / 4
        val bH = cam.viewportHeight / 4

        val camx = cam.position.x
        val camy = cam.position.y

        if (x > camx && x - camx > bW) {
            cam.position.x = x + originX - bW
        }
        if (x < camx && camx - x > bW) {
            cam.position.x = x + originX + bW
        }

        if (y > camy && y - camy > bH) {
            cam.position.y = y + originY - bH
        }
        if (y < camy && camy - y > bH) {
            cam.position.y = y + originY + bH
        }

        cam.update()
    }

    fun accelerateAtAngle(angle: Float) {
        accelerationVec.add(Vector2(acceleration, 0f).setAngle(angle))
    }

    private fun setSpeed(speed: Float) {
        if (velocityVec.len() == 0f) velocityVec.set(speed, 0f) else velocityVec.setLength(speed)
    }

    private fun applyPhysics(dt: Float) {
        // apply acceleration
        velocityVec.add(accelerationVec.x * dt, accelerationVec.y * dt)
        var speed: Float = velocityVec.len()

        // decrease speed (decelerate) when not accelerating
        if (accelerationVec.len() == 0f) speed -= deceleration * dt

        // keep speed within set bounds
        speed = MathUtils.clamp(speed, 0f, maxSpeed)

        // update velocity
        setSpeed(speed)

        // update position according to value stored in velocity vector
        if (moveByPossibleFun(velocityVec.x * dt, velocityVec.y * dt)) {
            moveBy(velocityVec.x * dt, velocityVec.y * dt)
        }

        // reset acceleration
        accelerationVec[0f] = 0f
    }

    override fun act(dt: Float) {
        super.act(dt)
        elapsedTime += dt;
        applyPhysics(dt)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(animation.getKeyFrame(elapsedTime),
                x, y, originX, originY,
                width, height, scaleX, scaleY, rotation);
        super.draw(batch, parentAlpha)
    }
}

class World(s: Stage) : Actor() {
    val worldData = arrayOf(
            "x", "x", "x", "x", "x", "x", "x", "x", "x", "x", "x", "x",
            "x", "x", "x", "x", "x", "x", "x", "x", "x", "x", "x", "x",
            "x", "x", "x", "x", "x", "x", "o", "o", "x", "x", "x", "x",
            "x", "x", "x", "x", "x", "o", "s", "o", "x", "x", "x", "x",
            "x", "x", "x", "x", "o", "o", "o", "o", "x", "x", "x", "x",
            "x", "x", "x", "x", "o", "o", "o", "o", "x", "x", "x", "x",
            "x", "x", "x", "o", "o", "o", "x", "x", "x", "x", "x", "x",
            "x", "x", "x", "o", "o", "o", "x", "x", "x", "x", "x", "x",
            "x", "x", "x", "o", "o", "o", "x", "x", "x", "x", "x", "x",
            "x", "x", "x", "o", "o", "o", "x", "x", "x", "x", "x", "x",
            "x", "x", "x", "o", "o", "o", "o", "x", "x", "x", "x", "x",
            "x", "x", "x", "o", "o", "o", "o", "o", "x", "x", "x", "x",
            "x", "x", "x", "x", "x", "x", "o", "o", "o", "o", "o", "x",
            "x", "x", "x", "x", "x", "x", "x", "x", "o", "o", "o", "x",
            "x", "x", "x", "x", "x", "x", "x", "x", "o", "o", "o", "x",
            "x", "x", "x", "x", "x", "x", "x", "x", "o", "o", "o", "x",
            "x", "x", "x", "x", "x", "x", "o", "o", "o", "o", "o", "x",
            "x", "x", "o", "o", "o", "o", "o", "o", "x", "x", "x", "x",
            "x", "x", "o", "o", "o", "x", "x", "x", "x", "x", "x", "x",
            "x", "x", "x", "x", "o", "o", "o", "x", "x", "x", "x", "x",
            "x", "x", "o", "o", "o", "o", "o", "x", "x", "o", "o", "x",
            "x", "x", "o", "o", "o", "o", "o", "o", "o", "o", "o", "x",
            "x", "x", "x", "o", "o", "o", "o", "o", "o", "o", "o", "x",
            "x", "x", "x", "o", "o", "o", "o", "o", "o", "o", "o", "x",
            "x", "x", "o", "o", "x", "o", "o", "x", "o", "o", "x", "x",
            "x", "x", "o", "x", "x", "x", "x", "x", "x", "x", "x", "x",
            "x", "o", "o", "x", "x", "x", "x", "x", "x", "x", "x", "x",
            "x", "x", "x", "x", "x", "x", "x", "x", "x", "x", "x", "x"
    )

    var spawnPoint = Vector2(0f, 0f)

    init {
        initFloor(s)
        findSpawnPoint();
    }

    private fun findSpawnPoint() {
        worldData.forEachIndexed { i, ch -> if (ch == "s") spawnPoint = Vector2((16 * (i % 12) + 8).toFloat(), (16 * (i / 12) + 8).toFloat()) }
    }

    private fun initFloor(s: Stage): Array<Tile> {
        return worldData.mapIndexed { index, ch -> Tile(index, ch, s) }.toTypedArray()
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
    }

    fun inside(x: Float, y: Float): Boolean = worldData[(y.toInt() / 16) * 12 + x.toInt() / 16] != "x"
}

class Tile(index: Int, val ch: String, s: Stage) : Actor() {
    companion object {
        private val floorTextureArr = arrayOf(
                textureHelper.loadTexture("floor_1.png"),
                textureHelper.loadTexture("floor_2.png"),
                textureHelper.loadTexture("floor_3.png"),
                textureHelper.loadTexture("floor_4.png"),
                textureHelper.loadTexture("floor_5.png"),
                textureHelper.loadTexture("floor_6.png")
        )
        private val floorEmpty = textureHelper.loadTexture("floor_empty.png")
    }

    private val tex: TextureRegion

    init {
        x = (index % 12).toFloat() * 16
        y = (index / 12).toFloat() * 16
        width = 16f
        height = 16f

        tex = when (ch) {
            "x" -> floorEmpty
            else -> floorTextureArr[Random.nextInt(0, 6)]
        }

        s.addActor(this)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(tex, x, y);
        super.draw(batch, parentAlpha)
    }
}

class Sword(val k: Knight, s: Stage) : Actor() {
    init {
        s.addActor(this)
    }

    private val tex = textureHelper.loadTexture("weapon_knight_sword.png")

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(tex, k.x, k.y, originX, originY, 16f, 16f, scaleX, scaleY, rotation);
        super.draw(batch, parentAlpha)
    }
}