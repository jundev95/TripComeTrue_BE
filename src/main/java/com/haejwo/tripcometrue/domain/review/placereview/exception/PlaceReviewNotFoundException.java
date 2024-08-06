package com.haejwo.tripcometrue.domain.review.placereview.exception;

import com.haejwo.tripcometrue.global.exception.ApplicationException;
import com.haejwo.tripcometrue.global.exception.ErrorCode;

public class PlaceReviewNotFoundException extends ApplicationException {

  private static ErrorCode ERROR_CODE = ErrorCode.PLACE_REVIEW_NOT_FOUND;

  public PlaceReviewNotFoundException(){
    super(ERROR_CODE);
  }
}
