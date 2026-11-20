package com.smartbudget.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class PlaceholderFragment : Fragment(R.layout.fragment_placeholder) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.tvPlaceholderTitle).text =
            requireArguments().getString(ARG_TITLE).orEmpty()
        view.findViewById<TextView>(R.id.tvPlaceholderMessage).text =
            requireArguments().getString(ARG_MESSAGE).orEmpty()
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"

        fun newInstance(title: String, message: String) = PlaceholderFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
            }
        }
    }
}
