package ii887522.snake.control

import android.graphics.Canvas
import android.graphics.Paint
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import ii887522.controlify.any.Control
import ii887522.controlify.struct.IntPoint
import ii887522.controlify.struct.IntRect
import ii887522.controlify.any.Map
import ii887522.snake.enums.CellType
import kotlin.random.Random

/**
 * **Not Thread Safe**: it must only be used in main thread
 */
class Food(lifecycleOwner: LifecycleOwner, private val wallRect: IntRect, private val cellSize: Int, private val random: Random, private val map: Map<CellType>,
           isEaten: MutableLiveData<Boolean>) : Control() {

  private val _position = MutableLiveData<IntPoint>()

  private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = 0xffffff00.toInt()
  }

  init {
    isEaten.observe(lifecycleOwner) {
      if (!it) return@observe
      spawn()
    }
    _position.observe(lifecycleOwner) {
      position.value = wallRect.position + it * cellSize
    }
    spawn()
  }

  private val randomSpawnablePosition: IntPoint
    get() {
      val l_position = IntPoint(random.nextInt(1, wallRect.size.w / cellSize - 1), random.nextInt(1, wallRect.size.h / cellSize - 1))
      while (true) {
        if (map[l_position] != CellType.BACKGROUND.ordinal) {
          ++l_position.x
        } else {
          break
        }
        if (l_position.x >= wallRect.size.w / cellSize - 1) {
          l_position.x = 1
          ++l_position.y
        }
        if (l_position.y >= wallRect.size.h / cellSize - 1) {
          l_position.y = 1
        }
      }
      return l_position
    }

  private fun spawn() {
    _position.value = randomSpawnablePosition
    map[_position.value!!] = CellType.FOOD
  }

  override fun render(canvas: Canvas) {
    canvas.drawRect(position.value!!.x.toFloat(), position.value!!.y.toFloat(), (position.value!!.x + cellSize).toFloat(), (position.value!!.y + cellSize).toFloat(), paint)
  }
}
