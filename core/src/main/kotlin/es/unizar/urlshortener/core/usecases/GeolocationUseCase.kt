package es.unizar.urlshortener.core.usecases

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

private const val CACHE_EXPIRATION_HOURS = 24L
private const val CACHE_MAX_SIZE = 100L

@Service
class GeolocationUseCase {

    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(CACHE_EXPIRATION_HOURS, TimeUnit.HOURS)
        .maximumSize(CACHE_MAX_SIZE)
        .build<String, Geolocation>()

    private val apiKey = "30b1fda9ca07af1d6d7c7ca052cd6aff"
    private val apiUrl = "http://api.ipstack.com/{ip}?access_key=$apiKey"

    fun getGeolocation(ip: String): Geolocation {
        require(isValidIpAddress(ip)) { "Invalid IP address: $ip" }

        return cache.get(ip) {
            fetchGeolocationFromApi(it)
        }
    }

    fun fetchGeolocationFromApi(ip: String): Geolocation {
        val url = apiUrl.replace("{ip}", ip)

        try {
            val responseString = RestTemplate().getForObject(url, String::class.java)
                ?: throw IllegalArgumentException("Could not obtain geolocation data for the IP: $ip")

            val countryRegex = """"country_name":\s*"(.*?)"""".toRegex()
            val cityRegex = """"city":\s*"(.*?)"""".toRegex()
            val latitudeRegex = """"latitude":(.*?),""".toRegex()
            val longitudeRegex = """"longitude":(.*?),""".toRegex()

            val countryMatch = countryRegex.find(responseString)?.groups?.get(1)?.value ?: "Unknown Country"
            val cityMatch = cityRegex.find(responseString)?.groups?.get(1)?.value ?: "Unknown City"
            val latitudeMatch = latitudeRegex.find(responseString)?.groups?.get(1)?.value?.toDouble() ?: 0.0
            val longitudeMatch = longitudeRegex.find(responseString)?.groups?.get(1)?.value?.toDouble() ?: 0.0

            return Geolocation(
                latitude = latitudeMatch,
                longitude = longitudeMatch,
                city = cityMatch,
                country = countryMatch
            )
        } catch (ex: RestClientException) {
            throw RestClientException("Failed to connect to the API or fetch geolocation data: ${ex.message}", ex)
        }
    }

    private fun isValidIpAddress(ip: String): Boolean {
        return try {
            val address = InetAddress.getByName(ip)
            !address.isLoopbackAddress && !address.isAnyLocalAddress && !address.isMulticastAddress
        } catch (ex: UnknownHostException) {
            throw IllegalArgumentException("Failed to resolve IP address: $ip", ex)
        }
    }
}

data class Geolocation(
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String
)
