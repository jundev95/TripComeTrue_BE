package com.haejwo.tripcometrue.domain.triprecord.service;

import com.haejwo.tripcometrue.domain.triprecord.dto.query.TripRecordScheduleVideoQueryDto;
import com.haejwo.tripcometrue.domain.triprecord.dto.response.triprecord_schedule_media.TripRecordScheduleVideoListItemResponseDto;
import com.haejwo.tripcometrue.domain.triprecord.repository.triprecord_schedule_video.TripRecordScheduleVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TripRecordScheduleVideoService {

    private final TripRecordScheduleVideoRepository tripRecordScheduleVideoRepository;

    private static final int HOME_CONTENT_SIZE = 5;

    @Transactional(readOnly = true)
    public List<TripRecordScheduleVideoListItemResponseDto> getNewestVideos(String type) {

        List<TripRecordScheduleVideoQueryDto> queryResults;

        if (type.equalsIgnoreCase("all")) {
            queryResults = tripRecordScheduleVideoRepository.findNewestVideos(HOME_CONTENT_SIZE);
        } else if (type.equalsIgnoreCase("domestic")) {
            queryResults = tripRecordScheduleVideoRepository.findNewestVideosDomestic(HOME_CONTENT_SIZE);
        } else {
            queryResults = tripRecordScheduleVideoRepository.findNewestVideosOverseas(HOME_CONTENT_SIZE);
        }

        return queryResults.stream()
            .map(TripRecordScheduleVideoListItemResponseDto::fromQueryDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TripRecordScheduleVideoListItemResponseDto> getVideosInMemberIds(List<Long> memberIds) {

        return tripRecordScheduleVideoRepository
            .findInMemberIds(memberIds)
            .stream()
            .map(TripRecordScheduleVideoListItemResponseDto::fromQueryDto)
            .toList();
    }
}
