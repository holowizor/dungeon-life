package dungeonlife

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type
import com.badlogic.gdx.scenes.scene2d.Stage
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

abstract class MappedScreen(val mapAsset: String) : BaseScreen() {
    // can walk just on "floor" types
    val tileTypesMap = HashMap<TileCoordinates, MutableSet<String>>()
    val knight: Knight
    val orcs = ArrayList<Orc>()
    val debug: WhitePx
    val spawn: MapObject

    init {
        val map = MapReader.readMap(mapAsset)
        val textureMap = HashMap<Int, TypedTexture>()
        map.textures.forEach { texture -> textureMap.putAll(TextureMapReader.readTextureMap(texture.firstGid, texture.source)) }
        map.tileMap.forEach { tileCoordinates, mapTile ->
            mapTile.gids.forEach {
                // FIXME add empty texture here! with empty type!
                Tile(textureMap[it]!!.textureRegion, tileCoordinates.x, -tileCoordinates.y, map.tileWidth, mainStage)
                tileTypesMap.getOrPut(TileCoordinates(tileCoordinates.x, -tileCoordinates.y)) { mutableSetOf() }.add(textureMap[it]!!.type)
            }
        }
        spawn = map.objectMap["spawn"]!!
        knight = Knight(spawn.x, -spawn.y, mainStage,
                TextureHelper.loadAnimation("elite-knight-walk-right.png", 32, 32, 0, 4),
                TextureHelper.loadAnimation("elite-knight-walk-left.png", 32, 32, 0, 4),
                TextureHelper.loadAnimation("elite-knight-idle-right.png", 32, 32, 0, 1),
                TextureHelper.loadAnimation("elite-knight-idle-left.png", 32, 32, 0, 1))
        debug = WhitePx(spawn.x, -spawn.y, mainStage, TextureHelper.loadAnimation("white-px.png", 1, 1, 0, 1), knight)

        knight.moveByPossibleFun = { dx, dy ->
            floorAt(knight.x + dx + 10, knight.y + dy - 1) &&
                    floorAt(knight.x + dx + 22, knight.y + dy - 1) &&
                    floorAt(knight.x + dx + 10, knight.y + dy + 6) &&
                    floorAt(knight.x + dx + 22, knight.y + dy + 6)
        }
        initOrc(map.objectMap["orc"])
    }

    fun initOrc(orcObject: MapObject?) {
        orcObject?.let {
            orcs.add(Orc(orcObject.x, -orcObject.y, mainStage,
                    TextureHelper.loadAnimation("orc-warrior-walk-right.png", 16, 20, 0, 4),
                    TextureHelper.loadAnimation("orc-warrior-walk-left.png", 16, 20, 0, 4),
                    TextureHelper.loadAnimation("orc-warrior-idle-right.png", 16, 20, 0, 4),
                    TextureHelper.loadAnimation("orc-warrior-idle-left.png", 16, 20, 0, 4)))
        }
    }

    fun reset() {
        knight.x = spawn.x
        knight.y = -spawn.y
    }

    fun tilesAt(x: Float, y: Float): Set<String> {
        val adjx = if (x < 0) -1 else 0
        val adjy = if (y < 0) -1 else 0
        val tx = (x / 16f).toInt() + adjx
        val ty = (y / 16f).toInt() + adjy
        return tileTypesMap.getOrDefault(TileCoordinates(tx, ty), mutableSetOf())
    }

    fun floorAt(x: Float, y: Float): Boolean {
        val tiles = tilesAt(x, y)
        return tiles.size > 0 && !tiles.contains("wall")
    }

    override fun keyDown(keyCode: Int): Boolean {
        when (keyCode) {
            Input.Keys.ESCAPE -> Gdx.app.exit()
        }

        return false
    }

