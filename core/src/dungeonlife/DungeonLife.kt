package dungeonlife

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class MenuScreen : BaseScreen() {
    init {
        val uiTable = Table()
        uiTable.setFillParent(true);
        uiStage.addActor(uiTable);

        val startButtonStyle = TextButtonStyle()
        startButtonStyle.up = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("btn-start.png"))))
        var startButton = Button(startButtonStyle)

        val quitButtonStyle = TextButtonStyle()
        quitButtonStyle.up = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("btn-quit.png"))))
        var quitButton = Button(quitButtonStyle)

        quitButton.addListener { e: Event ->
            if (e !is InputEvent) return@addListener false
            if (!(e as InputEvent).getType().equals(Type.touchDown)) return@addListener false
            Gdx.app.exit()
            true
        }

        uiTable.row();
        uiTable.add(startButton);
        uiTable.add(quitButton);
    }

    override fun keyDown(keyCode: Int): Boolean {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) Gdx.app.exit()
        return false
    }
}

class MapScreen: BaseScreen() {
    init {
        val map = MapReader.readMap("backyard.json")
        // add tiles to main Screen
        // this.mainStage
    }
}

// object with hero state


//object worldBounds {
//    var width: Float = 0f;
//    var height: Float = 0f;
//}

//class Dungeon : Actor {
//    constructor(x: Float, y: Float, s: Stage) {
//        this.x = x
//        this.y = y
//        s.addActor(this)
//
//        this.width = 3500f
//        this.height = 4000f
//
//        worldBounds.width = this.width
//        worldBounds.height = this.height
//    }
//
//    var texture = TextureRegion(Texture(Gdx.files.internal("sample-bg.jpg")))
//
//    override fun draw(batch: Batch, parentAlpha: Float) {
//        super.draw(batch, parentAlpha)
//
//        batch.draw(texture,
//                x, y, originX, originY,
//                width, height, scaleX, scaleY, rotation)
//    }
//}

class Knight(x: Float, y: Float, stage: Stage) : BaseActor(x, y, stage) {
    val anim1 = textureHelper.loadAnimation("knight_l.png", 16, 32, 1, 9)//loadAnimationFromFiles()
    val anim2 = textureHelper.loadAnimation("knight_r.png", 16, 32, 1, 9)//loadAnimationFromFiles()

    fun anim1() {
        animation = anim1
    }

    fun anim2() {
        animation = anim2
    }

    override fun act(dt: Float) {
        super.act(dt)
        alignCamera()
    }
}

fun Stage.knight(init: Knight.() -> Unit): Knight {
    val actor = Knight(0f, 0f, this)
    actor.init()
    return actor
}

class LevelScreen : BaseScreen() {

    //val dungeon = Dungeon(0f, 0f, this.mainStage)
    val world = World(mainStage)
    val knight = mainStage.knight {
        x = world.spawnPoint.x
        y = world.spawnPoint.y
        width = 16f
        height = 32f
        animation = anim1
        maxSpeed = 200f
        deceleration = 300f
        moveByPossibleFun = { dx, dy -> world.inside(this.x + dx, this.y + dy) }
    }
    val sword = Sword(knight, mainStage)

    override fun render(dt: Float) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            knight.accelerateAtAngle(180f)
            knight.anim1()
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            knight.accelerateAtAngle(0f)
            knight.anim2()
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            knight.accelerateAtAngle(90f)
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            knight.accelerateAtAngle(270f)

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
            if (!sword.hasActions())
                sword.addAction(Actions.rotateBy(360f, 0.25f))

        super.render(dt)
    }
}

object DungeonLife : Game() {
    override fun create() {
        Gdx.input.inputProcessor = InputMultiplexer()
        screen = MenuScreen()
        // a hack?
        screen.show()
    }

    override fun dispose() {
        screen.dispose()
    }
}