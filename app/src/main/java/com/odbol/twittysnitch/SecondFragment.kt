package com.odbol.twittysnitch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.odbol.twittysnitch.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.tweetText.text = ""
        val tweetRepo = TweetRepo(requireContext())
        val dbFiles = tweetRepo.list()
        binding.tweetText.apply{
            append("${dbFiles.size} files total\n\n")
        }
        if (dbFiles.isNotEmpty()) {
            tweetRepo.load(dbFiles.first())
                .subscribe(
                    // onNext
                    {
                        binding.tweetText.apply{
                            append(it)
                            append("\n")
                        }
                    },
                    // onError
                    {
                        binding.tweetText.text = "Error: $it"
                    }
                )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}