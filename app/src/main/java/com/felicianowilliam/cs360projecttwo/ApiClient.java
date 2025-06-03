package com.felicianowilliam.cs360projecttwo;

import android.content.Context;
import android.util.Log;
import static com.felicianowilliam.cs360projecttwo.Constants.BASE_URL;
import androidx.annotation.NonNull;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * A utility class for creating and managing an OkHttpClient instance.
 *
 * This class provides a singleton instance of OkHttpClient configured with a logging interceptor.
 * The logging interceptor logs request and response bodies, which is useful for debugging.
 */
public class ApiClient {

    private static OkHttpClient client;

    /**
     * Returns a synchronized OkHttpClient instance.
     * <p>
     * This method ensures that only one instance of OkHttpClient is created and used throughout the application (Singleton pattern).
     * The client is configured with two interceptors:
     * <ul>
     *     <li><b>Logging Interceptor:</b> Logs HTTP request and response bodies. This is useful for debugging network calls.</li>
     *     <li><b>Authentication Interceptor:</b>
     *         <ul>
     *             <li>Adds an "Authorization" header with a Bearer token to requests made to the "/inventory-items" endpoint.
     *             The token is retrieved from {@link SessionManager}.</li>
     *             <li>If a request to "/inventory-items" results in a 401 (Unauthorized) response, it attempts to refresh the access token
     *             using the refresh token stored in {@link SessionManager}.</li>
     *             <li>If the token refresh is successful, the original request is retried with the new access token.</li>
     *             <li>If the token refresh fails or no refresh token is available, the user is logged out via {@link SessionManager},
     *             and the original 401 response is returned.</li>
     *             <li>Requests to endpoints other than "/inventory-items" are passed through without authentication logic.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param context The application context, used to initialize {@link SessionManager} and potentially for other context-dependent operations within interceptors.
     * @return The singleton OkHttpClient instance.
     */
    public static synchronized OkHttpClient getClient(Context context) {

        if (client == null) {

            // Create a logging interceptor to log HTTP request and response bodies.
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create an authentication interceptor to add the access token to requests and handle token refresh.
            Interceptor authInterceptor = new Interceptor() {

                @NonNull
                @Override
                public Response intercept(Chain chain) throws IOException {

                    // grab the original request from the chain.
                    Request originalRequest = chain.request();

                    Request.Builder builder = originalRequest.newBuilder();

                    // Only apply auth logic for requests to the inventory items endpoint
                    if (originalRequest.url().encodedPath().contains("/inventory-items")) {

                        // Get the SessionManager to access the current token.
                        SessionManager sessionManager = new SessionManager(context);

                        String accessToken = sessionManager.getToken();

                        // If an access token exists, add it to the Authorization header.
                        if (accessToken != null) {

                            builder.header("Authorization", "Bearer " + accessToken);
                        }

                        Request newRequest = builder.build();

                        // Proceed with the request.
                        Response response = chain.proceed(newRequest);

                        // Check if the response code is 401 (Unauthorized), indicating the access token might be expired.
                        if (response.code() == 401) {

                            String refreshToken = sessionManager.getRefreshToken();

                            // If a refresh token exists, attempt to refresh the access token.
                            if (refreshToken != null) {

                                response.close(); // Close the old response

                                // Perform a synchronous call to refresh the token.
                                AuthResponse authResponse = refreshTokenSynchronously(context, refreshToken);

                                // If the token refresh was successful and a new token is received.
                                if (authResponse != null && authResponse.getToken() != null) {

                                    // Save the new access token.
                                    sessionManager.setToken(authResponse.getToken());

                                    // If a new refresh token is also received, save it.
                                    if (authResponse.getRefreshToken() != null) {

                                        sessionManager.setRefreshToken(authResponse.getRefreshToken());
                                    }

                                    Log.d("API CLIENT", "Token refreshed successfully.");

                                    // Create a new request with the new access token.
                                    Request newAuthenticatedRequest = originalRequest.newBuilder()
                                            .header("Authorization", "Bearer " + authResponse.getToken())
                                            .build();

                                    // Retry the request with the new authenticated request.
                                    return chain.proceed(newAuthenticatedRequest);
                                } else {

                                    Log.e("API CLIENT", "Failed to refresh token or new token is null.");

                                    // If token refresh fails, log out the user.
                                    sessionManager.logoutUser();

                                    // Return the original 401 response.
                                    return response;
                                }
                            } else {

                                Log.e("API CLIENT", "Refresh token is null, cannot refresh.");

                                // Return the original 401 response.
                                return response;
                            }
                        }

                        // If the response is not 401, return the response as is.
                        return response;
                    }

                    // For non-inventory items calls, proceed without auth logic
                    return chain.proceed(originalRequest);
                }
            };

            // Build the OkHttpClient with both the authentication and logging interceptors.
            client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .build();
        }
        return client;
    }

