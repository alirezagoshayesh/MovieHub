package com.test.moviehub.component.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.test.moviehub.R
import com.test.moviehub.component.adapters.SearchResultsAdapter
import com.test.moviehub.component.dialogs.LoadingDialog
import com.test.moviehub.component.viewModels.SearchFragmentVM
import com.test.moviehub.databinding.FragmentSearchBinding
import com.test.moviehub.component.viewModels.GetDetailsVM
import com.test.moviehub.domain.base.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search), SearchView.OnQueryTextListener,
        (CombinedLoadStates) -> Unit, SearchResultsAdapter.ItemListOnClickListener {

    lateinit var binding: FragmentSearchBinding

    @Inject
    lateinit var loadingDialog: LoadingDialog

    @Inject
    lateinit var searchResultsAdapter: SearchResultsAdapter
    private val searchFragmentVM: SearchFragmentVM by viewModels()
    private val getDetailsVM: GetDetailsVM by viewModels()

    private var mJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_search, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseView()
        registerObserver()
    }

    /**
     * function to register observers for the viewModel liveData.
     */
    private fun registerObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            getDetailsVM.movie.collect {
                when (it) {
                    is Resource.Error -> {
                        loadingDialog.hide()
                        Toast.makeText(
                            requireContext(),
                            "${it.errorModel.getErrorMessage()} with code ${it.errorModel.code}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    Resource.Loading -> loadingDialog.show()
                    is Resource.Success -> {
                        loadingDialog.hide()
                        findNavController().navigate(
                            SearchFragmentDirections.actionFragmentMainToFragmentDetail(
                                it.data, it.data.title
                            )
                        )
                    }
                }
            }
        }
    }

    private fun parseView() {
        binding.rv.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
            adapter = searchResultsAdapter
        }
        searchResultsAdapter.addLoadStateListener(this)
        searchResultsAdapter.setOnClickListener(this)
        search()
        binding.sv.setOnQueryTextListener(this)
    }

    private fun search(query: String? = null) {
        mJob?.cancel()
        mJob = lifecycleScope.launch {
            if (query == null)
                searchFragmentVM.getPopularMovies().collectLatest {
                    binding.textTop = getString(R.string.popular)
                    searchResultsAdapter.submitData(it)
                }
            else
                searchFragmentVM.searchMovies(query).collectLatest {
                    binding.textTop = "Movies including $query"
                    searchResultsAdapter.submitData(it)
                }
        }
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        if (!p0.isNullOrEmpty())
            search(p0)
        binding.sv.setQuery("", false)
        binding.root.requestFocus()
        return false
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        return false
    }

    override fun invoke(loadState: CombinedLoadStates) {
        if (loadState.refresh is LoadState.Loading ||
            loadState.append is LoadState.Loading
        )
            loadingDialog.show()
        else {
            loadingDialog.hide()

            // If we have an error, show a toast
            val errorState = when {
                loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                else -> null
            }
            if (loadState.refresh is LoadState.Error || (loadState.append.endOfPaginationReached &&
                        searchResultsAdapter.itemCount == 0)
            )
                binding.txtNoData.visibility = View.VISIBLE
            else
                binding.txtNoData.visibility = View.GONE
            errorState?.let {
                Toast.makeText(requireContext(), it.error.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onClick(id: Int) {
        getDetailsVM.getDetailsOfMovie(id)
        loadingDialog.show()
    }
}