package ii887522.snake.any

import androidx.lifecycle.MutableLiveData
import ii887522.controlify.enums.Input
import ii887522.controlify.struct.FloatPoint
import ii887522.controlify.struct.IntPoint
import ii887522.controlify.struct.IntSize
import ii887522.controlify.any.Map
import ii887522.oxy.any.AnimatedAny
import ii887522.oxy.struct.FloatVector
import ii887522.oxy.struct.IntVector
import ii887522.snake.enums.CellType
import ii887522.snake.enums.Direction
import java.util.concurrent.LinkedTransferQueue
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 * **Not Thread Safe**
 */
class SnakeModel(private val wallSize: IntSize, val cellSize: Int, private val random: Random, private val map: Map<CellType>,
                 private val hasEatFood: MutableLiveData<Boolean>, private val onHit: () -> Unit) {

  private val duration = 62 // animation duration
  val positions = ArrayList<AnimatedAny<FloatPoint>>()
  private val speeds = ArrayList<IntVector>()
  private val inputs = LinkedTransferQueue<Input>()

  var isDead = false
    set(value) {
      field = value
      if (!value) reborn()
    }

  private var prevTailSpeed = IntVector()

  init {
    reborn()
  }

  /**
   * @param i it must be less than the size of positions and speeds
   */
  private fun isMoveUp(i: Int) = speeds[i].x == 0 && speeds[i].y < 0

  /**
   * @param i it must be less than the size of positions and speeds
   */
  private fun isMoveRight(i: Int) = speeds[i].x > 0 && speeds[i].y == 0

  /**
   * @param i it must be less than the size of positions and speeds
   */
  private fun isMoveDown(i: Int) = speeds[i].x == 0 && speeds[i].y > 0

  /**
   * @param i it must be less than the size of positions and speeds
   */
  private fun isMoveLeft(i: Int) = speeds[i].x < 0 && speeds[i].y == 0

  private val isEatFood get() = when {
    isMoveUp(0) -> map[positions.first().now.toIntPoint()] == CellType.FOOD.ordinal
    isMoveRight(0) -> map[(positions.first().now + FloatVector(.999f, .0f)).toIntPoint()] == CellType.FOOD.ordinal
    isMoveDown(0) -> map[(positions.first().now + FloatVector(.0f, .999f)).toIntPoint()] == CellType.FOOD.ordinal
    isMoveLeft(0) -> map[positions.first().now.toIntPoint()] == CellType.FOOD.ordinal
    else -> false
  }

  private fun reactEatFood(dt: Int) {
    grow(dt)
    hasEatFood.value = true
  }

  private fun grow(dt: Int) {
    when {
      isTailMoveUpPreviously -> growWhenTailMoveUpPreviously()
      isTailMoveRightPreviously -> growWhenTailMoveRightPreviously()
      isTailMoveDownPreviously -> growWhenTailMoveDownPreviously()
      isTailMoveLeftPreviously -> growWhenTailMoveLeftPreviously()
    }
    positions.last().set(positions.last().now + speeds.last().toFloatVector())
    positions.last().step(dt)
  }

  private fun fixPositions() {
    for (i in 0 until positions.size) {
      when {
        isMoveUp(i) -> positions[i].teleport(FloatPoint(positions[i].now.x, (positions[i].now.y + 1f).toInt().toFloat()))
        isMoveRight(i) -> positions[i].teleport(FloatPoint(positions[i].now.x.toInt().toFloat(), positions[i].now.y))
        isMoveDown(i) -> positions[i].teleport(FloatPoint(positions[i].now.x, positions[i].now.y.toInt().toFloat()))
        isMoveLeft(i) -> positions[i].teleport(FloatPoint((positions[i].now.x + 1f).toInt().toFloat(), positions[i].now.y))
      }
    }
  }

  private fun stop() {
    for (i in 0 until speeds.size) speeds[i] = IntVector()
    prevTailSpeed = IntVector()
  }

  private val isHitSelf get() = when {
    isMoveUp(0) -> map[positions.first().now.toIntPoint()] == CellType.SNAKE_BODY.ordinal
    isMoveRight(0) -> map[(positions.first().now + FloatVector(.999f, .0f)).toIntPoint()] == CellType.SNAKE_BODY.ordinal
    isMoveDown(0) -> map[(positions.first().now + FloatVector(.0f, .999f)).toIntPoint()] == CellType.SNAKE_BODY.ordinal
    isMoveLeft(0) -> map[positions.first().now.toIntPoint()] == CellType.SNAKE_BODY.ordinal
    else -> false
  }

  private val isHitTopWall get() = isMoveUp(0) && map[positions.first().now.toIntPoint()] == CellType.WALL.ordinal
  private val isHitRightWall get() = isMoveRight(0) && map[(positions.first().now + FloatVector(.999f, 0f)).toIntPoint()] == CellType.WALL.ordinal
  private val isHitBottomWall get() = isMoveDown(0) && map[(positions.first().now + FloatVector(0f, .999f)).toIntPoint()] == CellType.WALL.ordinal
  private val isHitLeftWall get() = isMoveLeft(0) && map[(positions.first().now.toIntPoint())] == CellType.WALL.ordinal

  private fun reactHit() {
    fixPositions()
    stop()
    isDead = true
    onHit()
  }

  private fun reactUpInput() {
    if (!isDead && speeds.first().y <= 0) speeds[0] = IntVector(0, -1)
  }

  private fun reactLeftInput() {
    if (!isDead && speeds.first().x <= 0) speeds[0] = IntVector(-1, 0)
  }

  private fun reactDownInput() {
    if (!isDead && speeds.first().y >= 0) speeds[0] = IntVector(0, 1)
  }

  private fun reactRightInput() {
    if (!isDead && speeds.first().x >= 0) speeds[0] = IntVector(1, 0)
  }

  private fun reactNextInput() {
    when (inputs.poll()) {
      Input.UP -> reactUpInput()
      Input.DOWN -> reactDownInput()
      Input.LEFT -> reactLeftInput()
      Input.RIGHT -> reactRightInput()
      else -> return
    }
  }

  private fun clearSnakeInMap() {
    for (position in positions) map[position.now.toIntPoint()] = CellType.BACKGROUND
  }

  private fun setRandomSpeed() {
    when (random.nextInt(Direction.values().size)) {
      Direction.UP.ordinal -> speeds.add(IntVector(0, -1))
      Direction.RIGHT.ordinal -> speeds.add(IntVector(1, 0))
      Direction.DOWN.ordinal -> speeds.add(IntVector(0, 1))
      Direction.LEFT.ordinal -> speeds.add(IntVector(-1, 0))
    }
    prevTailSpeed = speeds.first()
  }

  private fun reborn() {
    clearSnakeInMap()
    positions.clear()
    speeds.clear()
    inputs.clear()
    positions.add(AnimatedAny(value = IntPoint(wallSize.w / cellSize shr 1, wallSize.h / cellSize shr 1).toFloatPoint(), duration, onAnimationEnd = {
      when {
        isMoveUp(0) -> map[(positions.first().now + FloatVector(0f, 1f)).toIntPoint()] = CellType.BACKGROUND
        isMoveRight(0) -> map[(positions.first().now - FloatVector(1f, 0f)).toIntPoint()] = CellType.BACKGROUND
        isMoveDown(0) -> map[(positions.first().now - FloatVector(0f, 1f)).toIntPoint()] = CellType.BACKGROUND
        isMoveLeft(0) -> map[(positions.first().now + FloatVector(1f, 0f)).toIntPoint()] = CellType.BACKGROUND
      }
      if (isDead) return@AnimatedAny
      prevTailSpeed = speeds.last()
      reactNextInput()
      positions.first().set(positions.first().now + speeds.first().toFloatVector())
    }))
    map[positions.first().now.toIntPoint()] = CellType.SNAKE_HEAD
    setRandomSpeed()
    positions.first().set(positions.first().now + speeds.first().toFloatVector())
  }

  private val isTailMoveUpPreviously get() = prevTailSpeed.x == 0 && prevTailSpeed.y < 0
  private val isTailMoveRightPreviously get() = prevTailSpeed.x > 0 && prevTailSpeed.y == 0
  private val isTailMoveDownPreviously get() = prevTailSpeed.x == 0 && prevTailSpeed.y > 0
  private val isTailMoveLeftPreviously get() = prevTailSpeed.x < 0 && prevTailSpeed.y == 0

  /**
   * @param i it must be greater than 0 and less than the size of positions and speeds
   */
  private fun followPrevCell(i: Int) {
    when {
      positions[i].now.y > positions[i - 1].now.y -> speeds[i] = IntVector(0, -1)
      positions[i].now.x < positions[i - 1].now.x -> speeds[i] = IntVector(1, 0)
      positions[i].now.y < positions[i - 1].now.y -> speeds[i] = IntVector(0, 1)
      positions[i].now.x > positions[i - 1].now.x -> speeds[i] = IntVector(-1, 0)
    }
    if (!isDead) positions[i].set(positions[i].now + speeds[i].toFloatVector())
  }

  private fun growWhenTailMoveUpPreviously() {
    val i = positions.size
    when {
      isMoveRight(i - 1) ->
        positions.add(AnimatedAny(value = (positions.last().now + FloatVector(.0f, .1f)).toIntPoint().toFloatPoint(), duration, onAnimationEnd = {
          when {
            isMoveUp(i) -> map[(positions[i].now + FloatVector(.0f, 1f)).toIntPoint()] = CellType.BACKGROUND
            isMoveRight(i) -> map[(positions[i].now - FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
            isMoveDown(i) -> map[(positions[i].now - FloatVector(0f, 1f)).toIntPoint()] = CellType.BACKGROUND
            isMoveLeft(i) -> map[(positions[i].now + FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
          }
          followPrevCell(i)
        }))
      isMoveLeft(i - 1) ->
        positions.add(AnimatedAny(value = (positions.last().now + FloatVector(1f, 1f)).toIntPoint().toFloatPoint(), duration, onAnimationEnd = {
          when {
            isMoveUp(i) -> map[(positions[i].now + FloatVector(.0f, 1f)).toIntPoint()] = CellType.BACKGROUND
            isMoveRight(i) -> map[(positions[i].now - FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
            isMoveDown(i) -> map[(positions[i].now - FloatVector(0f, 1f)).toIntPoint()] = CellType.BACKGROUND
            isMoveLeft(i) -> map[(positions[i].now + FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
          }
          followPrevCell(i)
        }))
      else ->
        positions.add(AnimatedAny(value = (positions.last().now + FloatVector(.0f, 2f)).toIntPoint().toFloatPoint(), duration, onAnimationEnd = {
          when {
            isMoveUp(i) -> map[(positions[i].now + FloatVector(.0f, 1f)).toIntPoint()] = CellType.BACKGROUND
            isMoveRight(i) -> map[(positions[i].now - FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
            isMoveDown(i) -> map[(positions[i].now - FloatVector(0f, 1f)).toIntPoint()] = CellType.BACKGROUND
            isMoveLeft(i) -> map[(positions[i].now + FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
          }
          followPrevCell(i)
        }))
    }
    map[positions.last().now.toIntPoint()] = CellType.SNAKE_BODY
    speeds.add(IntVector(0, -1))
  }

  private fun growWhenTailMoveRightPreviously() {
    val i = positions.size
    when {
      isMoveUp(i - 1) -> positions.add(AnimatedAny(value = (positions.last().now + FloatVector(-1f, 1f)).toIntPoint().toFloatPoint(), duration, onAnimationEnd = {
        when {
          isMoveUp(i) -> map[(positions[i].now + FloatVector(.0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveRight(i) -> map[(positions[i].now - FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
          isMoveDown(i) -> map[(positions[i].now - FloatVector(0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveLeft(i) -> map[(positions[i].now + FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
        }
        followPrevCell(i)
      }))
      else -> positions.add(AnimatedAny(value = (positions.last().now - FloatVector(1f, 0f)).toIntPoint().toFloatPoint(), duration, onAnimationEnd = {
        when {
          isMoveUp(i) -> map[(positions[i].now + FloatVector(.0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveRight(i) -> map[(positions[i].now - FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
          isMoveDown(i) -> map[(positions[i].now - FloatVector(0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveLeft(i) -> map[(positions[i].now + FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
        }
        followPrevCell(i)
      }))
    }
    map[positions.last().now.toIntPoint()] = CellType.SNAKE_BODY
    speeds.add(IntVector(1, 0))
  }

  private fun growWhenTailMoveDownPreviously() {
    val i = positions.size
    when {
      isMoveLeft(i - 1) -> positions.add(AnimatedAny(value = (positions.last().now + FloatVector(1f, -1f)).toIntPoint().toFloatPoint(), duration, onAnimationEnd = {
        when {
          isMoveUp(i) -> map[(positions[i].now + FloatVector(.0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveRight(i) -> map[(positions[i].now - FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
          isMoveDown(i) -> map[(positions[i].now - FloatVector(0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveLeft(i) -> map[(positions[i].now + FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
        }
        followPrevCell(i)
      }))
      else -> positions.add(AnimatedAny(value = (positions.last().now - FloatVector(0f, 1f)).toIntPoint().toFloatPoint(), duration, onAnimationEnd = {
        when {
          isMoveUp(i) -> map[(positions[i].now + FloatVector(.0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveRight(i) -> map[(positions[i].now - FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
          isMoveDown(i) -> map[(positions[i].now - FloatVector(0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveLeft(i) -> map[(positions[i].now + FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
        }
        followPrevCell(i)
      }))
    }
    map[positions.last().now.toIntPoint()] = CellType.SNAKE_BODY
    speeds.add(IntVector(0, 1))
  }

  private fun growWhenTailMoveLeftPreviously() {
    val i = positions.size
    when {
      isMoveUp(i - 1) -> positions.add(AnimatedAny(value = (positions.last().now + FloatVector(1f, 1f)).toIntPoint().toFloatPoint(), duration, onAnimationEnd = {
        when {
          isMoveUp(i) -> map[(positions[i].now + FloatVector(.0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveRight(i) -> map[(positions[i].now - FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
          isMoveDown(i) -> map[(positions[i].now - FloatVector(0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveLeft(i) -> map[(positions[i].now + FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
        }
        followPrevCell(i)
      }))
      isMoveDown(i - 1) -> positions.add(AnimatedAny(value = (positions.last().now + FloatVector(1f, 0f)).toIntPoint().toFloatPoint(), duration, onAnimationEnd = {
        when {
          isMoveUp(i) -> map[(positions[i].now + FloatVector(.0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveRight(i) -> map[(positions[i].now - FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
          isMoveDown(i) -> map[(positions[i].now - FloatVector(0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveLeft(i) -> map[(positions[i].now + FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
        }
        followPrevCell(i)
      }))
      else -> positions.add(AnimatedAny(value = (positions.last().now + FloatVector(2f, 0f)).toIntPoint().toFloatPoint(), duration, onAnimationEnd = {
        when {
          isMoveUp(i) -> map[(positions[i].now + FloatVector(.0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveRight(i) -> map[(positions[i].now - FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
          isMoveDown(i) -> map[(positions[i].now - FloatVector(0f, 1f)).toIntPoint()] = CellType.BACKGROUND
          isMoveLeft(i) -> map[(positions[i].now + FloatVector(1f, .0f)).toIntPoint()] = CellType.BACKGROUND
        }
        followPrevCell(i)
      }))
    }
    map[positions.last().now.toIntPoint()] = CellType.SNAKE_BODY
    speeds.add(IntVector(-1, 0))
  }

  fun reactInput(input: Input) {
    if (inputs.isEmpty() || (inputs.last() != input)) inputs.add(input)
  }

  fun step(dt: Int) {
    for (position in positions) position.step(dt)
  }

  fun checkAndReactHits(dt: Int) {
    if (isEatFood) reactEatFood(dt)
    else if (isHitSelf || isHitTopWall || isHitRightWall || isHitBottomWall || isHitLeftWall) reactHit()
    when {
      isMoveUp(0) -> map[positions.first().now.toIntPoint()] = CellType.SNAKE_HEAD
      isMoveRight(0) -> map[(positions.first().now + FloatVector(.999f, 0f)).toIntPoint()] = CellType.SNAKE_HEAD
      isMoveDown(0) -> map[(positions.first().now + FloatVector(0f, .999f)).toIntPoint()] = CellType.SNAKE_HEAD
      isMoveLeft(0) -> map[positions.first().now.toIntPoint()] = CellType.SNAKE_HEAD
    }
    for (i in 1 until positions.size) {
      when {
        isMoveUp(i) -> map[positions[i].now.toIntPoint()] = CellType.SNAKE_BODY
        isMoveRight(i) -> map[(positions[i].now + FloatVector(.999f, 0f)).toIntPoint()] = CellType.SNAKE_BODY
        isMoveDown(i) -> map[(positions[i].now + FloatVector(0f, .999f)).toIntPoint()] = CellType.SNAKE_BODY
        isMoveLeft(i) -> map[positions[i].now.toIntPoint()] = CellType.SNAKE_BODY
      }
    }
  }
}
