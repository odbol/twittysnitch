package com.odbol.twittysnitch

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.odbol.twittysnitch.databinding.FragmentSecondBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

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

        val context = requireContext()
        val tweetRepo = TweetRepo(context)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        binding.buttonExport.setOnClickListener {
            val originalText = binding.buttonExport.text
            binding.buttonExport.text = getString(R.string.exporting)
            binding.buttonExport.isEnabled = false
            FileExporter(context, tweetRepo)
                .exportAsEmail()
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally {
                    binding.buttonExport.text = originalText
                    binding.buttonExport.isEnabled = true
                }
                .subscribe(
                    {},
                    // onError
                    { error ->
                        Log.e(TAG, "Failed to save file", error);

                        Toast.makeText(context, R.string.export_error, Toast.LENGTH_LONG).show();
                    }
                )

        }

        binding.tweetText.text = ""
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

    companion object {
        private const val TAG = "SecondFragment"
    }
}