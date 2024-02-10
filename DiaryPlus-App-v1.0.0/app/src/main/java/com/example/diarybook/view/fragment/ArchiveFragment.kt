package com.example.diarybook.view.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diarybook.R
import com.example.diarybook.adapter.DiaryAdapter
import com.example.diarybook.databinding.FragmentArchiveBinding
import com.example.diarybook.model.Diary
import com.example.diarybook.swipe.ArchiveDiarySwipe
import com.example.diarybook.util.Constant.ARCHIVE_KEY
import com.example.diarybook.viewmodel.ArchiveViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_base.*

class ArchiveFragment : Fragment() {

    private lateinit var archiveFragmentBinding: FragmentArchiveBinding

    private lateinit var archiveViewModel: ArchiveViewModel

    private var isSearchViewVisible: Boolean = false

    private lateinit var filteredArchive: ArrayList<Diary>
    private lateinit var archiveAdView: AdView


    private var diaryAdapter = DiaryAdapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        archiveViewModel = ViewModelProvider(this)[ArchiveViewModel::class.java]

        filteredArchive = ArrayList<Diary>()

        MobileAds.initialize(requireContext()) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        archiveFragmentBinding = FragmentArchiveBinding.inflate(layoutInflater)
        return archiveFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(archiveFragmentBinding) {
        super.onViewCreated(view, savedInstanceState)

        this@ArchiveFragment.archiveAdView = archiveAdView
        val adRequest = AdRequest.Builder().build()
        this@ArchiveFragment.archiveAdView.loadAd(adRequest)

        archiveRecyclerView.layoutManager = LinearLayoutManager(context)

        archiveText.visibility = View.GONE

        observeLiveData()

        archiveSwipeRefresh.setOnRefreshListener {

            archiveText.visibility = View.GONE
            archiveSwipeRefresh.isRefreshing = true

            archiveViewModel.refreshArchive()
        }

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                backToHomeFragment()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)

    }

    override fun onPause() {
        super.onPause()
        archiveViewModel.saveBooleanData(ARCHIVE_KEY,false)
    }


    override fun onResume() {
        super.onResume()
        archiveViewModel.saveBooleanData(ARCHIVE_KEY,true)
        archiveViewModel.refreshArchive()
    }


    private fun showArchiveWithSwiper(archive: ArrayList<Diary>) = with(archiveFragmentBinding) {

        diaryAdapter.updateDiaryList(archive)

        archiveSearchButton.setOnClickListener {
            archiveViewModel.archiveSearchView.value = !isSearchViewVisible
            isSearchViewVisible = !isSearchViewVisible
        }

        filteredArchive.addAll(archive)

        archiveSortButton.setOnClickListener {
            showSortMenu(archive)
        }

        archiveSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?)
                    : Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filteredArchive.clear()
                    val searchText = newText.lowercase()
                    archive.forEach {
                        if (it.diaryTitle!!.lowercase().contains(searchText)){
                            filteredArchive.add(it)
                            val filterAdapter = DiaryAdapter(filteredArchive)
                            archiveRecyclerView.adapter = filterAdapter
                        }
                    }
                }
                return true
            }
        })

        val swipe = object : ArchiveDiarySwipe(requireActivity()) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when (direction) {
                    ItemTouchHelper.LEFT ->
                        diaryAdapter.deleteDiaryFromAdapter(viewHolder.adapterPosition) { noteId ->
                            archiveViewModel.deleteArchive(noteId)
                        }
                    ItemTouchHelper.RIGHT ->
                        diaryAdapter.deleteDiaryFromAdapter(viewHolder.adapterPosition) { noteId ->
                            archiveViewModel.moveArchiveToNote(noteId)
                        }
                }

            }
        }

        archiveRecyclerView.adapter = diaryAdapter

        val touchHelper = ItemTouchHelper(swipe)
        touchHelper.attachToRecyclerView(archiveRecyclerView)
    }

    private fun backToHomeFragment() {

        requireActivity().bottomMenuView.selectedItemId = R.id.homeButton

        val fragment = HomeFragment()
        val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager
        fragmentManager.beginTransaction().apply {
            replace(R.id.baseFragment, fragment).commit()
        }

    }

    private fun showSortMenu(archives: ArrayList<Diary>) {

        val popupMenu = PopupMenu(requireContext(), archiveFragmentBinding.archiveSortButton)
        popupMenu.menuInflater.inflate(R.menu.menu_sort, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sort_old -> {
                    val sortedArchivesFromOldToNew = ArrayList(
                        archives
                            .sortedWith(compareBy<Diary>
                            { it.diaryDateEn }
                                .thenBy { it.diaryTime })
                    )
                    showArchiveWithSwiper(sortedArchivesFromOldToNew)
                    true
                }
                R.id.sort_new -> {
                    val sortedArchivesFromNewToOld = ArrayList(
                        archives.sortedWith(
                            compareByDescending<Diary>
                            { it.diaryDateEn }
                                .thenByDescending { it.diaryTime })
                    )
                    showArchiveWithSwiper(sortedArchivesFromNewToOld)
                    true
                }
                R.id.sort_max -> {
                    val sortedArchivesFromMaxToMin = ArrayList(
                        archives.sortedByDescending {
                            it.diaryPhoto!!.size
                        }
                    )
                    showArchiveWithSwiper(sortedArchivesFromMaxToMin)
                    true
                }
                R.id.sort_min -> {
                    val sortedArchivesFromMinToMax = ArrayList(
                        archives.sortedBy {
                            it.diaryPhoto!!.size
                        }
                    )
                    showArchiveWithSwiper(sortedArchivesFromMinToMax)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun observeLiveData() = with(archiveFragmentBinding) {

        archiveViewModel.archiveToastMessage.observe(viewLifecycleOwner, Observer { toastMessage ->
            toastMessage?.let {
                Toast.makeText(requireContext(),toastMessage, Toast.LENGTH_SHORT).show()
            }
        })

        archiveViewModel.archiveSnackbarMessage.observe(viewLifecycleOwner, Observer { swipeMessage ->
            swipeMessage?.let {
                Snackbar.make(root,getString(swipeMessage),Snackbar.LENGTH_SHORT).show()
            }
        })

        archiveViewModel.archiveDiaryView.observe(viewLifecycleOwner, Observer { archives ->
            archives?.let {
                archiveRecyclerView.visibility = View.VISIBLE
                val sortedArchiveFromNewToOld = ArrayList(
                    archives.sortedWith(
                        compareByDescending<Diary>
                        { it.diaryDateEn }
                            .thenByDescending { it.diaryTime }
                    )
                )
                showArchiveWithSwiper(sortedArchiveFromNewToOld)
                archiveSwipeRefresh.isRefreshing = false
                archiveNoteNullMessage.visibility = View.GONE
                archiveNoteErrorMessage.visibility = View.GONE
                archiveText.visibility = View.VISIBLE
            }
        })

        archiveViewModel.archiveNotesErrorMessage.observe(viewLifecycleOwner,
            Observer { error ->
                error?.let {

                    if (it) {
                        archiveSwipeRefresh.isRefreshing = false
                        archiveRecyclerView.visibility = View.GONE
                        archiveText.visibility = View.GONE
                        archiveNoteErrorMessage.visibility = View.VISIBLE
                    } else {
                        archiveSwipeRefresh.isRefreshing = false
                        archiveNoteErrorMessage.visibility = View.GONE
                    }

                }
            })

        archiveViewModel.archiveNoteNullMessage.observe(viewLifecycleOwner,
            Observer { nullMessage ->
                nullMessage?.let {

                    if (it) {
                        archiveSwipeRefresh.isRefreshing = false
                        archiveRecyclerView.visibility = View.GONE
                        archiveNoteErrorMessage.visibility = View.GONE
                        archiveText.visibility = View.GONE
                        archiveNoteNullMessage.visibility = View.VISIBLE
                    } else {
                        archiveSwipeRefresh.isRefreshing = false
                        archiveNoteNullMessage.visibility = View.GONE
                    }

                }
            })

        archiveViewModel.archiveSearchView.observe(viewLifecycleOwner,
            Observer { searchView ->
                searchView?.let {

                    val inputMethodManager =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                    if (it) {
                        archiveSearchView.visibility = View.VISIBLE
                        archiveText.visibility = View.GONE
                        archiveSearchButton.setImageResource(R.drawable.arrow_back)
                        archiveSearchView.requestFocus()
                        inputMethodManager.showSoftInput(
                            archiveSearchView,
                            InputMethodManager.SHOW_IMPLICIT
                        )
                    } else {
                        archiveSearchView.visibility = View.GONE
                        archiveText.visibility = View.VISIBLE
                        archiveSearchView.clearFocus()
                        archiveSearchButton.setImageResource(R.drawable.base_search)
                        archiveSearchView.isIconified = true
                        inputMethodManager.hideSoftInputFromWindow(
                            archiveSearchView.windowToken,
                            0
                        )
                    }

                }
            })
    }
}