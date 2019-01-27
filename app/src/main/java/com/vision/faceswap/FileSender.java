package com.vision.faceswap;

import android.os.StrictMode;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;

public class FileSender {
    private static final String URL = "https://face-swap-server.herokuapp.com/swap";

    public File send(final File source, final File destination) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>() {{
            add("src", new FileSystemResource(source));
            add("dst", new FileSystemResource(destination));
        }};
        HttpHeaders headers = new HttpHeaders() {{
            setContentType(MediaType.MULTIPART_FORM_DATA);
        }};

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        RestTemplate restTemplate = new RestTemplate();
        byte[] response = restTemplate
                .postForObject(URL, requestEntity, byte[].class);
        final File outputFile = new File(Resources.getResource("result.jpg").getFile());

        Files.write(response, outputFile);

        return outputFile;
    }

}