    /**
     * Synchronously refreshes the access token using the provided refresh token.
     *
     * This method makes a blocking network request to the token refresh endpoint.
     * It should not be called on the main UI thread.
     *
     * @param context The application context, used for accessing SessionManager.
     * @param refreshTokenValue The refresh token string to be used for obtaining a new access token.
     * @return An {@link AuthResponse} object containing the new access and refresh tokens if the refresh is successful,
     *         otherwise returns {@code null} if the refresh fails or an error occurs.
     * @throws IOException If a network error occurs during the token refresh process.
     */
    private static AuthResponse refreshTokenSynchronously(Context context, String refreshTokenValue) {

        // Create a new SessionManager instance using the provided context
        SessionManager sessionManager = new SessionManager(context);

        // Create a new OkHttpClient for synchronous token refresh request
        OkHttpClient syncClient = new OkHttpClient();

        // Build the refresh token request with the refresh token value
        Request refreshTokenRequest = new Request.Builder()
            // Set the refresh token endpoint URL
            .url(BASE_URL + "/refresh-token")
            // Create a POST request with the refresh token in JSON format
            .post(okhttp3.RequestBody.create("{\"refreshToken\":\"" + refreshTokenValue + "\"}", MediaType.parse("application/json")))
            // Build the final request
            .build();

        // Execute the refresh token request synchronously
        try (Response refreshResponse = syncClient.newCall(refreshTokenRequest).execute()) {

            // Check if the response was successful and has a body
            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {

            // Parse the response body into an AuthResponse object
            return parseAuthResponse(refreshResponse.body().string());

            }
            
        } catch (Exception e) {

            // Log any errors that occur during token refresh
            Log.e("API CLIENT", "Error refreshing token", e);

        }

        // Return null if token refresh fails
        return null;

    }

    /**
     * Parses a JSON string to create an AuthResponse object.
     *
     * This method attempts to extract "token", "refreshToken", and "userName"
     * from the provided JSON string. If any of these fields are missing,
     * their corresponding values in the AuthResponse object will be null.
     *
     * @param jsonString The JSON string representing the authentication response.
     * @return An AuthResponse object populated with data from the JSON string,
     *         or null if a JSONException occurs during parsing.
     */
    private static AuthResponse parseAuthResponse(String jsonString) {

        // Try to parse the JSON string
        try {

            // Create a JSONObject from the input string
            JSONObject jsonObject = new JSONObject(jsonString);

            // Extract the token field from JSON, defaulting to null if not present
            String token = jsonObject.optString("token", null);

            // Extract the refreshToken field from JSON, defaulting to null if not present
            String refreshToken = jsonObject.optString("refreshToken", null);

            // Extract the userName field from JSON, defaulting to null if not present
            String userNAme = jsonObject.optString("userName", null);

            // Create and return a new AuthResponse with the extracted values
            return new AuthResponse(token, refreshToken, userNAme);

        // Catch JSON parsing exceptions
        } catch (JSONException e) {

            // Log any JSON parsing errors
            Log.e("ApiClient", "Error parsing AuthResponse from JSON", e);

            // Return null if parsing fails
            return null;
        }

    }

}