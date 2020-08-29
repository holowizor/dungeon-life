package dungeonlife

import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MenuScreen : BaseScreen() {
    init {
        val logo = Logo(0f, 0f, this.mainStage)

        val uiTable = Table()
        uiTable.setFillParent(true)
        uiStage.addActor(uiTable)

        val startButtonStyle = TextButtonStyle()
        startButtonStyle.up = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("btn-start.png"))))
        val startButton = Button(startButtonStyle)

        val quitButtonStyle = TextButtonStyle()
        quitButtonStyle.up = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("btn-quit.png"))))
        val quitButton = Button(quitButtonStyle)

        startButton.addListener { e: Event ->
            if (e !is InputEvent) return@addListener false
            if (!e.getType().equals(Type.touchDown)) return@addListener false
            DungeonLife.level()
            true
        }

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
    val monsters = ArrayList<Monster>()
    val movables = LinkedList<Actor>()

    val spawn: MapObject

    val weapon: Weapon

    init {
        val map = MapReader.readMap(mapAsset)
        val textureMap = HashMap<Long, TypedTexture>()
        map.textures.forEach { texture -> textureMap.putAll(TextureMapReader.readTextureMap(texture.firstGid, texture.source)) }
        var idx = 0
        map.tileMap.forEach { tileCoordinates, mapTile ->
            mapTile.gids.forEach { gidz ->
                val originGid = gidz.gid and 0x0000FFFF
                textureMap[originGid]?.let { tt ->
                    Tile(tt.textureRegion, tileCoordinates.x, -tileCoordinates.y, map.tileWidth, (idx++) + gidz.z, mainStage)
                    tileTypesMap.getOrPut(TileCoordinates(tileCoordinates.x, -tileCoordinates.y)) { mutableSetOf() }.add(tt.type)
                }
            }
        }
        spawn = (map.objectMap["spawn"] ?: error("No spawn point"))[0]
        knight = Knight(spawn.x, -spawn.y, mainStage)
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
        initMaskedOrcs(map.objectMap["orc2"])
        initBoss(map.objectMap["boss"])
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

            monsters.add(orc)
            movables.add(orc)
        }
    }

    private fun initMaskedOrcs(orcObjects: List<MapObject>?) {
        orcObjects?.forEach {
            val orc = MaskedOrc(it.x, -it.y, mainStage)

            orc.moveByPossibleFun = { dx, dy ->
                floorAt(orc.x + dx + 2, orc.y + dy - 1) &&
                        floorAt(orc.x + dx + 13, orc.y + dy - 1) &&
                        floorAt(orc.x + dx + 2, orc.y + dy + 5) &&
                        floorAt(orc.x + dx + 13, orc.y + dy + 5)
            }

            monsters.add(orc)
            movables.add(orc)
        }
    }

    private fun initBoss(finalBossObjects: List<MapObject>?) {
        finalBossObjects?.forEach {
            val finalBoss = FinalBoss(it.x, -it.y, mainStage)


            finalBoss.moveByPossibleFun = { dx, dy ->
                floorAt(knight.x + dx + 10, knight.y + dy - 1) &&
                        floorAt(knight.x + dx + 22, knight.y + dy - 1) &&
                        floorAt(knight.x + dx + 10, knight.y + dy + 6) &&
                        floorAt(knight.x + dx + 22, knight.y + dy + 6)
            }

            monsters.add(finalBoss)
            movables.add(finalBoss)
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
        return tiles.size > 0 && tiles.contains("floor")
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

        monsters.forEach {
            // detection range
            if (it.distanceFrom(knight) < it.range && !it.isDead() && !it.stunned) {
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

class LevelScreen(mapAsset: String, val nextLevel: () -> Unit) : MappedScreen(mapAsset) {

    val rayHandler: RayHandler
    val pointLight: PointLight

    init {
        rayHandler = RayHandler(World(Vector2(0f, 0f), false))
        rayHandler.setCombinedMatrix(mainStage.camera.combined, 0f, 0f,
                mainStage.viewport.screenWidth.toFloat(),
                mainStage.viewport.screenWidth.toFloat())   //<-- pass your camera combined matrix
        pointLight = PointLight(rayHandler, 10, Color(0.75f, 0.75f, 0.5f, 0.75f), 300f, 0f, 0f);
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.SPACE -> {
                if (!knight.stunned) {
                    // swing sword
                    weapon.hit()

                    // attack monsters in range
                    monsters.forEach {
                        if (it.distanceFrom(knight) < weapon.range) {
                            println("monster ${it.pos()} knight ${knight.pos()}")

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

        val coords = knight.pos()
        knight.stage.stageToScreenCoordinates(coords)
        pointLight.setPosition(coords.x, 480f - coords.y)

        if (tilesAt(knight.x + 10, knight.y - 1).contains("stairs") ||
                tilesAt(knight.x + 22, knight.y - 1).contains("stairs") ||
                tilesAt(knight.x + 10, knight.y + 6).contains("stairs") ||
                tilesAt(knight.x + 22, knight.y + 6).contains("stairs")) {
            reset()
            nextLevel()
        }

        rayHandler.updateAndRender()
    }
}

object DungeonLife : Game() {

    // BAD DESIGN
    private var menuScreen: MenuScreen? = null
    private var level: LevelScreen? = null

    override fun create() {
        Gdx.input.inputProcessor = InputMultiplexer()

        // BAD DESIGN
        menuScreen = MenuScreen()
        level = LevelScreen("level3.json") { menu() }

        menu()
    }

    fun menu() {
        screen = menuScreen
        screen.show()
    }


    fun level() {
        (level ?: error("No level screen")).reset()
        screen = level
        screen.show()
    }

    override fun dispose() {
        screen.dispose()
    }
}