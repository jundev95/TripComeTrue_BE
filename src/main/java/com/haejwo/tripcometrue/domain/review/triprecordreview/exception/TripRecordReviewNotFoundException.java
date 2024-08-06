package com.haejwo.tripcometrue.domain.review.triprecordreview.exception;

import com.haejwo.tripcometrue.global.exception.ApplicationException;
import com.haejwo.tripcometrue.global.exception.ErrorCode;

public class TripRecordReviewNotFoundException extends ApplicationException {

    private static final ErrorCode ERROR_CODE = ErrorCode.TRIP_RECORD_REVIEW_NOT_FOUND;

    public TripRecordReviewNotFoundException() {
        super(ERROR_CODE);
    }
}
