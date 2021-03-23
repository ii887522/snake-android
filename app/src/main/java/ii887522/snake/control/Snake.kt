package ii887522.snake.control

import android.graphics.Canvas
import android.graphics.Paint
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ii887522.controlify.any.Control
import ii887522.controlify.enums.Input
import ii887522.controlify.struct.IntRect
import ii887522.controlify.any.Map
import ii887522.snake.any.SnakeModel
import ii887522.snake.enums.CellType
import kotlin.random.Random

/**
 * **Not Thread Safe**: it must only be used in main thread
 * @see Control
 */
class Snake(lifecycleOwner: LifecycleOwner, wallRect: IntRect, cellSize: Int, random: Random, map: Map<CellType>, onHit: () -> Unit, isDead: LiveData<Boolean>,
            hasEatFood: MutableLiveData<Boolean>) : Control() {

  private val model = SnakeModel(wallRect.size, cellSize, random, map, hasEatFood, onHit)
  private val wallPosition = wallRect.position

  private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = 0xff00ff00.toInt()
  }

  init {
    isDead.observe(lifecycleOwner) {
      model.isDead = it
    }
  }

  override fun reactInput(input: Input) {
    model.reactInput(input)
  }

  override fun step(dt: Int) {
    model.step(dt)
  }

  override fun checkAndReactHits(dt: Int) {
    model.checkAndReactHits(dt)
  }

  override fun render(canvas: Canvas) {
    for (modelPosition in model.positions) {
      canvas.drawRect(
        wallPosition.x + modelPosition.now.x * model.cellSize, wallPosition.y + modelPosition.now.y * model.cellSize,
        wallPosition.x + (modelPosition.now.x + 1) * model.cellSize, wallPosition.y + (modelPosition.now.y + 1) * model.cellSize, paint
      )
    }
  }
}
