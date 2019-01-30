package com.vision.faceswap;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

public class FileSender {
    private static final String URL = "https://face-swap-server.herokuapp.com/swap";

    public String send(final File source, final File destination) throws IOException {

        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>() {{
            add("src", new FileSystemResource(source));
            add("dst", new FileSystemResource(destination));
        }};
        final HttpHeaders headers = new HttpHeaders() {{
            setContentType(MediaType.MULTIPART_FORM_DATA);
        }};

        final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        final RestTemplate restTemplate = new RestTemplate(true);

        return restTemplate
                .exchange(URL, HttpMethod.POST, requestEntity, String.class)
                .getBody();
    }

}