    override fun render(dt: Float) {
        super.render(dt)

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            knight.accelerateAtAngle(180f)
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            knight.accelerateAtAngle(0f)
        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            knight.accelerateAtAngle(90f)
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            knight.accelerateAtAngle(270f)

        orcs.forEach {
            if (it.distanceFrom(knight) < 5f*16f) {
                it.moveTowards(knight)
            } else {
                it.stop()
            }

            if (it.overlaps(knight)) {
                it.stop()
                knight.stop()
            }
        }
    }
}

class BackyardScreen(mapAsset: String) : MappedScreen(mapAsset) {

    override fun render(dt: Float) {
        super.render(dt)

        if (tilesAt(knight.x + 10, knight.y - 1).contains("stairs") ||
                tilesAt(knight.x + 22, knight.y - 1).contains("stairs") ||
                tilesAt(knight.x + 10, knight.y + 6).contains("stairs") ||
                tilesAt(knight.x + 22, knight.y + 6).contains("stairs")) {

            reset()
            DungeonLife.level()
        }
    }
}

class LevelScreen(mapAsset: String) : MappedScreen(mapAsset) {

    override fun render(dt: Float) {
        super.render(dt)

        if (tilesAt(knight.x + 10, knight.y - 1).contains("stairs") ||
                tilesAt(knight.x + 22, knight.y - 1).contains("stairs") ||
                tilesAt(knight.x + 10, knight.y + 6).contains("stairs") ||
                tilesAt(knight.x + 22, knight.y + 6).contains("stairs")) {
            reset()
            DungeonLife.backyard()
        }
    }
}

class Tile(val tex: TextureRegion, gridX: Int, gridY: Int, tileWidth: Int, s: Stage) : Actor() {
    init {
        x = gridX.toFloat() * 16
        y = gridY.toFloat() * 16
        width = tileWidth.toFloat()
        height = tileWidth.toFloat()

        s.addActor(this)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(tex, x, y);
        super.draw(batch, parentAlpha)
    }
}

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

class Knight(x: Float, y: Float, s: Stage, animRight: Animation<TextureRegion>, animLeft: Animation<TextureRegion>,
             animIdleRight: Animation<TextureRegion>, animIdleLeft: Animation<TextureRegion>) :
        AnimatedActor(x, y, s, animRight, animLeft, animIdleRight, animIdleLeft) {
    init {
        width = 32f
        height = 32f

        maxSpeed = 200f
        deceleration = 300f
    }

    //float[] vertices = {0,0, w,0, w,h, 0,h};
    override fun boundaryVerticles(): FloatArray = floatArrayOf(10f, 0f, 22f, 0f, 22f, 8f, 10f, 8f)

    override fun act(dt: Float) {
        super.act(dt)
        alignCamera()
    }
}

class Orc(x: Float, y: Float, s: Stage, animRight: Animation<TextureRegion>, animLeft: Animation<TextureRegion>,
          animIdleRight: Animation<TextureRegion>, animIdleLeft: Animation<TextureRegion>) :
        AnimatedActor(x, y, s, animRight, animLeft, animIdleRight, animIdleLeft) {
    init {
        width = 16f
        height = 20f

        maxSpeed = 100f
        deceleration = 500f
    }

    //float[] vertices = {0,0, w,0, w,h, 0,h};
    override fun boundaryVerticles(): FloatArray = floatArrayOf(2f, 0f, 13f, 0f, 13f, 5f, 2f, 5f)

    override fun act(dt: Float) {
        super.act(dt)
    }
}

object DungeonLife : Game() {

    // BAD DESIGN

    private var backyard: BackyardScreen? = null
    private var level: LevelScreen? = null

    override fun create() {
        Gdx.input.inputProcessor = InputMultiplexer()

        // BAD DESIGN

        backyard = BackyardScreen("backyard.json")
        level = LevelScreen("level1.json")

        backyard()
    }

    fun level() {
        level!!.reset()
        screen = level
        screen.show()
    }

    fun backyard() {
        backyard!!.reset()
        screen = backyard
        screen.show()
    }

    override fun dispose() {
        screen.dispose()
    }
}