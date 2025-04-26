package com.dizifun

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import org.jsoup.Jsoup

class DiziFun : MainAPI() {
    override var name = "DiziFun"
    override var mainUrl = "https://dizifun2.com"
    override var lang = "tr"
    override val supportedTypes = setOf(TvType.TvSeries)

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val doc = app.get(url).document
        return doc.select("div.listing-content > article").mapNotNull {
            val title = it.selectFirst("h2.entry-title > a")?.text() ?: return@mapNotNull null
            val link = it.selectFirst("h2.entry-title > a")?.attr("href") ?: return@mapNotNull null
            val poster = it.selectFirst("img")?.attr("src")
            TvSeriesSearchResponse(
                title,
                link,
                this.name,
                TvType.TvSeries,
                poster,
                null,
                null
            )
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1.entry-title")?.text() ?: "Dizi"
        val poster = doc.selectFirst("img")?.attr("src")
        val episodes = doc.select("a.episode").map {
            val epName = it.text()
            val epLink = it.attr("href")
            Episode(epName, epLink)
        }.reversed()
        return TvSeriesLoadResponse(
            title,
            url,
            this.name,
            TvType.TvSeries,
            episodes,
            poster,
            plot = null
        )
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val doc = app.get(data).document
        val iframe = doc.selectFirst("iframe")?.attr("src") ?: return
        callback.invoke(
            ExtractorLink(
                name,
                name,
                iframe,
                referer = mainUrl,
                quality = Qualities.Unknown
            )
        )
    }
}
