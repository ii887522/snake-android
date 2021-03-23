package ii887522.snake.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ii887522.controlify.enums.Input
import ii887522.snake.model.GameViewModel
import ii887522.snake.R
import ii887522.snake.databinding.FragmentGameBinding

class GameFragment : Fragment() {
  @SuppressLint("ClickableViewAccessibility") override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val viewModel = ViewModelProvider(this)[GameViewModel::class.java]
    val binding = DataBindingUtil.inflate<FragmentGameBinding>(inflater,
      R.layout.fragment_game, container, false)
    binding.viewLifecycleOwner = viewLifecycleOwner
    binding.isLosingModalShowing = viewModel.isLosingModalShowing
    binding.isWinningModalShowing = viewModel.isWinningModalShowing
    binding.pScore = binding.score
    viewModel.isLosingModalShowing.observe(viewLifecycleOwner) {
      if (it) {
        binding.upButton.isEnabled = false
        binding.rightButton.isEnabled = false
        binding.downButton.isEnabled = false
        binding.leftButton.isEnabled = false
        viewModel.vibrate(requireActivity())
        viewModel.beginFade(container!!)
        binding.sceneCover.visibility = View.VISIBLE
        binding.gameOver.visibility = View.VISIBLE
      } else {
        binding.sceneCover.visibility = View.INVISIBLE
        binding.gameOver.visibility = View.INVISIBLE
        binding.upButton.isEnabled = true
        binding.rightButton.isEnabled = true
        binding.downButton.isEnabled = true
        binding.leftButton.isEnabled = true
      }
    }
    viewModel.isWinningModalShowing.observe(viewLifecycleOwner) {
      if (it) {
        binding.upButton.isEnabled = false
        binding.rightButton.isEnabled = false
        binding.downButton.isEnabled = false
        binding.leftButton.isEnabled = false
        viewModel.beginFade(container!!)
        binding.sceneCover.visibility = View.VISIBLE
        binding.youWin.visibility = View.VISIBLE
      } else {
        binding.sceneCover.visibility = View.INVISIBLE
        binding.youWin.visibility = View.INVISIBLE
        binding.upButton.isEnabled = true
        binding.rightButton.isEnabled = true
        binding.downButton.isEnabled = true
        binding.leftButton.isEnabled = true
      }
    }
    binding.gameOverBackButton.setOnClickListener {
      viewModel.backFromGameOver(requireActivity())
    }
    binding.gameOverPlayAgainButton.setOnClickListener {
      viewModel.playAgainWhenGameOver()
    }
    binding.youWinBackButton.setOnClickListener {
      viewModel.backFromYouWin(requireActivity())
    }
    binding.youWinPlayAgainButton.setOnClickListener {
      viewModel.playAgainWhenYouWin()
    }
    binding.upButton.setOnTouchListener { _, _ ->
      binding.world.reactInput(Input.UP)
      false
    }
    binding.rightButton.setOnTouchListener { _, _ ->
      binding.world.reactInput(Input.RIGHT)
      false
    }
    binding.downButton.setOnTouchListener { _, _ ->
      binding.world.reactInput(Input.DOWN)
      false
    }
    binding.leftButton.setOnTouchListener { _, _ ->
      binding.world.reactInput(Input.LEFT)
      false
    }
    return binding.root
  }
}
