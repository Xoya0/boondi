package com.boondi.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BoondiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    public BoondiException(ErrorCode errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public static BoondiException userNotFound(String identifier) {
        return new BoondiException(
                ErrorCode.USER_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "User not found: " + identifier
        );
    }

    public static BoondiException emailAlreadyExists() {
        return new BoondiException(
                ErrorCode.EMAIL_ALREADY_EXISTS,
                HttpStatus.CONFLICT,
                "Email is already registered"
        );
    }

    public static BoondiException usernameAlreadyExists() {
        return new BoondiException(
                ErrorCode.USERNAME_ALREADY_EXISTS,
                HttpStatus.CONFLICT,
                "Username is already taken"
        );
    }

    public static BoondiException invalidCredentials() {
        return new BoondiException(
                ErrorCode.INVALID_CREDENTIALS,
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
        );
    }

    public static BoondiException accountSuspended() {
        return new BoondiException(
                ErrorCode.ACCOUNT_SUSPENDED,
                HttpStatus.FORBIDDEN,
                "Account has been suspended"
        );
    }

    public static BoondiException tokenInvalid() {
        return new BoondiException(
                ErrorCode.TOKEN_INVALID,
                HttpStatus.UNAUTHORIZED,
                "Token is invalid"
        );
    }

    public static BoondiException tokenExpired() {
        return new BoondiException(
                ErrorCode.TOKEN_EXPIRED,
                HttpStatus.UNAUTHORIZED,
                "Token has expired"
        );
    }

    public static BoondiException emailAlreadyVerified() {
        return new BoondiException(
                ErrorCode.EMAIL_ALREADY_VERIFIED,
                HttpStatus.CONFLICT,
                "Email is already verified"
        );
    }

    public static BoondiException verificationTokenInvalid() {
        return new BoondiException(
                ErrorCode.VERIFICATION_TOKEN_INVALID,
                HttpStatus.BAD_REQUEST,
                "Email verification token is invalid"
        );
    }

    public static BoondiException verificationTokenExpired() {
        return new BoondiException(
                ErrorCode.VERIFICATION_TOKEN_EXPIRED,
                HttpStatus.BAD_REQUEST,
                "Email verification token has expired. Please request a new one."
        );
    }

    public static BoondiException resetTokenInvalid() {
        return new BoondiException(
                ErrorCode.RESET_TOKEN_INVALID,
                HttpStatus.BAD_REQUEST,
                "Password reset token is invalid"
        );
    }

    public static BoondiException resetTokenExpired() {
        return new BoondiException(
                ErrorCode.RESET_TOKEN_EXPIRED,
                HttpStatus.BAD_REQUEST,
                "Password reset token has expired. Please request a new one."
        );
    }

    public static BoondiException fileTooLarge(long maxBytes) {
        return new BoondiException(
                ErrorCode.FILE_TOO_LARGE,
                HttpStatus.PAYLOAD_TOO_LARGE,
                "File exceeds maximum allowed size of " + (maxBytes / (1024 * 1024)) + "MB"
        );
    }

    public static BoondiException invalidFileType() {
        return new BoondiException(
                ErrorCode.INVALID_FILE_TYPE,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Invalid file type. Only JPEG, PNG, and WebP images are allowed."
        );
    }

    public static BoondiException fileUploadFailed(String reason) {
        return new BoondiException(
                ErrorCode.FILE_UPLOAD_FAILED,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "File upload failed: " + reason
        );
    }

    public static BoondiException postNotFound(String postId) {
        return new BoondiException(
                ErrorCode.POST_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "Post not found: " + postId
        );
    }

    public static BoondiException postAccessDenied() {
        return new BoondiException(
                ErrorCode.POST_ACCESS_DENIED,
                HttpStatus.FORBIDDEN,
                "You are not authorized to modify this post"
        );
    }

    public static BoondiException postEditWindowExpired() {
        return new BoondiException(
                ErrorCode.POST_EDIT_WINDOW_EXPIRED,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Posts can only be edited within 30 minutes of creation"
        );
    }

    public static BoondiException alreadyLiked() {
        return new BoondiException(
                ErrorCode.ALREADY_LIKED,
                HttpStatus.CONFLICT,
                "You have already liked this post"
        );
    }

    public static BoondiException notLiked() {
        return new BoondiException(
                ErrorCode.NOT_LIKED,
                HttpStatus.CONFLICT,
                "You have not liked this post"
        );
    }

    public static BoondiException alreadyReposted() {
        return new BoondiException(
                ErrorCode.ALREADY_REPOSTED,
                HttpStatus.CONFLICT,
                "You have already reposted this post"
        );
    }

    public static BoondiException notReposted() {
        return new BoondiException(
                ErrorCode.NOT_REPOSTED,
                HttpStatus.CONFLICT,
                "You have not reposted this post"
        );
    }

    public static BoondiException alreadyBookmarked() {
        return new BoondiException(
                ErrorCode.ALREADY_BOOKMARKED,
                HttpStatus.CONFLICT,
                "You have already bookmarked this post"
        );
    }

    public static BoondiException notBookmarked() {
        return new BoondiException(
                ErrorCode.NOT_BOOKMARKED,
                HttpStatus.CONFLICT,
                "You have not bookmarked this post"
        );
    }

    public static BoondiException alreadyFollowing() {
        return new BoondiException(
                ErrorCode.ALREADY_FOLLOWING,
                HttpStatus.CONFLICT,
                "You are already following this user"
        );
    }

    public static BoondiException notFollowing() {
        return new BoondiException(
                ErrorCode.NOT_FOLLOWING,
                HttpStatus.CONFLICT,
                "You are not following this user"
        );
    }

    public static BoondiException cannotFollowSelf() {
        return new BoondiException(
                ErrorCode.CANNOT_FOLLOW_SELF,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "You cannot follow yourself"
        );
    }

    public static BoondiException notificationNotFound(String notificationId) {
        return new BoondiException(
                ErrorCode.NOTIFICATION_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "Notification not found: " + notificationId
        );
    }

    public static BoondiException notificationAccessDenied() {
        return new BoondiException(
                ErrorCode.NOTIFICATION_ACCESS_DENIED,
                HttpStatus.FORBIDDEN,
                "You are not authorized to access this notification"
        );
    }

    public static BoondiException invalidReportTarget() {
        return new BoondiException(
                ErrorCode.INVALID_REPORT_TARGET,
                HttpStatus.BAD_REQUEST,
                "Exactly one of reportedUserId or reportedPostId must be provided"
        );
    }

    public static BoondiException cannotReportSelf() {
        return new BoondiException(
                ErrorCode.CANNOT_REPORT_SELF,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "You cannot report yourself"
        );
    }
}
