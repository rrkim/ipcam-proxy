package com.rrkim.ipcamserver.module.proxy;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.io.OutputStream;

@RestController
@RequiredArgsConstructor
public class ProxyController {
    private final RestTemplate restTemplate;

    @GetMapping("/stream")
    public void streamCamera(HttpServletResponse response) {
        String cameraUrl = "http://localhost:8080/stream";

        RequestCallback requestCallback = request -> {
            request.getHeaders().add(HttpHeaders.ACCEPT, "image/jpeg");
        };

        ResponseExtractor<Void> responseExtractor = clientResponse -> {
            response.setContentType("image/jpeg");
            response.setStatus(HttpStatus.OK.value());

            try (InputStream inputStream = clientResponse.getBody();
                 OutputStream outputStream = response.getOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return null;
        };

        restTemplate.execute(cameraUrl, HttpMethod.GET, requestCallback, responseExtractor);
    }
}
