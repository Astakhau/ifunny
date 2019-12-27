package me.ruslanys.ifunny.crawler.source

import java.time.LocalDateTime

data class MemeInfo(
        val pageUrl: String? = null,
        val resourceUrl: String? = null,
        val title: String? = null,
        val publishDateTime: LocalDateTime? = null,
        val likes: Int? = null,
        val comments: Int? = null,
        val author: String? = null
)

