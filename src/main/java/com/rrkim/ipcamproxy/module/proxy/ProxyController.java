package com.rrkim.ipcamproxy.module.proxy;

import com.rrkim.ipcamproxy.module.proxy.dto.IPCamApiResponse;
import com.rrkim.ipcamproxy.module.proxy.dto.SecureKeyRequestDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProxyController {
    private final RestTemplate restTemplate;

    @Value("${talchwi.ipcam.url}")
    private String cameraUrl;

    @GetMapping("/stream")
    public void streamCamera(HttpServletResponse response) {
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

        restTemplate.execute(cameraUrl + "/stream", HttpMethod.GET, requestCallback, responseExtractor);
    }

    @PostMapping("/auth/secure-key")
    public ResponseEntity<IPCamApiResponse> secureKey(@RequestBody(required = false) SecureKeyRequestDto secureKeyRequestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SecureKeyRequestDto> requestEntity = new HttpEntity<>(secureKeyRequestDto, headers);

        ResponseEntity<IPCamApiResponse> response = restTemplate.exchange(cameraUrl + "/auth/secure-key", HttpMethod.POST, requestEntity, IPCamApiResponse.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
