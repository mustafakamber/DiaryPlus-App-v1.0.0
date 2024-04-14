package com.example.diarybook.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.diarybook.adapter.DiaryAdapter
import com.example.diarybook.constant.Constant.CALENDAR_KEY
import com.example.diarybook.constant.Constant.NOTE_DATA
import com.example.diarybook.constant.Constant.NULL_STRING
import com.example.diarybook.constant.Constant.SELECTED_CALENDAR_DATE_EN
import com.example.diarybook.constant.Constant.SELECTED_CALENDAR_DATE_TR
import com.example.diarybook.databinding.FragmentCalendarBinding
import com.example.diarybook.model.Diary
import com.example.diarybook.view.activity.DiaryActivity
import com.example.diarybook.view.dialog.DatePickerDialog
import com.example.diarybook.viewmodel.CalendarViewModel

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var viewModel: CalendarViewModel

    private var selectedDate: String = ""
    private var currentDate: String = ""
    private var selectedDateCalendarViewEn: String? = null
    private var selectedDateCalendarViewTr: String? = null

    private var diaryAdapter = DiaryAdapter(arrayListOf())
    private lateinit var datePicker: DatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[CalendarViewModel::class.java]
        datePicker = DatePickerDialog(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        setupCalendarScreen()
        observeLiveData()
    }

    override fun onResume() {
        super.onResume()
        if (selectedDate != NULL_STRING) {
            viewModel.getDiariesForSelectedDate(selectedDate)
        } else {
            viewModel.getDiariesForSelectedDate(currentDate)
        }
    }

    private fun setupCalendarScreen() = with(binding) {
        currentDate = datePicker.currentDateEn()

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            datePicker.selectedDate(year, month, dayOfMonth) { dateEn, dateTr ->
                selectedDate = dateEn
                selectedDateCalendarViewEn = dateEn
                selectedDateCalendarViewTr = dateTr
                viewModel.getDiariesForSelectedDate(selectedDate)
            }
        }

        calendarSwipeRefresh.setOnRefreshListener {
            calendarSwipeRefresh.isRefreshing = true
            if (selectedDate != NULL_STRING) {
                viewModel.refreshCalendarFragment(selectedDate)
            } else {
                viewModel.refreshCalendarFragment(currentDate)
            }
        }

        calendarAddNoteButton.setOnClickListener {
            navigateDiaryScreenToAddDiary()
        }
    }

    private fun navigateDiaryScreenToAddDiary() {
        viewModel.saveBooleanData(NOTE_DATA, true)
        val intentToDiaryActivity = Intent(requireActivity(), DiaryActivity::class.java)
        if (selectedDateCalendarViewEn != null && selectedDateCalendarViewTr != null) {
            intentToDiaryActivity
                .putExtra(SELECTED_CALENDAR_DATE_EN, selectedDateCalendarViewEn)
                .putExtra(SELECTED_CALENDAR_DATE_TR, selectedDateCalendarViewTr)
        }
        startActivity(intentToDiaryActivity)
    }

    private fun showDiaries(diaries: ArrayList<Diary>) = with(binding) {
        diaryAdapter.updateDiaryList(diaries)
        calendarRecyclerView.adapter = diaryAdapter
    }

    private fun observeLiveData() = with(binding) {
        viewModel.toastMessage.observe(
            viewLifecycleOwner
        ) { toastMessage ->
            toastMessage?.let {
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
                calendarRecyclerView.visibility = View.GONE
            }
        }

        viewModel.diaryView.observe(
            viewLifecycleOwner
        ) { diaries ->
            diaries?.let {
                calendarRecyclerView.visibility = View.VISIBLE
                showDiaries(diaries as ArrayList<Diary>)
                calendarSwipeRefresh.isRefreshing = false
                calendarNoteErrorMessage.visibility = View.GONE
                calendarNoteNullMessage.visibility = View.GONE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                if (it) {
                    calendarSwipeRefresh.isRefreshing = false
                    calendarRecyclerView.visibility = View.GONE
                    calendarNoteErrorMessage.visibility = View.VISIBLE
                } else {
                    calendarSwipeRefresh.isRefreshing = false
                    calendarNoteErrorMessage.visibility = View.GONE
                }
            }
        }

        viewModel.nullMessage.observe(
            viewLifecycleOwner
        ) { nullMessage ->
            nullMessage?.let {
                if (it) {
                    calendarSwipeRefresh.isRefreshing = false
                    calendarRecyclerView.visibility = View.GONE
                    calendarNoteErrorMessage.visibility = View.GONE
                    calendarNoteNullMessage.visibility = View.VISIBLE
                } else {
                    calendarSwipeRefresh.isRefreshing = false
                    calendarNoteNullMessage.visibility = View.GONE
                }
            }
        }
    }
}