$(document).ready(
    /**
     * Initializes the document and sets up the form submission handler.
     */
    function () {
        $("#shortener").submit(
            /**
             * Handles the form submission event.
             * Prevents the default form submission and sends an AJAX POST request.
             * @param {Event} event - The form submission event.
             */
            function (event) {
                event.preventDefault();
                const isGeolocated = $("#geolocationCheckbox").is(":checked");
                $.ajax({
                    type: "POST",
                    url: "/api/link",
                    data: $(this).serialize(),
                    /**
                     * Handles the successful AJAX response.
                     * Displays the shortened URL in the result div.
                     * @param {Object} msg - The response message.
                     * @param {string} status - The status of the response.
                     * @param {Object} request - The XMLHttpRequest object.
                     */
                    success: function (msg, status, request) {
                        const target = msg.url;
                        $("#result").html(
                            "<div class='alert alert-success lead'><a target='_blank' href='"
                            + target
                            + "'>"
                            + request.getResponseHeader('Location')
                            + "</a></div>");
                    },
                    /**
                     * Handles the AJAX error response.
                     * Displays an error message in the result div.
                     */
                    error: function () {
                        $("#result").html(
                            "<div class='alert alert-danger lead'>ERROR</div>");
                    }
                });
                if (isGeolocated) {
                    $.ajax({
                        type: "GET",
                        url: "/api/geolocation",
                        success: function (data) {
                            const resultHtml = `
                                <div class='alert alert-success lead'>
                                    <p>Latitude: ${data.latitude}</p>
                                    <p>Longitude: ${data.longitude}</p>
                                    <p>Country: ${data.country}</p>
                                    <p>City: ${data.city}</p>
                                </div>`;
                            $("#geolocation_result").html(resultHtml);
                        },
                        error: function (jqXHR) {
                            alert("Error when fetching geolocation: " + jqXHR.responseText);
                            $("#geolocation_result").html("<div class='alert alert-danger lead'>ERROR</div>");
                        }
                    });
                }
            });
        $("#qr_generator").submit(
            /**
             * Handles the form submission event.
             * Prevents the default form submission and sends an AJAX GET request.
             * @param {Event} event - The form submission event.
             */
            function (event) {
                event.preventDefault();
                $.ajax({
                    type: "POST",
                    url: "/api/qr",
                    data: $(this).serialize(),
                    xhrFields: {
                        responseType: 'blob' // Handles binary image
                    },
                    /**
                     * Handles the successful AJAX response.
                     * Displays the generated QR in the qr_result div.
                     * @param {Object} blob - The response message.
                     */
                    success: function (blob) {
                        const imageUrl = URL.createObjectURL(blob);
                        $("#qr_result").html(
                            "<div>" +
                            "<a target='_blank' href='" + imageUrl + "'>" +
                            "<img src='" + imageUrl + "' alt='QR Code'>" +
                            "</a></div>"
                        );
                    },
                    /**
                     * Handles the AJAX error response.
                     * Displays an error message in the result div.
                     */
                    error: function(jqXHR, textStatus, errorThrown) {
                        alert("Error when generating QR code: " + jqXHR.responseText);
                        $("#qr_result").html(
                            "<div class='alert alert-danger lead'>ERROR</div>");
                    }
                })
            });
        $("#csv_upload").submit(
            function (event) {
                event.preventDefault();
                $.ajax({
                    type: "POST",
                    url: "/api/csv",
                    data: new FormData(this),
                    processData: false,
                    contentType: false,
                    xhrFields: {
                        responseType: 'blob'
                    },

                    success: function (blob, status, xhr) {
                        if (blob instanceof Blob) {
                            const contentDisposition = xhr.getResponseHeader("Content-Disposition");
                            const filename = contentDisposition.split('filename=')[1].replace(/"/g, '');
                            const url = window.URL.createObjectURL(blob);

                            $("#csv_result").html(
                                "<div class='alert alert-success lead'>" +
                                "CSV file uploaded. <a href='" + url + "' download='" + filename + "'>Download the file</a>" +
                                "</div>"
                            );
                        } else {
                            alert("Error: the response is not a valid Blob.");
                        }
                    },

                    error: function () {
                        alert("Error when uploading CSV file: " + jqXHR.responseText);
                        $("#csv_result").html(
                            "<div class='alert alert-danger lead'>ERROR</div>"
                        );
                    }
                });
            });
    });