package dungeonlife

import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MenuScreen : BaseScreen() {
    init {
        val uiTable = Table()
        uiTable.setFillParent(true)
        uiStage.addActor(uiTable)

        val startButtonStyle = TextButtonStyle()
        startButtonStyle.up = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("btn-start.png"))))
        val startButton = Button(startButtonStyle)

        val quitButtonStyle = TextButtonStyle()
        quitButtonStyle.up = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("btn-quit.png"))))
        val quitButton = Button(quitButtonStyle)

        quitButton.addListener { e: Event ->
            if (e !is InputEvent) return@addListener false
            if (!e.getType().equals(Type.touchDown)) return@addListener false
            Gdx.app.exit()
            true
        }

        uiTable.row()
        uiTable.add(startButton)
        uiTable.add(quitButton)
    }

    override fun keyDown(keycode: Int): Boolean {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) Gdx.app.exit()
        return false
    }
}

abstract class MappedScreen(mapAsset: String) : BaseScreen() {
    // can walk just on "floor" types
    val tileTypesMap = HashMap<TileCoordinates, MutableSet<String>>()

    val knight: Knight
    val orcs = ArrayList<Orc>()
    val movables = LinkedList<Actor>()

    val debug: WhitePx
    val spawn: MapObject

    val weapon: Weapon

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
        spawn = (map.objectMap["spawn"] ?: error("No spawn point"))[0]
        knight = Knight(spawn.x, -spawn.y, mainStage)
        debug = WhitePx(spawn.x, -spawn.y, mainStage, TextureHelper.loadAnimation("white-px.png", 1, 1, 0, 1), knight)
        movables.add(knight)

        weapon = Weapon(mainStage, knight, 400L, 5f,
                TextureHelper.loadAnimation("weapon-anime-sword-idle-right.png", 30, 30, 0, 1),
                TextureHelper.loadAnimation("weapon-anime-sword-idle-left.png", 30, 30, 0, 1),
                TextureHelper.loadAnimation("weapon-anime-sword-hit-right.png", 30, 30, 0, 1, loop = false),
                TextureHelper.loadAnimation("weapon-anime-sword-hit-left.png", 30, 30, 0, 1, loop = false)
        )

        knight.moveByPossibleFun = { dx, dy ->
            floorAt(knight.x + dx + 10, knight.y + dy - 1) &&
                    floorAt(knight.x + dx + 22, knight.y + dy - 1) &&
                    floorAt(knight.x + dx + 10, knight.y + dy + 6) &&
                    floorAt(knight.x + dx + 22, knight.y + dy + 6)
        }
        initOrcs(map.objectMap["orc"])
    }

    private fun initOrcs(orcObjects: List<MapObject>?) {
        orcObjects?.forEach {
            val orc = Orc(it.x, -it.y, mainStage)

            orc.moveByPossibleFun = { dx, dy ->
                floorAt(orc.x + dx + 2, orc.y + dy - 1) &&
                        floorAt(orc.x + dx + 13, orc.y + dy - 1) &&
                        floorAt(orc.x + dx + 2, orc.y + dy + 5) &&
                        floorAt(orc.x + dx + 13, orc.y + dy + 5)
            }

            orcs.add(orc)
            movables.add(orc)
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

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE -> Gdx.app.exit()
        }

        return false
    }

    override fun render(dt: Float) {
        super.render(dt)

        if (!knight.stunned) {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                knight.accelerateAtAngle(180f)
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                knight.accelerateAtAngle(0f)
            if (Gdx.input.isKeyPressed(Input.Keys.UP))
                knight.accelerateAtAngle(90f)
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
                knight.accelerateAtAngle(270f)
        }

        // FIXME move to level class?
        orcs.forEach {
            // detection range
            if (it.distanceFrom(knight) < it.range && !it.stunned) {
                if (it.canAttack()) {
                    it.attack()
                    //knight.stun()
                    knight.heroState.decreaseHealth(knight.heroState.attack)
                    BloodBucket.freshBlood(knight.pos(), this.mainStage)
                }
            }

            if (it.distanceFrom(knight) < it.sight && !it.isDead() && !it.stunned) {
                it.moveTowards(knight)
            }

            if (it.overlaps(knight) && !it.isDead()) {
                it.stop()
                knight.stop()
            }
        }

        movables.sortByDescending { it.y }
        movables.forEachIndexed { index, actor -> actor.setZIndex(100000 + index) }
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

class EnlightenedBackyardScreen(mapAsset: String) : MappedScreen(mapAsset) {

    val rayHandler: RayHandler
    val pointLight: PointLight

    init {
        rayHandler = RayHandler(World(Vector2(0f, 0f), false))
        rayHandler.setCombinedMatrix(mainStage.camera.combined, 0f, 0f,
                mainStage.viewport.screenWidth.toFloat(),
                mainStage.viewport.screenWidth.toFloat())   //<-- pass your camera combined matrix
        pointLight = PointLight(rayHandler, 10, Color.WHITE, 300f, knight.x, knight.y);
    }

    override fun render(dt: Float) {
        pointLight.setPosition(320f, 240f)
        super.render(dt)

        if (tilesAt(knight.x + 10, knight.y - 1).contains("stairs") ||
                tilesAt(knight.x + 22, knight.y - 1).contains("stairs") ||
                tilesAt(knight.x + 10, knight.y + 6).contains("stairs") ||
                tilesAt(knight.x + 22, knight.y + 6).contains("stairs")) {

            reset()
            DungeonLife.level()
        }
        rayHandler.updateAndRender()
    }
}

class LevelScreen(mapAsset: String) : MappedScreen(mapAsset) {

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.SPACE -> {
                if (!knight.stunned) {
                    // swing sword
                    weapon.hit()

                    // attack all orcs in range
                    orcs.forEach {
                        if (it.distanceFrom(knight) < weapon.range) {
                            println("orc ${it.pos()} knight ${knight.pos()}")

                            it.setSpeed(500f)
                            it.motionAngle(it.pos().sub(knight.pos()).angle());

                            it.stun()
                            it.state.decreaseHealth(knight.heroState.attack)
                            BloodBucket.freshBlood(it.pos(), this.mainStage)
                        }
                    }
                }
            }
        }

        return false
    }

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
        (level ?: error("No level screen")).reset()
        screen = level
        screen.show()
    }

    fun backyard() {
        (backyard ?: error("No backyard screen")).reset()
        screen = backyard
        screen.show()
    }

    override fun dispose() {
        screen.dispose()
    }
}