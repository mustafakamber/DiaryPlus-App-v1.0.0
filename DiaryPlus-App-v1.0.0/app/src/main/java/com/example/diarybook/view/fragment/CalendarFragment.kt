package com.example.diarybook.view.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diarybook.R
import com.example.diarybook.adapter.DiaryAdapter
import com.example.diarybook.databinding.FragmentCalendarBinding
import com.example.diarybook.model.Diary
import com.example.diarybook.constant.Constant.CALENDAR_KEY
import com.example.diarybook.constant.Constant.NOTE_DATA
import com.example.diarybook.constant.Constant.SELECTED_CALENDAR_DATE_EN
import com.example.diarybook.constant.Constant.SELECTED_CALENDAR_DATE_TR
import com.example.diarybook.view.activity.DiaryActivity
import com.example.diarybook.view.dialog.DatePickerDialog
import com.example.diarybook.viewmodel.CalendarViewModel
import kotlinx.android.synthetic.main.activity_base.*
import kotlin.collections.ArrayList

class CalendarFragment : Fragment() {


    private lateinit var calendarFragmentBinding: FragmentCalendarBinding

    private lateinit var calendarViewModel: CalendarViewModel


    private var selectedDate: String = ""
    private var currentDate: String = ""

    private var selectedDateCalendarViewEn : String? = null
    private var selectedDateCalendarViewTr : String? = null


    private var diaryAdapter = DiaryAdapter(arrayListOf())

    private lateinit var datePicker : DatePickerDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        calendarViewModel = ViewModelProvider(this)[CalendarViewModel::class.java]


        datePicker = DatePickerDialog(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        calendarFragmentBinding = FragmentCalendarBinding.inflate(layoutInflater)
        return calendarFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(calendarFragmentBinding) {
        super.onViewCreated(view, savedInstanceState)


        currentDate = datePicker.currentDateEn()

        calendarRecyclerView.layoutManager = LinearLayoutManager(context)

        observeLiveData()


        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->

           datePicker.selectedDate(year, month, dayOfMonth) { dateEn, dateTr ->

                selectedDate = dateEn
                selectedDateCalendarViewEn = dateEn
                selectedDateCalendarViewTr = dateTr

                calendarViewModel.getDiariesForSelectedDate(selectedDate)
           }

        }

        calendarSwipeRefresh.setOnRefreshListener {

            calendarSwipeRefresh.isRefreshing = true

            if (selectedDate != "") {
                calendarViewModel.refreshCalendarFragment(selectedDate)

            } else {
                calendarViewModel.refreshCalendarFragment(currentDate)
            }

        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backToHomeFragment()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)

        calendarAddNoteButton.setOnClickListener {
            getDiaryActivityToAddNote()
        }

    }

    override fun onPause() {
        calendarViewModel.saveBooleanData(CALENDAR_KEY, false)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        calendarViewModel.saveBooleanData(CALENDAR_KEY, true)

        if (selectedDate != ""){
            calendarViewModel.getDiariesForSelectedDate(selectedDate)
        }else{
            calendarViewModel.getDiariesForSelectedDate(currentDate)
        }

    }

    private fun getDiaryActivityToAddNote() {

        calendarViewModel.saveBooleanData(NOTE_DATA, true)

        val intentToDiaryActivity = Intent(requireActivity(), DiaryActivity::class.java)

        if(selectedDateCalendarViewEn != null && selectedDateCalendarViewTr != null){
            intentToDiaryActivity
                .putExtra(SELECTED_CALENDAR_DATE_EN,selectedDateCalendarViewEn)
                .putExtra(SELECTED_CALENDAR_DATE_TR,selectedDateCalendarViewTr)
        }

        startActivity(intentToDiaryActivity)
    }

    private fun backToHomeFragment() {

        requireActivity().bottomMenuView.selectedItemId = R.id.homeButton

        val fragment = HomeFragment()
        val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager
        fragmentManager.beginTransaction().apply {
            replace(R.id.baseFragment, fragment).commit()
        }

    }

    private fun showDiaries(diaries: ArrayList<Diary>) = with(calendarFragmentBinding) {

        diaryAdapter.updateDiaryList(diaries)
        calendarRecyclerView.adapter = diaryAdapter
    }

    private fun observeLiveData() = with(calendarFragmentBinding) {

        calendarViewModel.calendarToastMessage.observe(viewLifecycleOwner,
            Observer { toastMessage ->
                toastMessage?.let {

                    Toast.makeText(requireContext(),toastMessage, Toast.LENGTH_SHORT).show()
                    calendarRecyclerView.visibility = View.GONE

                }
            })

        calendarViewModel.calendarDiaryView.observe(viewLifecycleOwner,
            Observer { diaries ->
                diaries?.let {

                    calendarRecyclerView.visibility = View.VISIBLE
                    showDiaries(diaries as ArrayList<Diary>)
                    calendarSwipeRefresh.isRefreshing = false
                    calendarNoteErrorMessage.visibility = View.GONE
                    calendarNoteNullMessage.visibility = View.GONE

                }
            })

        calendarViewModel.calendarNotesErrorMessage.observe(viewLifecycleOwner,
            Observer { error ->
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
            })

        calendarViewModel.calendarNoteNullMessage.observe(viewLifecycleOwner,
            Observer { nullMessage ->
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
            })

    }


}