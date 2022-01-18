/*
 * Copyright (c) 2021  Eric A. Snell
 *
 * This file is part of eAlvaMusicInfo
 *
 * eAlvaMusicInfo is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version
 * 3 of  the License, or (at your option) any later version.
 *
 * eAlvaMusicInfo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * eAlvaMusicInfo. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ealva.musicinfo.service.wiki

import com.ealva.ealvalog.e
import com.ealva.ealvalog.invoke
import com.ealva.musicinfo.BuildConfig
import com.ealva.musicinfo.common.NotWikipediaUrlException
import com.ealva.musicinfo.log.libLogger
import com.ealva.musicinfo.service.common.AppName
import com.ealva.musicinfo.service.common.AppVersion
import com.ealva.musicinfo.service.common.ContactEmail
import com.ealva.musicinfo.service.common.MusicInfoMessage
import com.ealva.musicinfo.service.common.MusicInfoMessage.MusicInfoExceptionMessage
import com.ealva.musicinfo.service.common.MusicInfoMessage.MusicInfoStatusMessage.MusicInfoErrorCodeMessage
import com.ealva.musicinfo.service.common.MusicInfoMessage.MusicInfoStatusMessage.MusicInfoNullReturn
import com.ealva.musicinfo.service.init.EalvaMusicInfo
import com.ealva.musicinfo.service.net.CacheControlInterceptor
import com.ealva.musicinfo.service.net.ThrottlingInterceptor
import com.ealva.musicinfo.service.wiki.WikipediaService.WikibaseItemId
import com.ealva.musicinfo.wiki.Wikipedia
import com.ealva.musicinfo.wiki.data.WikiDataEntities
import com.ealva.musicinfo.wiki.data.WikiSummary
import com.ealva.musicinfo.wiki.data.theWikiMoshi
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.coroutines.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.ealva.ealvabrainz.brainz.data.Url as BrainzUrl

private val LOG by libLogger(WikipediaService::class)

/**
 * A WikipediaCall is a suspending function which has a [Wikipedia] receiver and returns a Retrofit
 * Response with a type parameter of the returned Wikipedia entity.
 */
public typealias WikipediaCall<T> = suspend Wikipedia.() -> Response<T>

/**
 * WikipediaResult<T> is a Result<T, MusicInfoMessage>, T being the Ok return type and a
 * specialization of MusicInfoMessage being the Err return type.
 */
public typealias WikipediaResult<T> = Result<T, MusicInfoMessage>

/**
 * WikipediaService is a wrapper around a Retrofit Wikipedia instance that provides higher level
 * functionality such as rate limiting, exception handling, etc.
 *
 * Functions return a [WikipediaResult] which is a Result<T, MusicInfoMessage>. If the result is
 * [Ok] it will contain an instance of T. If an error occurs an [Err] is returned which contains a
 * MusicInfoMessage.
 *
 * An [Err] will be a [MusicInfoMessage] of type:
 * * [MusicInfoExceptionMessage] if an underlying exception is thrown
 * * [MusicInfoNullReturn] if the response is OK but null
 * * [MusicInfoErrorCodeMessage] if the response is not successful - contains the response
 * status code.
 *
 * All suspend functions are main safe, in that they are dispatched on a contained
 * Coroutine dispatcher, typically Dispatchers.IO. Exceptions are not thrown across this boundary
 * and instead a Result monad, indicating success (Ok) or failure (Err), is returned. Look at the
 * implementation of the wiki() method to see where everything comes together.
 *
 * [Result monad](https://github.com/michaelbull/kotlin-result)
 */
public interface WikipediaService {
  /**
   * Given a [wikiUrl], MusicBrainz [BrainzUrl], which is either of type "wikipedia" or "wikidata",
   * get the WikiSummary for the article referenced by [wikiUrl]. If [wikiUrl] is neither
   * wikipedia.org or wikidata.org, a [MusicInfoExceptionMessage] will be returned which contains a
   * [NotWikipediaUrlException] exception
   */
  public suspend fun getArticleSummary(wikiUrl: BrainzUrl): WikipediaResult<WikiSummary>

  /**
   * An ArticleTitle is the Wikipedia article title - the last path segment of the URL
   */
  @JvmInline
  public value class ArticleTitle(public val value: String)

  /**
   *
   *
   * [Wikibase Item](https://www.mediawiki.org/wiki/Wikibase/DataModel#Items)
   */
  @JvmInline
  public value class WikibaseItemId(public val value: String)

  /**
   * Get the WikiSummary of the article entitled [articleTitle], which is the last path segment
   * of the Wikipedia URL. For the rock band Nirvana the URL is
   * https://en.wikipedia.org/wiki/Nirvana_(band), and the title is "Nirvana_(band)"
   */
  public suspend fun getArticleSummary(articleTitle: ArticleTitle): WikipediaResult<WikiSummary>

  public suspend fun getSiteLinks(itemId: WikibaseItemId): WikipediaResult<WikiDataEntities>

