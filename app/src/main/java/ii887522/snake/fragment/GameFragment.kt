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
import ii887522.snake.any.GameViewModel
import ii887522.snake.R
import ii887522.snake.databinding.FragmentGameBinding

class GameFragment : Fragment() {
  @SuppressLint("ClickableViewAccessibility") override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val viewModel = ViewModelProvider(this)[GameViewModel::class.java]
    val binding = DataBindingUtil.inflate<FragmentGameBinding>(inflater, R.layout.fragment_game, container, false)
    binding.viewLifecycleOwner = viewLifecycleOwner
    binding.isLosingModalShowing = viewModel.isLosingModalShowing
    binding.isWinningModalShowing = viewModel.isWinningModalShowing
    binding.pScore = binding.score
    viewModel.isLosingModalShowing.observe(viewLifecycleOwner) {
      if (it) {
        binding.gamepad.upButton.isEnabled = false
        binding.gamepad.rightButton.isEnabled = false
        binding.gamepad.downButton.isEnabled = false
        binding.gamepad.leftButton.isEnabled = false
        viewModel.vibrate(requireActivity())
        viewModel.beginFade(container!!)
        binding.sceneCover.visibility = View.VISIBLE
        binding.gameOver.layout.visibility = View.VISIBLE
      } else {
        binding.sceneCover.visibility = View.INVISIBLE
        binding.gameOver.layout.visibility = View.INVISIBLE
        binding.gamepad.upButton.isEnabled = true
        binding.gamepad.rightButton.isEnabled = true
        binding.gamepad.downButton.isEnabled = true
        binding.gamepad.leftButton.isEnabled = true
      }
    }
    viewModel.isWinningModalShowing.observe(viewLifecycleOwner) {
      if (it) {
        binding.gamepad.upButton.isEnabled = false
        binding.gamepad.rightButton.isEnabled = false
        binding.gamepad.downButton.isEnabled = false
        binding.gamepad.leftButton.isEnabled = false
        viewModel.beginFade(container!!)
        binding.sceneCover.visibility = View.VISIBLE
        binding.youWin.layout.visibility = View.VISIBLE
      } else {
        binding.sceneCover.visibility = View.INVISIBLE
        binding.youWin.layout.visibility = View.INVISIBLE
        binding.gamepad.upButton.isEnabled = true
        binding.gamepad.rightButton.isEnabled = true
        binding.gamepad.downButton.isEnabled = true
        binding.gamepad.leftButton.isEnabled = true
      }
    }
    binding.gameOver.backButton.setOnClickListener {
      viewModel.backFromGameOver(requireActivity())
    }
    binding.gameOver.playAgainButton.setOnClickListener {
      viewModel.playAgainWhenGameOver()
    }
    binding.youWin.backButton.setOnClickListener {
      viewModel.backFromYouWin(requireActivity())
    }
    binding.youWin.playAgainButton.setOnClickListener {
      viewModel.playAgainWhenYouWin()
    }
    binding.gamepad.upButton.setOnTouchListener { _, _ ->
      binding.world.reactInput(Input.UP)
      false
    }
    binding.gamepad.rightButton.setOnTouchListener { _, _ ->
      binding.world.reactInput(Input.RIGHT)
      false
    }
    binding.gamepad.downButton.setOnTouchListener { _, _ ->
      binding.world.reactInput(Input.DOWN)
      false
    }
    binding.gamepad.leftButton.setOnTouchListener { _, _ ->
      binding.world.reactInput(Input.LEFT)
      false
    }
    return binding.root
  }
}
