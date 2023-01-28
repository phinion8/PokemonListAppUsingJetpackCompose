package com.phinion.pokelist.pokemonlist

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.phinion.pokelist.data.models.PokeListEntry
import com.phinion.pokelist.data.remote.responses.Pokemon
import com.phinion.pokelist.repository.PokemonRepository
import com.phinion.pokelist.util.Constants.PAGE_SIZE
import com.phinion.pokelist.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private var curPage = 0

    var pokemonList = mutableStateOf<List<PokeListEntry>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    //endReached is something when we at the end of the list and we want to stop the pagination
    var endReached = mutableStateOf(false)

    private var cachedPokemonList = listOf<PokeListEntry>()

    private var isSearchStarting = true

    var isSearching = mutableStateOf(false)

    suspend fun getPokemonInfo(pokemonName: String): Resource<Pokemon>{
        return repository.getPokemonInfo(pokemonName)
    }


    init {
        loadPokemonPaginated()
    }

    fun searchPokemonList(query: String){
        val listToSearch = if (isSearchStarting){
            pokemonList.value
        }else{
            cachedPokemonList
        }
        viewModelScope.launch(Dispatchers.Default) {
            if (query.isEmpty()){
                pokemonList.value = cachedPokemonList
                isSearching.value = false
                isSearchStarting = true
                return@launch
            }
            val results = listToSearch.filter {
                it.pokemonName.contains(query.trim(), ignoreCase = true) ||
                        it.number.toString() == query.trim()
            }
            if (isSearchStarting){
                cachedPokemonList = pokemonList.value
                isSearchStarting = false
            }
            pokemonList.value = results
            isSearching.value = true
        }
    }

    fun loadPokemonPaginated(){
        viewModelScope.launch {
            isLoading.value = true
            //here offset is like when we are at the 0th position then we want to load the first 20 pokemon and then
            //when we loaded the first 20 then curPage will will be one then we multiply it with page size which will
            //give another 20
            val result = repository.getPokemonList(PAGE_SIZE, curPage * PAGE_SIZE)
            when(result){
                is Resource.Success -> {

                    Log.d("DBPS", "success")
                    //basically here is when we at the end of the list then at some time the value of curPage * page size
                    // will be greater than the result count so at that moment we want to stop the pagination
                    endReached.value = curPage * PAGE_SIZE >= result.data!!.count

                    val pokeListEntries = result.data.results.mapIndexed{index, entry ->
                        val number = if (entry.url.endsWith("/")){
                            entry.url.dropLast(1).takeLastWhile { it.isDigit() }
                        }else{
                            entry.url.takeLastWhile { it.isDigit() }
                        }
                        val url = "https://unpkg.com/pokeapi-sprites@2.0.2/sprites/pokemon/other/dream-world/$number.svg"
//                        val url = "https://www.pngplay.com/wp-content/uploads/2/Pokeball-PNG-Pic-Background.png"
                        PokeListEntry(entry.name.capitalize(Locale.ROOT), url, number.toInt())
                    }
                    curPage++
                    loadError.value = ""
                    isLoading.value = false
                    pokemonList.value += pokeListEntries
                }
                is Resource.Error -> {

                    loadError.value = result.message!!
                    isLoading.value = false
                    Log.d("DBPS", "error")

                }
                else -> {
                    Log.d("DBPS", "error")
                }
            }
        }
    }

    fun calcDominantColor(drawable: Drawable, onFinish: (Color) -> Unit){
        val bitmap = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

        Palette.from(bitmap).generate{
            palette->
            palette?.dominantSwatch?.rgb?.let {
                colorValue ->
                onFinish(Color(colorValue))
            }
        }


    }

}