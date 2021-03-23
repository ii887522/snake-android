package ii887522.snake.functions

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import ii887522.snake.view.World

@BindingAdapter("lifecycleOwner") fun World.setLifecycleOwner(value: LifecycleOwner?) {
  if (value != null) lifecycleOwner = value
}

@BindingAdapter("isLosingModalShowing") fun World.setIsLosingModalShowing(value: MutableLiveData<*>?) {
  @Suppress("UNCHECKED_CAST") if (value != null) isLosingModalShowing = value as MutableLiveData<Boolean>
}

@BindingAdapter("isWinningModalShowing") fun World.setIsWinningModalShowing(value: MutableLiveData<*>?) {
  @Suppress("UNCHECKED_CAST") if (value != null) isWinningModalShowing = value as MutableLiveData<Boolean>
}

@BindingAdapter("score") fun World.setScore(value: TextView?) {
  if (value != null) score = value
}