  public companion object {
    public operator fun invoke(
      appName: AppName,
      appVersion: AppVersion,
      contactEmail: ContactEmail,
      okHttpClient: OkHttpClient = EalvaMusicInfo.okHttpClient,
      coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): WikipediaService = WikipediaServiceImpl(
      Retrofit.Builder()
        .client(
          makeOkHttpClient(
            "WikipediaService",
            appName,
            appVersion,
            contactEmail,
            addLoggingInterceptor = BuildConfig.DEBUG,
            okHttpClient
          )
        )
        .baseUrl("https://en.wikipedia.org/api/rest_v1/")
        .addConverterFactory(MoshiConverterFactory.create(theWikiMoshi))
        .build()
        .create(Wikipedia::class.java),
      coroutineDispatcher
    )
  }
}

private class WikipediaServiceImpl(
  private val wikipedia: Wikipedia,
  private val dispatcher: CoroutineDispatcher
) : WikipediaService {
  override suspend fun getArticleSummary(
    wikiUrl: BrainzUrl
  ): WikipediaResult<WikiSummary> = withContext(dispatcher) {
    when {
      wikiUrl.isWikipedia -> getArticleSummary(wikiUrl.articleTitle)
      wikiUrl.isWikidata -> getSiteLinks(WikibaseItemId(wikiUrl.lastSegment))
        .andThen { wikiDataEntities ->
          wikiDataEntities
            .entities
            .asSequence()
            .map { (_, dataEntity) -> dataEntity }
            .map { entity -> entity.sitelinks["enwiki"] }
            .firstOrNull()
            ?.let { siteLink -> getArticleSummary(WikipediaService.ArticleTitle(siteLink.title)) }
            ?: Err(MusicInfoMessage.MusicInfoErrorMessage("No (English) article found: $wikiUrl"))
        }
      else -> Err(MusicInfoExceptionMessage(NotWikipediaUrlException()))
    }
  }

  override suspend fun getArticleSummary(
    articleTitle: WikipediaService.ArticleTitle
  ): WikipediaResult<WikiSummary> = wiki {
    getWikipediaSummary(articleTitle.value)
  }

  override suspend fun getSiteLinks(
    itemId: WikibaseItemId
  ): WikipediaResult<WikiDataEntities> = wiki {
    getSiteLinks(itemId.value)
  }

  suspend fun <T : Any> wiki(
    block: WikipediaCall<T>
  ): Result<T, MusicInfoMessage> = withContext(dispatcher) {
    runSuspendCatching { wikipedia.block() }
      .mapError { ex -> MusicInfoExceptionMessage(ex) }
      .mapResponse()
  }
}

public fun <U, V : Response<U>> Result<V, MusicInfoMessage>.mapResponse(): WikipediaResult<U> =
  when (this) {
    is Ok -> handleResponse()
    is Err -> this
  }

private fun <U, V : Response<U>> Ok<V>.handleResponse(): Result<U, MusicInfoMessage> = try {
  when {
    value.isSuccessful -> value.body()?.let { Ok(it) } ?: Err(MusicInfoNullReturn(value.code()))
    else -> value.toErrResult()
  }
} catch (e: Exception) {
  Err(MusicInfoExceptionMessage(e))
}

private fun <T, U> Response<T>.toErrResult(): Result<U, MusicInfoMessage> =
  Err(MusicInfoErrorCodeMessage(code(), this))

private const val DAYS_MAX_AGE = 14
private const val DAYS_MIN_FRESH = 14
private const val DAYS_MAX_STALE = 365
private const val WIKIPEDIA_MAX_CALLS_PER_SECOND = 5.0

internal fun makeOkHttpClient(
  serviceName: String,
  appName: AppName,
  appVersion: AppVersion,
  contactEmail: ContactEmail,
  addLoggingInterceptor: Boolean = true,
  okHttpClient: OkHttpClient
): OkHttpClient = okHttpClient.newBuilder().apply {
  addInterceptor(CacheControlInterceptor(DAYS_MAX_AGE, DAYS_MIN_FRESH, DAYS_MAX_STALE))
  addInterceptor(ThrottlingInterceptor(WIKIPEDIA_MAX_CALLS_PER_SECOND, serviceName))
  addInterceptor(WikipediaApiInterceptor(appName, appVersion, contactEmail))
  if (addLoggingInterceptor) {
    addInterceptor(
      HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    )
  }
}.build()


private val BrainzUrl.lastSegment: String
  get() = try {
    resource.substring(resource.lastIndexOf("/") + 1)
  } catch (e: Exception) {
    LOG.e(e) { it("Error parsing URL $resource") }
    throw NotWikipediaUrlException()
  }

private val BrainzUrl.articleTitle: WikipediaService.ArticleTitle
  get() = WikipediaService.ArticleTitle(lastSegment)

private val BrainzUrl.isWikipedia: Boolean
  get() = resource.contains("wikipedia.org", ignoreCase = true)

private val BrainzUrl.isWikidata: Boolean
  get() = resource.contains("wikidata.org", ignoreCase = true)
