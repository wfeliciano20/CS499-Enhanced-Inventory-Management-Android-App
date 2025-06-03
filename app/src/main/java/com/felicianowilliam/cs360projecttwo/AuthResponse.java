package com.felicianowilliam.cs360projecttwo;

import androidx.annotation.NonNull;

/**
 * Represents the response from an authentication request.
 * This class encapsulates the authentication token, refresh token, and username.
 */
public class AuthResponse{

    private String token;

    private String refreshToken;

    private String userName;


    /**
     * Constructs a new AuthResponse object.
     *
     * @param token The authentication token.
     * @param refreshToken The refresh token.
     * @param userName The username associated with the authentication.
     */
    public AuthResponse(String token, String refreshToken, String userName) {

        this.token = token;

        this.refreshToken = refreshToken;

        this.userName = userName;
    }

    /**
     * Retrieves the authentication token.
     *
     * This method returns the current authentication token associated with the object.
     * The token is typically used for authorizing requests to a service or API.
     *
     * @return The authentication token as a String. Returns null if no token is set.
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the authentication token.
     *
     * This method is used to assign a new authentication token to the current object.
     * The token is typically used for authorizing requests or identifying a user session.
     *
     * @param token The new authentication token as a String. It should not be null,
     *              though specific validation might depend on the application's requirements.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Returns the refresh token associated with the authentication.
     *
     * <p>A refresh token is a credential used to obtain a new access token
     * when the current access token expires. This allows the application to
     * maintain an authenticated session without requiring the user to
     * re-enter their credentials.</p>
     *
     * @return The refresh token as a String, or {@code null} if no refresh token
     *         is available for this authentication.
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Sets the refresh token.
     *
     * <p>A refresh token is a special kind of token that can be used to obtain a new access token.
     * Refresh tokens are typically long-lived and are used to maintain a user's session without
     * requiring them to re-authenticate frequently.
     *
     * <p>This method is typically used when:
     * <ul>
     *     <li>Initializing an authentication response object after a successful authentication.</li>
     *     <li>Updating the refresh token if a new one is issued (e.g., during a token refresh operation).</li>
     * </ul>
     *
     * @param refreshToken The refresh token string. This value should not be null or empty if a refresh token
     *                     is expected. It's important to handle this token securely as it allows for
     *                     obtaining new access tokens.
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

      /**
       * Returns the username associated with this object.
       *
       * @return The username as a String.
       */
      public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name for this user.
     *
     * @param userName The new user name.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns a string representation of the {@code AuthResponse} object.
     * This string includes the values of the token, refreshToken, and userName fields.
     *
     * @return A string representation of this object, useful for logging and debugging.
     */
    @NonNull
    @Override
    public String toString() {

        return "AuthResponse{" +
                "token='" + token + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}