package ii887522.snake.view

import android.animation.TimeAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import ii887522.controlify.any.ScoreModel
import ii887522.controlify.control.BorderView
import ii887522.controlify.control.ControlGroup
import ii887522.controlify.struct.Border
import ii887522.controlify.struct.IntPoint
import ii887522.controlify.struct.IntRect
import ii887522.controlify.struct.IntSize
import ii887522.controlify.any.Map
import ii887522.controlify.enums.Input
import ii887522.snake.R
import ii887522.snake.control.Food
import ii887522.snake.control.Snake
import ii887522.snake.enums.CellType
import java.lang.System.nanoTime
import kotlin.random.Random

const val cellSize = 16
const val wallCellsCount = 2

class World(context: Context, attrs: AttributeSet) : View(context, attrs) {
  lateinit var lifecycleOwner: LifecycleOwner
  private val random = Random(nanoTime())
  lateinit var isLosingModalShowing: MutableLiveData<Boolean>
  lateinit var isWinningModalShowing: MutableLiveData<Boolean>
  lateinit var score: TextView
  private lateinit var isModalShowing: LiveData<Boolean>
  private lateinit var canScoreReset: LiveData<Boolean>
  private val isSnakeEatFood = MutableLiveData(false)
  private lateinit var scene: ControlGroup

  private val timeAnimator = TimeAnimator().apply {
    setTimeListener { _, _, dt ->
      if (!isLaidOut) return@setTimeListener
      scene.step(dt.toInt())
      scene.checkAndReactHits(dt.toInt())
      invalidate()
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
    super.onSizeChanged(w, h, oldW, oldH)
    isModalShowing = Transformations.switchMap(isLosingModalShowing) { left ->
      Transformations.map(isWinningModalShowing) { right ->
        left || right
      }
    }
    canScoreReset = Transformations.switchMap(isLosingModalShowing) { left ->
      Transformations.map(isWinningModalShowing) { right ->
        !(left || right)
      }
    }
    val map = Map<CellType>(IntSize(w / cellSize, h / cellSize))
    scene = ControlGroup(lifecycleOwner, IntPoint(), arrayOf(
      BorderView(Border(IntRect(IntPoint(0, 0), IntSize(w, h)), cellSize), Color.RED, map, CellType.WALL),
      Snake(lifecycleOwner, IntRect(IntPoint(0, 0), IntSize(w, h)), cellSize, random, map, {
        isLosingModalShowing.value = true
      }, isDead = isModalShowing, hasEatFood = isSnakeEatFood),
      Food(lifecycleOwner, IntRect(IntPoint(0, 0), IntSize(w, h)), cellSize, random, map, isSnakeEatFood)
    ))
    ScoreModel(lifecycleOwner, (((w / cellSize - wallCellsCount) * (h / cellSize - wallCellsCount)) * .75f).toInt(), {
      isWinningModalShowing.value = true
    }, canIncrement = isSnakeEatFood, canReset = canScoreReset).value.observe(lifecycleOwner) {
      score.text = resources.getString(R.string.score, it)
    }
  }

  fun reactInput(input: Input) {
    scene.reactInput(input)
  }

  override fun onDraw(canvas: Canvas?) {
    scene.render(canvas!!)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    timeAnimator.start()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    timeAnimator.cancel()
  }
}
