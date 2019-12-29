package me.ruslanys.ifunny.crawler.source

import me.ruslanys.ifunny.crawler.domain.Language
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class FunpotSource : Source(Language.GERMAN, "https://funpot.net") {

    override fun pagePath(pageNumber: Int): String = "$baseUrl/entdecken/lustiges/$pageNumber/"

    override fun parsePageList(body: String): List<MemeInfo> {
        val document = Jsoup.parse(body)
        val boxes = document.getElementsByClass("contentline")

        val list = arrayListOf<MemeInfo>()

        for (box in boxes) {
            // Type
            val type = parseType(box)
            if (type == null || !type.startsWith("Bild") && !type.startsWith("Online-Video")) {
                continue // skip non-image and non-video content
            }

            // Header
            val header = parseHeader(box)
            val url = header.first
            val title = header.second

            // Likes
            val likes = parseLikes(box)

            // Author
            val author = parseAuthor(box)

            // Publish Date
            val publishDateTime = parsePublishDateTime(box)

            // --
            val info = MemeInfo(
                    pageUrl = url,
                    title = title,
                    likes = likes,
                    author = author,
                    publishDateTime = publishDateTime
            )
            list.add(info)
        }

        return list
    }

    private fun parseType(box: Element): String? {
        return box.select(".look_datei > .kleine_schrift").firstOrNull()?.text()
    }

    private fun parseHeader(box: Element): Pair<String, String> {
        val link = box.select(".look_datei > a").first()

        val memeUrl = link.attr("href")
        val title = link.text()

        return memeUrl to title
    }

    private fun parseLikes(box: Element): Int {
        return box.select("td.look_bewertung .kleine_schrift").firstOrNull()?.text()?.toInt() ?: 0
    }

    private fun parseAuthor(box: Element): String? {
        return box.select("td.look_nickname a").firstOrNull()?.text()
    }

    private fun parsePublishDateTime(box: Element): LocalDateTime {
        val dateText = box.select("td.look_freigabedatum").text()

        // Date unification
        val now = LocalDateTime.now()
        val yearString = now.year.toString().substring(2)

        val unifiedDateText = dateText
                .replace("heute", "${now.dayOfMonth}.${now.monthValue}.")
                .replace(".-", ".$yearString-")

        // --
        return LocalDateTime.parse(unifiedDateText, DATE_EXTRACTOR)
    }

    override fun parsePageMeme(info: MemeInfo, body: String): MemeInfo {
        val document = Jsoup.parse(body).also { it.setBaseUri(baseUrl) }
        val container = document.getElementById("content")

        // --
        val resourceUrl = parseResourceUrl(container)

        // --
        return MemeInfo(
                pageUrl = info.pageUrl,
                resourceUrl = resourceUrl,
                title = info.title,
                likes = info.likes,
                author = info.author,
                publishDateTime = info.publishDateTime
        )
    }

    private fun parseResourceUrl(container: Element): String {
        return container.getElementById("Direktdownload")?.absUrl("href")
                ?: container.select("video > source").first().absUrl("src")
    }

    companion object {
        private val DATE_EXTRACTOR = DateTimeFormatter.ofPattern("dd.MM.yy'-'HH:mm")
    }

}
