package com.nunes.eduardo.videoapp.model

import com.nunes.eduardo.videoapp.Movie

data class VideosItem(
	val sources: List<String?>? = null,
	val thumb: String? = null,
	val banner: String? = null,
	val subtitle: String? = null,
	val description: String? = null,
	val title: String? = null
){
	fun convertToMovie(id: Long): Movie{
		return Movie(
				id,
				title,
				description,
				banner,
				thumb,
				sources?.first(),
				subtitle
		)
	}
}
