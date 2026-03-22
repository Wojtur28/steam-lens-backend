package com.example.steamlensbackend.kafka.consumer;

import com.example.steamlensbackend.async.model.RequestStatus;
import com.example.steamlensbackend.async.service.AsyncRequestService;
import com.example.steamlensbackend.family.dto.SharedLibraryPriceResponse;
import com.example.steamlensbackend.family.service.FamilyService;
import com.example.steamlensbackend.kafka.dto.SharedLibraryRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedLibraryRequestConsumer {

    private final FamilyService familyService;
    private final AsyncRequestService asyncRequestService;

    @KafkaListener(
            topics = "${async.request.kafka.topic:shared-library-requests}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeSharedLibraryRequest(SharedLibraryRequestMessage message) {
        String requestId = message.requestId();
        log.info("Received shared library request: requestId={}, familyGroupId={}",
                requestId, message.familyGroupId());

        try {
            asyncRequestService.updateStatus(requestId, RequestStatus.PROCESSING);

            Pageable pageable = PageRequest.of(message.page(), message.size());

            SharedLibraryPriceResponse result = familyService.getSharedLibraryApps(
                    message.accessToken(),
                    message.familyGroupId(),
                    message.steamId(),
                    pageable,
                    message.apiKey()
            );

            asyncRequestService.saveResult(requestId, result);
            log.info("Successfully processed request: requestId={}", requestId);

        } catch (Exception e) {
            log.error("Error processing request: requestId={}, error={}", requestId, e.getMessage(), e);
            asyncRequestService.markFailed(requestId, e.getMessage());
        }
    }
}
