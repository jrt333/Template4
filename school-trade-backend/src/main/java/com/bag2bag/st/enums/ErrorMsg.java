package com.bag2bag.st.enums;


/**
 * 错误信息美剧
 *
 * @author: ShanZhu
 * @date: 2024-01-05
 */
public enum ErrorMsg {

    ACCOUNT_EXIT("User already exists."),
    ACCOUNT_Ban("Account has been banned."),
    ACCOUNT_NOT_EXIT("User does not exist."),
    PASSWORD_IS_NOT_SAME("Passwords do not match."),
    PASSWORD_RESET_ERROR("Failed to change password."),
    EMAIL_SEND_ERROR("Failed to send email. Please try again."),
    PARAM_ERROR("Invalid parameter(s)."),
    SYSTEM_ERROR("System error."),
    REGISTER_ERROR("Registration failed."),
    FILE_TYPE_ERROR("Invalid file type. Please choose .jpg or .png."),
    FILE_UPLOAD_ERROR("File upload failed."),
    FILE_NOT_EXIT("File does not exist.” / “File not found."),
    FILE_DOWNLOAD_ERROR("File download error."),
    FILE_SIZE_ERROR("File is too large."),
    OPERAT_FREQUENCY("Too many requests. Please try again later."),
    MISSING_PARAMETER("Missing parameter(s)."),
    COOKIE_ERROR("Please log in again."),
    EMAIL_LOGIN_ERROR("Login failed. Incorrect account or password."),
    CODE_ERROR("Verification code is incorrect or has expired."),
    JSON_READ_ERROR("Error parsing JSON parameters."),
    FORM_NUMBER_ERROR("Invalid form ID."),
    REPEAT_COMMIT_ERROR("Please do not submit repeatedly."),
    COMMIT_FAIL_ERROR("Submission failed."),
    FAVORITE_EXIT("Favorite already exists."),
    RESPOND_CONFLICT_OR_NO_PERMISSION("RESPOND_CONFLICT_OR_NO_PERMISSION"),
    CONFIRM_CONFLICT_OR_NO_PERMISSION("CONFIRM_CONFLICT_OR_NO_PERMISSION"),
    CREATE_TRADE_FAIL("CREATE_TRADE_FAIL"),
    GOOGLE_AUTH_ERROR("Google authentication failed. Please try again."),
    INVALID_EMAIL_DOMAIN("Only @aucklanduni.ac.nz accounts can sign in."),
    Fail_is_empty("Fail is empty"),
    Please_upload_image_only("Please upload image only"),
    your_image_too_large("your image too large"),
    Upload_fail("Upload_fail"),
    Try_Again("Try Again");
    private String msg;

    ErrorMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
