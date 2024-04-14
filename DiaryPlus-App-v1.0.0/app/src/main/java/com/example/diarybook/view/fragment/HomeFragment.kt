package com.example.diarybook.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.diarybook.R
import com.example.diarybook.adapter.CalendarAdapter
import com.example.diarybook.adapter.DiaryAdapter
import com.example.diarybook.constant.Constant.NOTE_DATA
import com.example.diarybook.databinding.FragmentHomeBinding
import com.example.diarybook.model.Calendar
import com.example.diarybook.model.Diary
import com.example.diarybook.swipe.HomeDiarySwipe
import com.example.diarybook.swipe.HomePhotoSwipe
import com.example.diarybook.view.activity.DiaryActivity
import com.example.diarybook.viewmodel.HomeViewModel
import com.example.diarybook.viewmodel.UserViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var userViewModel: UserViewModel

    private lateinit var binding: FragmentHomeBinding
    private lateinit var filteredDiaries: ArrayList<Diary>
    private lateinit var bannerAdView: AdView
    private lateinit var inputMethodManager: InputMethodManager

    private var diaryAdapter = DiaryAdapter(arrayListOf())
    private var photoAdapter = CalendarAdapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        filteredDiaries = ArrayList()
        MobileAds.initialize(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHomeScreen()
        observeLiveData()
    }

    override fun onStop() {
        super.onStop()
        closeSearchView()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshHomeFragment()
    }

    private fun setupHomeScreen() = with(binding) {
        this@HomeFragment.bannerAdView = homeAdView
        val adRequest = AdRequest.Builder().build()
        this@HomeFragment.bannerAdView.loadAd(adRequest)

        homeCalendarRecyclerView.adapter = photoAdapter

        homeSwipeRefresh.setOnRefreshListener {
            homeSwipeRefresh.isRefreshing = true
            viewModel.refreshHomeFragment()
        }

        homeAddNoteButton.setOnClickListener {
            navigateDiaryScreenToAddDiary()
        }
    }

    private fun showCalendarPhotoWithSwiper(calendarPhoto: ArrayList<Calendar>) =
        with(binding) {
            photoAdapter.updateCalendarList(calendarPhoto)
            val swipe = object : HomePhotoSwipe(requireActivity()) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            photoAdapter.showNextMonthImage(viewHolder.adapterPosition)
                        }
                    }
                }
            }
            homeCalendarRecyclerView.adapter = photoAdapter
            val touchHelper = ItemTouchHelper(swipe)
            touchHelper.attachToRecyclerView(homeCalendarRecyclerView)
        }


    private fun showDiaryWithSwiper(diaries: ArrayList<Diary>) = with(binding) {
        diaryAdapter.updateDiaryList(diaries)
        filteredDiaries.addAll(diaries)

        homeSearchButton.setOnClickListener {
            viewModel.updateSearchViewVisibilityState()
        }

        homeSortButton.setOnClickListener {
            showSortMenu(diaries)
        }

        homeSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filteredDiaries.clear()
                    val searchText = newText.lowercase()
                    diaries.forEach {
                        if (it.diaryTitle.lowercase().contains(searchText)) {
                            filteredDiaries.add(it)
                            val filterAdapter = DiaryAdapter(filteredDiaries)
                            homeRecyclerView.adapter = filterAdapter
                        }
                    }
                }
                return true
            }
        })

        val swipe = object : HomeDiarySwipe(requireActivity()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when (direction) {
                    ItemTouchHelper.LEFT ->
                        diaryAdapter.deleteDiaryFromAdapter(viewHolder.adapterPosition) { noteId ->
                            viewModel.deleteNote(noteId)
                        }
                    ItemTouchHelper.RIGHT ->
                        diaryAdapter.deleteDiaryFromAdapter(viewHolder.adapterPosition) { noteId ->
                            viewModel.moveNoteToArchive(noteId)
                        }
                }
            }
        }
        homeRecyclerView.adapter = diaryAdapter
        val touchHelper = ItemTouchHelper(swipe)
        touchHelper.attachToRecyclerView(homeRecyclerView)
    }

    private fun showSortMenu(diaries: ArrayList<Diary>) {
        val popupMenu = PopupMenu(requireContext(), binding.homeSortButton)
        popupMenu.menuInflater.inflate(R.menu.menu_sort, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sort_old -> {
                    val sortedDiariesFromOldToNew = ArrayList(
                        diaries.sortedWith(compareBy<Diary> { it.diaryDateEn }
                            .thenBy { it.diaryTime })
                    )
                    showDiaryWithSwiper(sortedDiariesFromOldToNew)
                    true
                }
                R.id.sort_new -> {
                    val sortedDiariesFromNewToOld = ArrayList(
                        diaries.sortedWith(compareByDescending<Diary> { it.diaryDateEn }
                            .thenByDescending { it.diaryTime })
                    )
                    showDiaryWithSwiper(sortedDiariesFromNewToOld)
                    true
                }
                R.id.sort_max -> {
                    val sortedDiariesFromMaxToMin = ArrayList(
                        diaries.sortedByDescending {
                            it.diaryPhoto?.size
                        }
                    )
                    showDiaryWithSwiper(sortedDiariesFromMaxToMin)
                    true
                }
                R.id.sort_min -> {
                    val sortedDiariesFromMinToMax = ArrayList(
                        diaries.sortedBy {
                            it.diaryPhoto?.size
                        }
                    )
                    showDiaryWithSwiper(sortedDiariesFromMinToMax)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun closeSearchView() = with(binding) {
        homeSearchView.clearFocus()
        homeSearchView.isIconified = true
        inputMethodManager.hideSoftInputFromWindow(
            homeSearchView.windowToken, 0
        )
    }

    private fun navigateDiaryScreenToAddDiary() {
        viewModel.saveBooleanData(NOTE_DATA, true)
        val intentToDiaryActivity = Intent(requireActivity(), DiaryActivity::class.java)
        startActivity(intentToDiaryActivity)
    }

    private fun observeLiveData() = with(binding) {
        viewModel.homeToastMessage.observe(viewLifecycleOwner) { toastMessage ->
            toastMessage?.let {
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.homeSnackbarMessage.observe(viewLifecycleOwner) { swipeMessage ->
            swipeMessage?.let {
                Snackbar.make(root, getString(swipeMessage), Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.homeUserName.observe(viewLifecycleOwner) { username ->
            username?.let {
                homeNameText.visibility = View.VISIBLE
                homeWelcomeText.visibility = View.VISIBLE
                homeEmoji.visibility = View.VISIBLE
                homeNameText.text = username
            }
        }

        viewModel.homeCalendarView.observe(viewLifecycleOwner) { images ->
            images?.let {
                homeCalendarRecyclerView.visibility = View.VISIBLE
                showCalendarPhotoWithSwiper(images as ArrayList<Calendar>)
            }
        }

        viewModel.homeDateText.observe(viewLifecycleOwner) { date ->
            date?.let {
                homeCalendar.visibility = View.VISIBLE
                homeCalendar.text = date
            }
        }

        viewModel.homeDiaryView.observe(viewLifecycleOwner) { diaries ->
            diaries?.let {
                homeRecyclerView.visibility = View.VISIBLE
                val sortedDiariesFromNewToOld = ArrayList(
                    diaries.sortedWith(compareByDescending<Diary> { it.diaryDateEn }
                        .thenByDescending { it.diaryTime })
                )
                showDiaryWithSwiper(sortedDiariesFromNewToOld)
                homeSwipeRefresh.isRefreshing = false
                homeNoteErrorMessage.visibility = View.GONE
                homeNoteNullMessage.visibility = View.GONE
            }
        }

        viewModel.homeErrorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                if (it) {
                    homeNoteErrorMessage.visibility = View.VISIBLE
                    homeSwipeRefresh.isRefreshing = false
                    homeRecyclerView.visibility = View.GONE
                } else {
                    homeSwipeRefresh.isRefreshing = false
                    homeNoteErrorMessage.visibility = View.GONE
                }
            }
        }

        viewModel.homeNullMessage.observe(viewLifecycleOwner) { nullMessage ->
            nullMessage?.let {
                if (it) {
                    homeNoteNullMessage.visibility = View.VISIBLE
                    homeSwipeRefresh.isRefreshing = false
                    homeRecyclerView.visibility = View.GONE
                    homeNoteErrorMessage.visibility = View.GONE
                } else {
                    homeSwipeRefresh.isRefreshing = false
                    homeNoteNullMessage.visibility = View.GONE
                }
            }
        }

        viewModel.homeSearchView.observe(viewLifecycleOwner) { searchView ->
            searchView?.let {
                if (it) {
                    homeCalendar.visibility = View.GONE
                    homeSearchView.visibility = View.VISIBLE
                    homeCalendarRecyclerView.visibility = View.GONE
                    homeSearchButton.setImageResource(R.drawable.arrow_back)
                    homeSearchView.requestFocus()
                    inputMethodManager.showSoftInput(
                        homeSearchView, InputMethodManager.SHOW_IMPLICIT
                    )
                } else {
                    homeCalendar.visibility = View.VISIBLE
                    homeSearchView.visibility = View.GONE
                    homeCalendarRecyclerView.visibility = View.VISIBLE
                    homeSearchButton.setImageResource(R.drawable.base_search)
                    closeSearchView()
                }
            }
        }
    }
}