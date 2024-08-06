package com.haejwo.tripcometrue.domain.member.facade;

import com.haejwo.tripcometrue.domain.member.dto.response.*;
import com.haejwo.tripcometrue.domain.member.service.MemberReadSearchService;
import com.haejwo.tripcometrue.domain.triprecord.dto.response.triprecord.TripRecordListItemResponseDto;
import com.haejwo.tripcometrue.domain.triprecord.dto.response.triprecord_schedule_media.TripRecordScheduleVideoListItemResponseDto;
import com.haejwo.tripcometrue.domain.triprecord.service.TripRecordScheduleVideoService;
import com.haejwo.tripcometrue.domain.triprecord.service.TripRecordService;
import com.haejwo.tripcometrue.global.util.SliceResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberReadSearchFacade {

    private final MemberReadSearchService memberReadSearchService;
    private final TripRecordScheduleVideoService tripRecordScheduleVideoService;
    private final TripRecordService tripRecordService;

    public MemberSearchResultWithContentResponseDto searchByNicknameResultWithContent(String nickname) {
        List<MemberSimpleResponseDto> members = memberReadSearchService.searchByNickname(nickname);

        List<Long> memberIds = members
            .stream()
            .map(MemberSimpleResponseDto::memberId)
            .toList();

        List<TripRecordScheduleVideoListItemResponseDto> videos = tripRecordScheduleVideoService.getVideosInMemberIds(memberIds);
        List<TripRecordListItemResponseDto> tripRecords = tripRecordService.findTripRecordsWihMemberInMemberIds(memberIds);

        return MemberSearchResultWithContentResponseDto.builder()
            .members(members)
            .videos(videos)
            .tripRecords(tripRecords)
            .build();
    }

    public SliceResponseDto<MemberDetailListItemResponseDto> searchByNicknamePagination(String query, Pageable pageable) {

        SliceResponseDto<MemberSimpleResponseDto> sliceResult = memberReadSearchService.searchByNickname(query, pageable);

        List<Long> memberIds = sliceResult.content()
            .stream()
            .map(MemberSimpleResponseDto::memberId)
            .toList();

        Map<Long, List<TripRecordListItemResponseDto>> tripRecordMap = getGroupByMemberIdTripRecordMap(
            tripRecordService.findTripRecordsWihMemberInMemberIds(memberIds)
        );

        Map<Long, List<TripRecordScheduleVideoListItemResponseDto>> videoMap = getGroupByMemberIdVideoMap(
            tripRecordScheduleVideoService.getVideosInMemberIds(memberIds)
        );


        return SliceResponseDto.<MemberDetailListItemResponseDto>builder()
            .content(
                sliceResult.content()
                    .stream()
                    .map(dto -> {
                            int tripRecordTotal = (Objects.isNull(tripRecordMap.get(dto.memberId()))) ? 0 : tripRecordMap.get(dto.memberId()).size();
                            int videoTotal = (Objects.isNull(videoMap.get(dto.memberId()))) ? 0 : videoMap.get(dto.memberId()).size();

                            return MemberDetailListItemResponseDto.of(dto, tripRecordTotal, videoTotal);
                        }
                    )
                    .toList()
            )
            .totalCount(sliceResult.totalCount())
            .currentPageNum(sliceResult.currentPageNum())
            .pageSize(sliceResult.pageSize())
            .sort(sliceResult.sort())
            .first(sliceResult.first())
            .last(sliceResult.last())
            .build();
    }

    public MemberCreatorInfoResponseDto getCreatorInfo(Long memberId) {
        MemberSimpleResponseDto memberInfo = memberReadSearchService.getMemberSimpleInfo(memberId);

        List<TripRecordScheduleVideoListItemResponseDto> videos = tripRecordScheduleVideoService.getVideosInMemberIds(List.of(memberId));
        List<TripRecordListItemResponseDto> tripRecords = tripRecordService.findTripRecordsWihMemberInMemberIds(List.of(memberId));

        return MemberCreatorInfoResponseDto.builder()
            .memberDetailInfo(
                MemberDetailListItemResponseDto.of(
                    memberInfo, tripRecords.size(), videos.size()
                )
            )
            .videos(videos)
            .tripRecords(tripRecords)
            .build();
    }

    public List<MemberInfoWithTripRecordsResponseDto> listTopMemberCreators() {
        List<MemberSimpleResponseDto> memberSimpleInfos = memberReadSearchService.listTopMemberSimpleInfos();
        log.info("memberSimpleInfo");


        Map<Long, List<TripRecordListItemResponseDto>> tripRecordMap = getGroupByMemberIdTripRecordMap(
            tripRecordService.findTripRecordsWihMemberInMemberIds(
                memberSimpleInfos
                    .stream()
                    .map(MemberSimpleResponseDto::memberId)
                    .toList()
            )
        );

        return memberSimpleInfos.stream()
            .map(memberInfo -> MemberInfoWithTripRecordsResponseDto.builder()
                .memberInfo(memberInfo)
                .tripRecords(
                    Objects.nonNull(tripRecordMap.get(memberInfo.memberId())) ?
                        tripRecordMap.get(memberInfo.memberId()) : new ArrayList<>()
                )
                .build()
            )
            .toList();
    }

    private Map<Long, List<TripRecordScheduleVideoListItemResponseDto>> getGroupByMemberIdVideoMap(
        List<TripRecordScheduleVideoListItemResponseDto> tripRecordScheduleVideos
    ) {
        return tripRecordScheduleVideos
            .stream()
            .collect(Collectors.groupingBy(TripRecordScheduleVideoListItemResponseDto::memberId));
    }

    private Map<Long, List<TripRecordListItemResponseDto>> getGroupByMemberIdTripRecordMap(
        List<TripRecordListItemResponseDto> tripRecords
    ) {
        return tripRecords
            .stream()
            .collect(Collectors.groupingBy(TripRecordListItemResponseDto::memberId));
    }
}
