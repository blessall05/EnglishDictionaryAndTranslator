package com.blessall05.translator.view

import androidx.fragment.app.Fragment
import com.blessall05.translator.model.DatabaseHelper

abstract class BaseFragment : Fragment() {
    protected val activity by lazy { requireActivity() as MainActivity }
    protected val toolbar by lazy { activity.binding.toolbar }
    protected val databaseHelper by lazy { DatabaseHelper(activity) }
}