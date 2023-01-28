package com.phinion.pokelist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.toLowerCase
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.phinion.pokelist.pokemondetail.PokemonDetailScreen
import com.phinion.pokelist.pokemonlist.PokemonListScreen
import com.phinion.pokelist.ui.theme.PokelistTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokelistTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "pokemon_list_screen") {

                    composable("pokemon_list_screen") {

                        PokemonListScreen(navController = navController)

                    }
                    composable("pokemon_detail_screen/{dominantColor}/{pokemonName}/{pokemonId}",
                        arguments = listOf(
                            navArgument("dominantColor") {
                                type = NavType.IntType
                            },
                            navArgument("pokemonName") {
                                type = NavType.StringType
                            },
                            navArgument("pokemonId") {
                                type = NavType.IntType
                            }
                        )
                    ) {
                        val dominantColor = remember {
                            val color = it.arguments?.getInt("dominantColor")
                            if (color != null) {
                                Color(color)
                            } else {
                                Color.White
                            }
                        }

                        val pokemonName = remember {
                            it.arguments?.getString("pokemonName")
                        }
                        val pokemonId = remember {
                            it.arguments?.getInt("pokemonId")
                        }
                        PokemonDetailScreen(
                            dominantColor = dominantColor, pokemonName = pokemonName?.toLowerCase(
                                Locale.ROOT
                            ) ?: "", navController = navController, pokemonId = pokemonId ?: -1
                        )

                    }
                }

            }
        }
    }
}

