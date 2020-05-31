package me.anon.grow3.util.states

sealed class UiState<D, E>
{
	class Complete<D>(val state: D) : UiState<D, Nothing>()
	class Error<E>(val error: E) : UiState<Nothing, E>()
	object Loading : UiState<Nothing, Nothing>()
	object Empty : UiState<Nothing, Nothing>()
	object Initial : UiState<Nothing, Nothing>()
}
