package com.cartravelsdailerapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.FragmentAutoCallListFrgBinding


class AutoCallListFrg : Fragment() {
    lateinit var binding: FragmentAutoCallListFrgBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAutoCallListFrgBinding.inflate(layoutInflater)

        return binding.root
    }

}