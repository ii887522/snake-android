package ii887522.snake.any

import android.app.Activity
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.transition.Fade
import androidx.transition.TransitionManager
import ii887522.snake.R

class GameViewModel : ViewModel() {
  val isLosingModalShowing = MutableLiveData(false)
  val isWinningModalShowing = MutableLiveData(false)

  private val fade = Fade().apply {
    duration = 125
    addTarget(R.id.scene_cover)
    addTarget(R.id.game_over)
    addTarget(R.id.you_win)
  }

  private val oneShotVibrationEffect = VibrationEffect.createOneShot(256, 255)

  fun vibrate(activity: Activity) {
    (activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(oneShotVibrationEffect)
  }

  fun beginFade(container: ViewGroup) {
    TransitionManager.beginDelayedTransition(container, fade)
  }

  fun backFromGameOver(activity: Activity) {
    activity.onBackPressed()
    isLosingModalShowing.value = false
  }

  fun playAgainWhenGameOver() {
    isLosingModalShowing.value = false
  }

  fun backFromYouWin(activity: Activity) {
    activity.onBackPressed()
    isWinningModalShowing.value = false
  }

  fun playAgainWhenYouWin() {
    isWinningModalShowing.value = false
  }
}
