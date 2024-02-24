package com.example.diarybook.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diarybook.R
import com.example.diarybook.adapter.CalendarAdapter
import com.example.diarybook.adapter.DiaryAdapter
import com.example.diarybook.databinding.FragmentHomeBinding
import com.example.diarybook.model.Calendar
import com.example.diarybook.model.Diary
import com.example.diarybook.swipe.HomePhotoSwipe
import com.example.diarybook.swipe.HomeDiarySwipe
import com.example.diarybook.util.Constant.NOTE_DATA
import com.example.diarybook.view.activity.DiaryActivity
import com.example.diarybook.viewmodel.HomeViewModel
import com.example.diarybook.viewmodel.UserViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.ads.MobileAds

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var userViewModel: UserViewModel

    private lateinit var homeFragmentBinding: FragmentHomeBinding
    private var isSearchViewVisible: Boolean = false
    private lateinit var filteredDiaries: ArrayList<Diary>
    private lateinit var homeAdView: AdView
    private lateinit var inputMethodManager: InputMethodManager

    private var diaryAdapter = DiaryAdapter(arrayListOf())
    private var photoAdapter = CalendarAdapter(arrayListOf())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        filteredDiaries = ArrayList<Diary>()

        MobileAds.initialize(requireContext()) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        homeFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return homeFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(homeFragmentBinding) {
            super.onViewCreated(view, savedInstanceState)

            this@HomeFragment.homeAdView = homeAdView
            val adRequest = AdRequest.Builder().build()
            this@HomeFragment.homeAdView.loadAd(adRequest)

            homeRecyclerView.layoutManager = LinearLayoutManager(context)
            homeCalendarRecyclerView.layoutManager = LinearLayoutManager(context)

            observeLiveData()

            homeCalendarRecyclerView.adapter = photoAdapter

            homeSwipeRefresh.setOnRefreshListener {

                homeSwipeRefresh.isRefreshing = true

                homeViewModel.refreshHomeFragment()
            }

            homeAddNoteButton.setOnClickListener {
                getDiaryActivityToAddDiary()
            }

            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(callback)

        }

    override fun onStop() {
        super.onStop()

        closeSearchView()
    }

    override fun onResume() {
        super.onResume()

        homeViewModel.refreshHomeFragment()
    }

    private fun showCalendarPhotoWithSwiper(calendarPhoto : ArrayList<Calendar>) = with(homeFragmentBinding){

        photoAdapter.updateCalendarList(calendarPhoto)

        val swipe = object : HomePhotoSwipe(requireActivity()) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {


                when (direction) {

                    ItemTouchHelper.LEFT ->{
                        photoAdapter.showNextMonthImage(viewHolder.adapterPosition)
                    }

                }

            }
        }

        homeCalendarRecyclerView.adapter = photoAdapter

        val touchHelper = ItemTouchHelper(swipe)
        touchHelper.attachToRecyclerView(homeCalendarRecyclerView)
    }


    private fun showNoteWithSwiper(diaries: ArrayList<Diary>) = with(homeFragmentBinding) {

        diaryAdapter.updateDiaryList(diaries)

        homeSearchButton.setOnClickListener {
            homeViewModel.homeSearchView.value = !isSearchViewVisible
            isSearchViewVisible = !isSearchViewVisible
        }

        filteredDiaries.addAll(diaries)

        homeSortButton.setOnClickListener {
            showSortMenu(diaries)
        }

        homeSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filteredDiaries.clear()
                    val searchText = newText.lowercase()
                    diaries.forEach {
                        if (it.diaryTitle!!.lowercase().contains(searchText)) {
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
                            homeViewModel.deleteNote(noteId)
                        }
                    ItemTouchHelper.RIGHT ->
                        diaryAdapter.deleteDiaryFromAdapter(viewHolder.adapterPosition) { noteId ->
                            homeViewModel.moveNoteToArchive(noteId)
                        }
                }

            }
        }
        homeRecyclerView.adapter = diaryAdapter


        val touchHelper = ItemTouchHelper(swipe)
        touchHelper.attachToRecyclerView(homeRecyclerView)
    }

    private fun showSortMenu(diaries: ArrayList<Diary>) {

        val popupMenu = PopupMenu(requireContext(), homeFragmentBinding.homeSortButton)
        popupMenu.menuInflater.inflate(R.menu.menu_sort, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.sort_old -> {
                    val sortedDiariesFromOldToNew = ArrayList(
                        diaries.sortedWith(compareBy<Diary> { it.diaryDateEn }
                            .thenBy { it.diaryTime })
                    )
                    showNoteWithSwiper(sortedDiariesFromOldToNew)
                    true
                }
                R.id.sort_new -> {
                    val sortedDiariesFromNewToOld = ArrayList(
                        diaries.sortedWith(compareByDescending<Diary> { it.diaryDateEn }
                            .thenByDescending { it.diaryTime })
                    )
                    showNoteWithSwiper(sortedDiariesFromNewToOld)
                    true
                }
                R.id.sort_max -> {
                    val sortedDiariesFromMaxToMin = ArrayList(
                        diaries.sortedByDescending {
                            it.diaryPhoto!!.size
                        }
                    )
                    showNoteWithSwiper(sortedDiariesFromMaxToMin)
                    true
                }
                R.id.sort_min -> {
                    val sortedDiariesFromMinToMax = ArrayList(
                        diaries.sortedBy {
                            it.diaryPhoto!!.size
                        }
                    )
                    showNoteWithSwiper(sortedDiariesFromMinToMax)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun closeSearchView() = with(homeFragmentBinding){
        homeSearchView.clearFocus()
        homeSearchView.isIconified = true
        inputMethodManager.hideSoftInputFromWindow(
            homeSearchView.windowToken, 0
        )
    }

    private fun getDiaryActivityToAddDiary() {


        homeViewModel.saveBooleanData(NOTE_DATA, true)


        val intentToDiaryActivity = Intent(requireActivity(), DiaryActivity::class.java)
        startActivity(intentToDiaryActivity)

    }

    private fun observeLiveData() = with(homeFragmentBinding) {


        homeViewModel.homeToastMessage.observe(viewLifecycleOwner, Observer { toastMessage ->
            toastMessage?.let {
                Toast.makeText(requireContext(),toastMessage,Toast.LENGTH_SHORT).show()
            }
        })

        homeViewModel.homeSnackbarMessage.observe(viewLifecycleOwner, Observer { swipeMessage ->
            swipeMessage?.let {
                Snackbar.make(root, getString(swipeMessage), Snackbar.LENGTH_SHORT).show()
            }
        })

        homeViewModel.homeUserName.observe(viewLifecycleOwner, Observer { username ->

            username?.let {
                homeNameText.visibility = View.VISIBLE
                homeWelcomeText.visibility = View.VISIBLE
                homeEmoji.visibility = View.VISIBLE
                homeNameText.text = username
            }

        })


        homeViewModel.homeCalendarView.observe(viewLifecycleOwner, Observer { images ->
            images?.let {
                homeCalendarRecyclerView.visibility = View.VISIBLE
                showCalendarPhotoWithSwiper(images as ArrayList<Calendar>)
            }
        })

        homeViewModel.homeDateText.observe(viewLifecycleOwner, Observer { date ->
            date?.let {
                homeCalender.visibility = View.VISIBLE
                homeCalender.text = date
            }
        })

        homeViewModel.homeDiaryView.observe(viewLifecycleOwner, Observer { diaries ->
            diaries?.let {

                homeRecyclerView.visibility = View.VISIBLE
                val sortedDiariesFromNewToOld = ArrayList(
                    diaries.sortedWith(compareByDescending<Diary> { it.diaryDateEn }
                        .thenByDescending { it.diaryTime })
                )
                showNoteWithSwiper(sortedDiariesFromNewToOld)

                homeSwipeRefresh.isRefreshing = false
                homeNoteErrorMessage.visibility = View.GONE
                homeNoteNullMessage.visibility = View.GONE

            }
        })

        homeViewModel.homeErrorMessage.observe(viewLifecycleOwner, Observer { error ->
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
        })

        homeViewModel.homeNullMessage.observe(viewLifecycleOwner, Observer { nullMessage ->
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
        })

        homeViewModel.homeSearchView.observe(viewLifecycleOwner, Observer { searchView ->
            searchView?.let {
                if (it) {
                    homeSearchView.visibility = View.VISIBLE
                    homeCalendarRecyclerView.visibility = View.GONE
                    homeSearchButton.setImageResource(R.drawable.arrow_back)
                    homeSearchView.requestFocus()
                    inputMethodManager.showSoftInput(
                        homeSearchView, InputMethodManager.SHOW_IMPLICIT
                    )
                } else {
                    homeSearchView.visibility = View.GONE
                    homeCalendarRecyclerView.visibility = View.VISIBLE
                    homeSearchButton.setImageResource(R.drawable.base_search)
                    closeSearchView()
                }
            }
        })

    }

}