package com.haejwo.tripcometrue.domain.triprecord.service;

import com.haejwo.tripcometrue.domain.city.entity.City;
import com.haejwo.tripcometrue.domain.city.exception.CityNotFoundException;
import com.haejwo.tripcometrue.domain.city.repository.CityRepository;
import com.haejwo.tripcometrue.domain.place.entity.Place;
import com.haejwo.tripcometrue.domain.place.exception.PlaceNotFoundException;
import com.haejwo.tripcometrue.domain.place.repositroy.PlaceRepository;
import com.haejwo.tripcometrue.domain.triprecord.dto.request.CreateSchedulePlaceRequestDto;
import com.haejwo.tripcometrue.domain.triprecord.dto.request.TripRecordRequestDto;
import com.haejwo.tripcometrue.domain.triprecord.dto.request.TripRecordScheduleRequestDto;
import com.haejwo.tripcometrue.domain.triprecord.dto.response.triprecord.GetCountryCityResponseDto;
import com.haejwo.tripcometrue.domain.triprecord.dto.response.triprecord.GetCountryResponseDto;
import com.haejwo.tripcometrue.domain.triprecord.dto.response.triprecord_schedule.SearchScheduleTripResponseDto;
import com.haejwo.tripcometrue.domain.triprecord.entity.TripRecord;
import com.haejwo.tripcometrue.domain.triprecord.entity.TripRecordImage;
import com.haejwo.tripcometrue.domain.triprecord.entity.TripRecordSchedule;
import com.haejwo.tripcometrue.domain.triprecord.entity.TripRecordScheduleImage;
import com.haejwo.tripcometrue.domain.triprecord.entity.TripRecordScheduleVideo;
import com.haejwo.tripcometrue.domain.triprecord.entity.TripRecordTag;
import com.haejwo.tripcometrue.domain.triprecord.exception.TripRecordNotFoundException;
import com.haejwo.tripcometrue.domain.triprecord.repository.triprecord.TripRecordRepository;
import com.haejwo.tripcometrue.domain.triprecord.repository.triprecord_image.TripRecordImageRepository;
import com.haejwo.tripcometrue.domain.triprecord.repository.triprecord_schedule.TripRecordScheduleRepository;
import com.haejwo.tripcometrue.domain.triprecord.repository.triprecord_schedule_image.TripRecordScheduleImageRepository;
import com.haejwo.tripcometrue.domain.triprecord.repository.triprecord_schedule_video.TripRecordScheduleVideoRepository;
import com.haejwo.tripcometrue.domain.triprecord.repository.triprecord_tag.TripRecordTagRepository;
import com.haejwo.tripcometrue.global.enums.Continent;
import com.haejwo.tripcometrue.global.enums.Country;
import com.haejwo.tripcometrue.global.exception.PermissionDeniedException;
import com.haejwo.tripcometrue.global.springsecurity.PrincipalDetails;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripRecordEditService {

    private final TripRecordRepository tripRecordRepository;
    private final TripRecordImageRepository tripRecordImageRepository;
    private final TripRecordTagRepository tripRecordTagRepository;
    private final TripRecordScheduleRepository tripRecordScheduleRepository;
    private final TripRecordScheduleImageRepository tripRecordScheduleImageRepository;
    private final TripRecordScheduleVideoRepository tripRecordScheduleVideoRepository;
    private final CityRepository cityRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public void addTripRecord(PrincipalDetails principalDetails, TripRecordRequestDto requestDto) {
        validatePlaceId(requestDto.tripRecordSchedules());
        TripRecord requestTripRecord = requestDto.toEntity(principalDetails.getMember());
        tripRecordRepository.save(requestTripRecord);

        saveTripRecordImages(requestDto, requestTripRecord);
        saveHashTags(requestDto, requestTripRecord);
        saveTripRecordSchedules(requestDto, requestTripRecord);
    }

    private void saveTripRecordImages(TripRecordRequestDto requestDto,
        TripRecord requestTripRecord) {
        requestDto.tripRecordImages().forEach(tripRecordImageRequestDto -> {
            TripRecordImage tripRecordImage = tripRecordImageRequestDto.toEntity(requestTripRecord);
            tripRecordImageRepository.save(tripRecordImage);
        });
    }

    private void saveHashTags(TripRecordRequestDto requestDto, TripRecord requestTripRecord) {
        requestDto.hashTags().forEach(hashTag -> {
            TripRecordTag tripRecordTag = TripRecordTag.builder().hashTagType(hashTag)
                .tripRecord(requestTripRecord).build();
            tripRecordTagRepository.save(tripRecordTag);
        });
    }

    private void saveTripRecordSchedules(TripRecordRequestDto requestDto,
        TripRecord requestTripRecord) {
        requestDto.tripRecordSchedules().forEach(tripRecordScheduleRequestDto -> {
            Place place = placeRepository.findById(tripRecordScheduleRequestDto.placeId())
                .orElseThrow(PlaceNotFoundException::new);

            TripRecordSchedule tripRecordSchedule = tripRecordScheduleRequestDto.toEntity(
                requestTripRecord, place);
            tripRecordScheduleRepository.save(tripRecordSchedule);

            saveTripRecordScheduleImages(tripRecordScheduleRequestDto, tripRecordSchedule);
            saveTripRecordScheduleVideos(tripRecordScheduleRequestDto, tripRecordSchedule);
        });
    }

    private void saveTripRecordScheduleImages(TripRecordScheduleRequestDto requestDto,
        TripRecordSchedule tripRecordSchedule) {
        requestDto.tripRecordScheduleImages().forEach(tripRecordScheduleImageUrl -> {
            TripRecordScheduleImage tripRecordImage = TripRecordScheduleImage.builder()
                .imageUrl(tripRecordScheduleImageUrl).tripRecordSchedule(tripRecordSchedule)
                .build();
            tripRecordScheduleImageRepository.save(tripRecordImage);
        });
    }

    private void saveTripRecordScheduleVideos(@NotNull TripRecordScheduleRequestDto requestDto,
        TripRecordSchedule tripRecordSchedule) {
        requestDto.tripRecordScheduleVideos().forEach(tripRecordScheduleVideoUrl -> {
            TripRecordScheduleVideo tripRecordScheduleVideo = TripRecordScheduleVideo.builder()
                .videoUrl(tripRecordScheduleVideoUrl).tripRecordSchedule(tripRecordSchedule)
                .build();
            tripRecordScheduleVideoRepository.save(tripRecordScheduleVideo);
        });
    }

    public List<SearchScheduleTripResponseDto> searchSchedulePlace(Country country, String city) {
        return cityRepository.findByNameAndCountry(city, country).map(
            foundCity -> placeRepository.findByCityId(foundCity.getId())
                .stream().map(SearchScheduleTripResponseDto::fromEntity)
                .collect(Collectors.toList())).orElseGet(() -> new ArrayList<>());
    }

    public Long createSchedulePlace(CreateSchedulePlaceRequestDto createSchedulePlaceRequestDto) {
        City city = cityRepository.findByNameAndCountry(createSchedulePlaceRequestDto.cityname(),
            createSchedulePlaceRequestDto.country()).orElseThrow(CityNotFoundException::new);

        return placeRepository.save(createSchedulePlaceRequestDto.toEntity(city)).getId();
    }

    @Transactional
    public void deleteTripRecord(PrincipalDetails principalDetails, Long tripRecordId) {

        Optional<TripRecord> tripRecord = tripRecordRepository.findById(tripRecordId);

        if (tripRecord.isPresent()) {
            TripRecord foundTripRecord = tripRecord.get();
            if (foundTripRecord.getMember().getId().equals(principalDetails.getMember().getId())) {
                tripRecordRepository.delete(foundTripRecord);
            } else {
                throw new PermissionDeniedException();
            }
        } else {
            throw new TripRecordNotFoundException();
        }
    }

    @Transactional
    public void modifyTripRecord(PrincipalDetails principalDetails,
        TripRecordRequestDto requestDto, Long tripRecordId) {

        validatePlaceId(requestDto.tripRecordSchedules());
        Optional<TripRecord> tripRecord = tripRecordRepository.findById(tripRecordId);

        if (tripRecord.isPresent()) {
            TripRecord foundTripRecord = tripRecord.get();
            if (foundTripRecord.getMember().getId().equals(principalDetails.getMember().getId())) {

                foundTripRecord.update(requestDto);
                tripRecordRepository.save(foundTripRecord);

                deleteTripRecordAssociations(foundTripRecord);
                saveTripRecordImages(requestDto, foundTripRecord);
                saveHashTags(requestDto, foundTripRecord);
                saveTripRecordSchedules(requestDto, foundTripRecord);

            } else {
                throw new PermissionDeniedException();
            }
        } else {
            throw new TripRecordNotFoundException();
        }
    }

    private void deleteTripRecordAssociations(TripRecord foundTripRecord) {
        tripRecordImageRepository.deleteAllByTripRecordId(foundTripRecord.getId());
        tripRecordTagRepository.deleteAllByTripRecordId(foundTripRecord.getId());
        tripRecordScheduleRepository.deleteAllByTripRecordId(foundTripRecord.getId());
    }

    private void validatePlaceId(
        List<TripRecordScheduleRequestDto> tripRecordScheduleRequestDtoList) {
        for (TripRecordScheduleRequestDto tripRecordScheduleRequestDto : tripRecordScheduleRequestDtoList) {
            placeRepository.findById(tripRecordScheduleRequestDto.placeId())
                .orElseThrow(PlaceNotFoundException::new);
        }
    }

    public List<GetCountryResponseDto> getCountryCity(Continent continent) {

        List<GetCountryResponseDto> responseDtoList = new ArrayList<>();

        for (Country country : Country.values()) {

            if (continent == null || country.getContinent().equals(continent)) {
                List<GetCountryCityResponseDto> cityList = new ArrayList<>();
                cityRepository.findAllByCountry(country).forEach(city -> {
                    cityList.add(GetCountryCityResponseDto.fromEntity(city));
                });

                responseDtoList.add(new GetCountryResponseDto(
                    country.getContinent(),
                    country,
                    country.getDescription(),
                    country.getImageUrl(),
                    cityList
                ));
            }
        }

        return responseDtoList;
    }
}
