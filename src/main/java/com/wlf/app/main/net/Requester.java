package com.wlf.app.main.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wlf.app.main.data.ContentLanguage;
import lombok.Data;

public class Requester implements Closeable {
    Logger log = Logger.getLogger(Requester.class.getSimpleName());

    public static final String MOD_API_URL = "https://grorepository.ru/api/mods/";
    public static final String MOD_SITE_URL = "https://grorepository.ru/mod/";
    private static final String DOWNLOAD_API_URL = "https://grorepository.ru/api/mods/download/";
    private static final String DOWNLOAD_API_FILE_URL = "https://grorepository.ru/api/file/";

    private final HttpClient client;
    private final ObjectMapper mapper;

    public Requester() {
        client = HttpClient.newBuilder()
                            //.sslContext(createSslContext())
                            .build();
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    // GET https://grorepository.ru/api/mods/gates-to-hammurabi

    public JsonNode getJsonFromURL(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        log.log(Level.INFO, "Sending request to fetch metadata for: {0}", url);
        HttpResponse<String> response;
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            log.log(Level.SEVERE, "Failed to fetch file URI. HTTP Status: {0}", response.statusCode());
            return mapper.readTree(response.body());
        }

        return null;
    }

    /**
     * Sends request to grorepository API, fetching all metadata for a given mod
     * @param modName
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public ModInfo requestModInfo(String modName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MOD_API_URL + modName))
                .GET()
                .build();

        log.log(Level.INFO, "Sending request to fetch metadata for: {0}", modName);
        HttpResponse<String> response;
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            ModInfo modInfo = mapper.readValue(response.body(), ModInfo.class);
            log.log(Level.INFO, "Fetched mod info:\n", modInfo);
            return modInfo;
        }

        return null;
    }

    /**
     * Requests the actual file download URL via grorepository API that can be used to initiate downloads.
     * @param modId
     * @param language
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public URI requestDownloadURI(long modId, ContentLanguage language) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DOWNLOAD_API_URL
                        + modId
                        + "?linkType="
                        + language.ordinal()))
                .GET()
                .build();

        log.log(Level.INFO, "Sending request to: {0}", request.uri());

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.log(Level.SEVERE, "Failed to fetch file URI. HTTP Status: {0}", response.statusCode());
            return null;
        }

        // "https://grorepository.ru/api/file/080b9fbe-1ffb-4903-9c99-b593fad060aa"
        if (response.body().startsWith(DOWNLOAD_API_FILE_URL)) {
            return URI.create(response.body());
        }

        return null;
    }

    public FileInfo requestFileInfo(URI fileURI) throws IOException, InterruptedException {
        HttpRequest fileInfoRequest = HttpRequest.newBuilder()
                .uri(fileURI)
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();

        log.log(Level.INFO, "Sending request to: {0}", fileURI.toString());
        HttpResponse<Void> httpResponse = client.send(fileInfoRequest, HttpResponse.BodyHandlers.discarding());

        if (httpResponse.statusCode() == 200) {
            String contentDisposition = httpResponse.headers().firstValue("content-disposition").orElse("unknown");
            String contentLength = httpResponse.headers().firstValue("content-length").orElse("unknown");
            log.log(Level.INFO, "Got content: {0}", contentDisposition);
            log.log(Level.INFO, "total size: {0}", contentLength);

            FileInfo fileInfo = new FileInfo();
            fileInfo.setTotalFileSize(Integer.parseInt(contentLength));
            if (contentDisposition.contains("filename=")) {
                fileInfo.setFileName(contentDisposition.substring(contentDisposition.indexOf("filename=") + "filename=".length(), contentDisposition.length())
                        .replace("\"", ""));
            }

            return fileInfo;
        } else {
            log.log(Level.SEVERE, "Failed to download file. HTTP Status: {0}", httpResponse.statusCode());
        }

        return null;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Data
    public static class FileInfo {
        private int totalFileSize;
        private String fileName;
    }
}
