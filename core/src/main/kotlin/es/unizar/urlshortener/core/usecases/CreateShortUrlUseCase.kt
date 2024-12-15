@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*
import org.springframework.lang.NonNull

/**
 * Given an url returns the key that is used to create a short URL.
 * When the url is created optional data may be added.
 *
 * **Note**: This is an example of functionality.
 */
interface CreateShortUrlUseCase {
    /**
     * Creates a short URL for the given URL and optional data.
     *
     * @param url The URL to be shortened.
     * @param data The optional properties for the short URL.
     * @return The created [ShortUrl] entity.
     */
    fun create(url: String, data: ShortUrlProperties): ShortUrl
    /**
     * Finds a [ShortUrl] by its key.
     *
     * @param key The key of the [ShortUrl].
     * @return The found [ShortUrl] or null if not found.
     */
    fun findByKey(@NonNull key: String): ShortUrl?
}

/**
 * Implementation of [CreateShortUrlUseCase].
 */
class CreateShortUrlUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val validatorService: ValidatorService,
    private val hashService: HashService
) : CreateShortUrlUseCase {
    /**
     * Creates a short URL for the given URL and optional data.
     *
     * @param url The URL to be shortened.
     * @param data The optional properties for the short URL.
     * @return The created [ShortUrl] entity.
     * @throws InvalidUrlException if the URL is not valid or exists a branded link and its invalid .
     */
    override fun create(url: String, data: ShortUrlProperties): ShortUrl =
        if (safeCall { validatorService.isValid(url) }) {
            if (!data.brand.isNullOrEmpty() && !safeCall { validatorService.isValidDomain(data.brand) }) {
                throw InvalidUrlException(data.brand)
            }
            val id = safeCall { hashService.hasUrl(url) }
            val su = ShortUrl(
                hash = id,
                redirection = Redirection(target = url),
                properties = ShortUrlProperties(
                    safe = data.safe,
                    ip = data.ip,
                    sponsor = data.sponsor,
                    brand = data.brand
                )
            )
            safeCall { shortUrlRepository.save(su) }
        } else {
            throw InvalidUrlException(url)
        }
    /**
     * Finds a [ShortUrl] by its key.
     *
     * @param key The key of the [ShortUrl].
     * @return The found [ShortUrl] or null if not found.
     */
    override fun findByKey(@NonNull key: String): ShortUrl? = shortUrlRepository.findByKey(key)
}
