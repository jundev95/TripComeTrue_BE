package com.haejwo.tripcometrue.domain.place.service;

import com.haejwo.tripcometrue.domain.city.entity.City;
import com.haejwo.tripcometrue.domain.city.exception.CityNotFoundException;
import com.haejwo.tripcometrue.domain.city.repository.CityRepository;
import com.haejwo.tripcometrue.domain.place.dto.request.PlaceRequestDto;
import com.haejwo.tripcometrue.domain.place.dto.response.PlaceListItemResponseDto;
import com.haejwo.tripcometrue.domain.place.dto.response.PlaceMapInfoResponseDto;
import com.haejwo.tripcometrue.domain.place.dto.response.PlaceNearbyResponseDto;
import com.haejwo.tripcometrue.domain.place.dto.response.PlaceResponseDto;
import com.haejwo.tripcometrue.domain.place.entity.Place;
import com.haejwo.tripcometrue.domain.place.exception.PlaceNotFoundException;
import com.haejwo.tripcometrue.domain.place.repositroy.PlaceRepository;
import com.haejwo.tripcometrue.domain.store.repository.PlaceStoreRepository;
import com.haejwo.tripcometrue.global.springsecurity.PrincipalDetails;
import java.util.List;
import java.util.Objects;

import com.haejwo.tripcometrue.domain.triprecord.dto.query.TripRecordScheduleImageWithPlaceIdQueryDto;
import com.haejwo.tripcometrue.domain.triprecord.repository.triprecord_schedule_image.TripRecordScheduleImageRepository;
import com.haejwo.tripcometrue.global.util.SliceResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final CityRepository cityRepository;
    private final TripRecordScheduleImageRepository tripRecordScheduleImageRepository;
    private final PlaceStoreRepository placeStoreRepository;

    @Transactional
    public PlaceResponseDto addPlace(PlaceRequestDto requestDto) {
        City city = cityRepository.findById(requestDto.cityId())
            .orElseThrow(CityNotFoundException::new);
        Place requestPlace = requestDto.toEntity(city);
        Place savedPlace = placeRepository.save(requestPlace);
        PlaceResponseDto responseDto = PlaceResponseDto.fromEntity(savedPlace);

        return responseDto;

    }

    @Transactional(readOnly = true)
    public PlaceResponseDto findPlace(PrincipalDetails principalDetails, Long placeId) {

        Place findPlace = findPlaceById(placeId);
        
        Boolean isStored = false;
        if(principalDetails != null) {
            isStored = placeStoreRepository.existsByMemberAndPlace(principalDetails.getMember(), findPlace);
        }

        PlaceResponseDto responseDto = PlaceResponseDto.fromEntity(findPlace, isStored);

        return responseDto;
    }

    @Transactional(readOnly = true)
    public Page<PlaceResponseDto> findPlaces(Pageable pageable, Integer storedCount) {

        Page<Place> findPlaces = placeRepository.findPlaceWithFilter(pageable, storedCount);

        Page<PlaceResponseDto> result = findPlaces.map(PlaceResponseDto::fromEntity);

        return result;

    }

    @Transactional(readOnly = true)
    public SliceResponseDto<PlaceListItemResponseDto> listPlacesByName(
        String placeName, Pageable pageable
    ) {
        Slice<Place> places = placeRepository.findPlacesWithCityByPlaceName(placeName, pageable);

        return SliceResponseDto.of(
            places.map(
                place ->
                    PlaceListItemResponseDto
                        .fromEntity(
                            place,
                            // 여행지 대표이미지 추출
                            tripRecordScheduleImageRepository
                                .findInPlaceIdsOrderByCreatedAtDesc(
                                    places.getContent()
                                        .stream()
                                        .map(Place::getId)
                                        .toList()
                                )
                                .stream()
                                .filter(Objects::nonNull)
                                .findFirst()
                                .map(TripRecordScheduleImageWithPlaceIdQueryDto::imageUrl)
                                .orElse(null)
                        )
            )
        );
    }

    public List<PlaceMapInfoResponseDto> findPlaceMapInfoList(Long placeId) {

        List<PlaceMapInfoResponseDto> responseDtos = placeRepository.findPlaceMapInfoListByPlaceId(placeId);

        return responseDtos;
    }

    public List<PlaceNearbyResponseDto> findNearbyPlaceList(Long placeId) {

        List<PlaceNearbyResponseDto> responseDtos = placeRepository.findNearbyPlaces(placeId);

        return responseDtos;

    }

    @Transactional
    public PlaceResponseDto modifyPlace(Long placeId, PlaceRequestDto requestDto) {

        Place place = findPlaceById(placeId);
        place.update(requestDto);
        PlaceResponseDto responseDto = PlaceResponseDto.fromEntity(place);

        return responseDto;
    }

    @Transactional
    public void removePlace(Long placeId) {
        Place findPlace = findPlaceById(placeId);
        placeRepository.delete(findPlace);
    }

    private Place findPlaceById(Long placeId) {

        Place findPlace = placeRepository.findById(placeId)
            .orElseThrow(PlaceNotFoundException::new);

        return findPlace;
    }

}
