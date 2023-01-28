package com.phinion.pokelist.pokemondetail

import androidx.lifecycle.ViewModel
import com.phinion.pokelist.data.remote.responses.Pokemon
import com.phinion.pokelist.repository.PokemonRepository
import com.phinion.pokelist.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    suspend fun getPokemonInfo(pokemonName: String): Resource<Pokemon>{
        return repository.getPokemonInfo(pokemonName)
    }

    suspend fun getPokemonInformation(pokemonName: String): Pokemon{
        return repository.getPokemonInformation(pokemonName)
    }

}