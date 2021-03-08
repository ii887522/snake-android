package ii887522.snake

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ii887522.snake.databinding.FragmentMainBinding

class MainFragment : Fragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val binding: FragmentMainBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
    binding.playButton.setOnClickListener {
      findNavController().navigate(MainFragmentDirections.actionMainFragmentToGameFragment())
    }
    return binding.root
  }
}
