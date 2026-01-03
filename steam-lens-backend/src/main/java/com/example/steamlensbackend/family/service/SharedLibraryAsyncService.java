package com.example.steamlensbackend.family.service;

import com.example.steamlensbackend.async.dto.AsyncRequestAcceptedResponse;
import com.example.steamlensbackend.async.service.AsyncRequestService;
import com.example.steamlensbackend.kafka.dto.SharedLibraryRequestMessage;
import com.example.steamlensbackend.kafka.producer.SharedLibraryRequestProducer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SharedLibraryAsyncService {

    private final AsyncRequestService asyncRequestService;
    private final SharedLibraryRequestProducer sharedLibraryRequestProducer;

    public AsyncRequestAcceptedResponse initiateSharedLibraryRequest(
            String accessToken,
            String familyGroupId,
            String steamId,
            String apiKey,
            Pageable pageable,
            HttpServletRequest request) {

        String requestId = asyncRequestService.createRequest();

        SharedLibraryRequestMessage message = new SharedLibraryRequestMessage(
                requestId,
                accessToken,
                familyGroupId,
                steamId,
                apiKey,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        sharedLibraryRequestProducer.sendRequest(message);

        String baseUrl = buildBaseUrl(request);
        return AsyncRequestAcceptedResponse.of(requestId, baseUrl);
    }

    private String buildBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        return url.toString();
    }
}
