package com.vision.faceswap;

import android.content.Context;
import android.os.Environment;
import android.os.StrictMode;

import com.google.common.io.Files;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

public class FileSender {
    private static final String URL = "https://face-swap-server.herokuapp.com/swap";

    public File send(final File source, final File destination, Context context) throws IOException {


        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>() {{
            add("src", new FileSystemResource(source));
            add("dst", new FileSystemResource(destination));
        }};
        final HttpHeaders headers = new HttpHeaders() {{
            setContentType(MediaType.MULTIPART_FORM_DATA);
        }};
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        final RestTemplate restTemplate = new RestTemplate(true);
        SimpleClientHttpRequestFactory rf =
                (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        rf.setReadTimeout(100 * 1000);
        rf.setConnectTimeout(100* 1000);


        byte[] image = restTemplate
                .exchange(URL, HttpMethod.POST, requestEntity, byte[].class)
                .getBody();

        final File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File outputFile = File.createTempFile(
                "result",  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );
        Files.write(image, outputFile);

        return outputFile;
    }

}
