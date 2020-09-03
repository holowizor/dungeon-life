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
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage


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

    protected var elapsedTime = 0f
    protected val velocityVec: Vector2 = Vector2(0f, 0f)
    protected val accelerationVec: Vector2 = Vector2(0f, 0f)
    val boundaryPolygon = Polygon(boundaryVerticles())
    var acceleration = 200f
    var maxSpeed = 50f
    var deceleration = 200f
    var moveByPossibleFun: (dx: Float, dy: Float) -> Boolean = { dx, dy -> true }
    var midPoint = Vector2(0f, 0f)

    open fun boundaryVerticles(): FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

    fun pos() = Vector2(x + midPoint.x, y + midPoint.y)

    fun thisBoundaryPolygon(): Polygon {
        boundaryPolygon.setPosition(x, y)
        boundaryPolygon.setOrigin(originX, originY)
        boundaryPolygon.setRotation(rotation)
        boundaryPolygon.setScale(scaleX, scaleY)
        return boundaryPolygon
    }

    open fun overlaps(other: BaseActor): Boolean {
        val poly1: Polygon = this.thisBoundaryPolygon()
        val poly2: Polygon = other.thisBoundaryPolygon()

        return if (!poly1.boundingRectangle.overlaps(poly2.boundingRectangle)) false else Intersector.overlapConvexPolygons(poly1, poly2)
    }

    fun distanceFrom(other: BaseActor) = pos().dst(other.pos())

    fun moveTowards(other: BaseActor) = accelerateAtAngle(other.pos().sub(this.pos()).angle())

    fun alignCamera() {
        val cam: Camera = stage.camera

        val bW = cam.viewportWidth / 4
        val bH = cam.viewportHeight / 4

        val camx = cam.position.x
        val camy = cam.position.y

        val x = this.x + midPoint.x
        val y = this.y + midPoint.y

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

    fun stop() {
        setSpeed(0.0f)
    }

    fun motionAngle(angle: Float) {
        velocityVec.setAngle(angle)
    }

    fun setSpeed(speed: Float) {
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
        } else {
            // bounce back
            val ang = velocityVec.angle()
            if (ang in listOf<Float>(0f, 90f, 180f, 270f))
                velocityVec.rotate(180f)
            else if (ang < 90f && ang > 270f)
                velocityVec.rotate(270f)
            else
                velocityVec.rotate(90f)
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

open class AnimatedActor(x: Float, y: Float, s: Stage,
                         val animRight: Animation<TextureRegion>,
                         val animLeft: Animation<TextureRegion>,
                         val animIdleRight: Animation<TextureRegion>,
                         val animIdleLeft: Animation<TextureRegion>,
                         val animDeadRight: Animation<TextureRegion>,
                         val animDeadLeft: Animation<TextureRegion>) : BaseActor(x, y, s) {
    var moveRight = true
    var idleAnim = true

    init {
        super.animation = animIdleRight
        moveRight = true
        idleAnim = true
    }

    override fun act(dt: Float) {
        super.act(dt)
        setActiveAnimation()
    }

    open fun isDead() = false

    private fun setActiveAnimation() {
        if (isDead()) {
            super.animation = if (moveRight) animDeadRight else animDeadLeft
        } else if (velocityVec.len() > 0f) {
            val tmp = moveRight
            moveRight = !(velocityVec.angle() > 90 && velocityVec.angle() < 270)
            if (tmp != moveRight || idleAnim) {
                elapsedTime = 0f
                super.animation = if (moveRight) animRight else animLeft
                idleAnim = false
            }
        } else {
            idleAnim = true
            super.animation = if (moveRight) animIdleRight else animIdleLeft
        }
    }
}