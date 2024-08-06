package com.haejwo.tripcometrue.domain.member.controller;

import com.haejwo.tripcometrue.domain.member.dto.response.MemberCreatorInfoResponseDto;
import com.haejwo.tripcometrue.domain.member.dto.response.MemberDetailListItemResponseDto;
import com.haejwo.tripcometrue.domain.member.dto.response.MemberSearchResultWithContentResponseDto;
import com.haejwo.tripcometrue.domain.member.facade.MemberReadSearchFacade;
import com.haejwo.tripcometrue.global.util.ResponseDTO;
import com.haejwo.tripcometrue.global.util.SliceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/v1/members")
@RestController
public class MemberReadSearchController {

    private final MemberReadSearchFacade memberReadSearchFacade;

    @GetMapping("/{memberId}")
    public ResponseEntity<ResponseDTO<MemberCreatorInfoResponseDto>> memberCreatorInfo(
        @PathVariable("memberId") Long memberId
    ) {
        return ResponseEntity
            .ok()
            .body(
                ResponseDTO.okWithData(
                    memberReadSearchFacade.getCreatorInfo(memberId)
                )
            );
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseDTO<MemberSearchResultWithContentResponseDto>> searchByNicknameResultWithContent(
        @RequestParam("query") String query
    ) {
        return ResponseEntity
            .ok()
            .body(
                ResponseDTO.okWithData(
                    memberReadSearchFacade.searchByNicknameResultWithContent(query)
                )
            );
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<SliceResponseDto<MemberDetailListItemResponseDto>>> searchByNicknamePagination(
        @RequestParam("query") String query,
        @PageableDefault(sort = "rating", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity
            .ok()
            .body(
                ResponseDTO.okWithData(
                    memberReadSearchFacade.searchByNicknamePagination(query, pageable)
                )
            );
    }

    // 홈 피드 HOT 인기 크리에이터 리스트 조회
    @GetMapping("/top-list")
    public ResponseEntity<ResponseDTO<?>> listTopCreators() {
        return ResponseEntity
            .ok()
            .body(
                ResponseDTO.okWithData(
                    memberReadSearchFacade.listTopMemberCreators()
                )
            );
    }
}
