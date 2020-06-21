package dungeonlife

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import kotlin.random.Random

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

class MapScreen : BaseScreen() {
    init {
        val map = MapReader.readMap("backyard.json")
        val textureMap = HashMap<Int, TextureRegion>()
        map.textures.forEach { texture -> textureMap.putAll(TextureMapReader.readTextureMap(texture.firstGid, texture.source)) }
        map.tileMap.forEach { tileCoordinates, mapTile ->
            mapTile.gids.forEach {
                Tile(textureMap[it]!!, tileCoordinates.x, -tileCoordinates.y, map.tileWidth, mainStage)
            }
        }
    }

    override fun keyDown(keyCode: Int): Boolean {
        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            mainStage.camera.position.y+=20
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            mainStage.camera.position.y-=20
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            mainStage.camera.position.x+=20
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            mainStage.camera.position.x-=20

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) Gdx.app.exit()

        return false
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

object DungeonLife : Game() {
    override fun create() {
        Gdx.input.inputProcessor = InputMultiplexer()
        screen = MapScreen()
        // a hack?
        screen.show()
    }

    override fun dispose() {
        screen.dispose()
    }
}