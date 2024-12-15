package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.usecases.GeolocationUseCase
import es.unizar.urlshortener.core.usecases.Geolocation
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClientException


/**
 * Controller responsible for handling geolocation requests.
 */
@RestController
class GeolocationController(
    private val geolocationUseCase: GeolocationUseCase
) {

    /**
     * Returns geolocation data for the client's IP.
     *
     * @param request the HTTP request
     * @return a ResponseEntity with geolocation data
     */
    @GetMapping("/api/geolocation")
    fun getGeolocation(request: HttpServletRequest): ResponseEntity<Any> {
        return try {
            val ip = getClientIp(request)
            val geolocation = geolocationUseCase.getGeolocation(ip)

            ResponseEntity.ok(geolocation)
        } catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        } catch (ex: RestClientException) {
            ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(mapOf("error" to "Failed to fetch geolocation data: ${ex.message}"))
        }
    }

    fun getClientIp(request: HttpServletRequest): String {
        return request.getRemoteAddr()
    }
}
