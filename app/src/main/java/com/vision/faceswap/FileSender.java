package com.vision.faceswap;

import com.google.common.io.Files;
import com.google.common.io.Resources;

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

    public File send(final File source, final File destination) throws IOException {

        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>() {{
            add("src", new FileSystemResource(source));
            add("dst", new FileSystemResource(destination));
        }};
        final HttpHeaders headers = new HttpHeaders() {{
            setContentType(MediaType.MULTIPART_FORM_DATA);
        }};

        final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        final RestTemplate restTemplate = new RestTemplate(true);

        byte[] image = restTemplate
                .exchange(URL, HttpMethod.POST, requestEntity, byte[].class)
                .getBody();

        final File outputFile = new File(Resources.getResource("result.jpg").getFile());
        Files.write(image, outputFile);

        return outputFile;
    }

}
