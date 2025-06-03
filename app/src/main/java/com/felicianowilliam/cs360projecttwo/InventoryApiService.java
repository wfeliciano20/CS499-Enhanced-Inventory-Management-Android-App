package com.felicianowilliam.cs360projecttwo;



import static com.felicianowilliam.cs360projecttwo.Constants.BASE_URL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Service class for interacting with the Inventory Management Web API.
 *
 * This class encapsulates all network operations required to communicate with the
 * backend API for managing inventory items and user authentication. It uses OkHttp
 * for making HTTP requests and handles asynchronous execution of these requests
 * on a dedicated thread pool. Results of API calls, whether successful or erroneous,
 * are delivered back to the caller on the main UI thread via an {@link ApiCallback}
 * interface.
 *
 * Key functionalities include:
 * <ul>
 *   <li>User registration and login.</li>
 *   <li>Refreshing authentication tokens.</li>
 *   <li>CRUD (Create, Read, Update, Delete) operations for inventory items.</li>
 * </ul>
 *
 * Error handling is implemented to parse error messages from API responses and
 * to report network or JSON parsing issues.
 *
 * The service should be shut down when no longer needed (e.g., when the application
 * is closing) to release resources held by the thread pool.
 */
public class InventoryApiService {

    // OkHttpClient instance for making HTTP requests
    private final OkHttpClient client;

    // MediaType constant for JSON content
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Thread pool executor for handling asynchronous network operations
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    // Handler for posting results back to the main/UI thread
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    // Constructor that initializes the service with an OkHttpClient
    public InventoryApiService(Context context) {

        // Gets a configured OkHttpClient instance from ApiClient
        client = ApiClient.getClient(context);
    }

    // Callback interface for handling API responses
    public interface ApiCallback<T> {

        // Called when API call succeeds
        Response onSuccess(T result);

        // Called when API call fails
        void onError(String error, int statusCode); 
    }

    /**
     * Registers a new user with the provided details.
     * This method sends a POST request to the "/register" endpoint of the API.
     *
     * @param name The name of the user to register.
     * @param email The email address of the user to register. This will be used as the login identifier.
     * @param password The password for the new user account.
     * @param callback An {@link ApiCallback} to handle the response from the API.
     *                 The callback will receive an {@link AuthResponse} on success,
     *                 or an error message and status code on failure.
     *                 The callback methods (onSuccess, onError) will be invoked on the main thread.
     */
    public void registerUser(String name, String email, String password, ApiCallback<AuthResponse> callback) {

        // Execute the registration request on a background thread
        executor.execute(() -> {

            try {
            // Create a new JSON object to hold the registration data
            JSONObject jsonBody = new JSONObject();

            // Add the user's name to the JSON object
            jsonBody.put("name", name);

            // Add the user's email to the JSON object
            jsonBody.put("email", email);

            // Add the user's password to the JSON object
            jsonBody.put("password", password);

            // Create a request body with the JSON data
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            // Build the HTTP POST request with the registration endpoint
            Request request = new Request.Builder()
                .url(BASE_URL + "/register")
                .post(body)
                .build();

            // Handle the authentication response
            handleAuthResponse(request, callback);

            } catch (JSONException e) {
            // If JSON creation fails, post the error to the main thread
            mainThreadHandler.post(() -> callback.onError("JSON Exception: " + e.getMessage(), 0));
            }
        });
    }

    /**
     * Logs in a user with the provided email and password.
     *
     * This method asynchronously sends a POST request to the "/login" endpoint of the BASE_URL.
     * The request body contains the user's email and password in JSON format.
     *
     * Upon receiving a response, the {@code handleAuthResponse} method is called to process it.
     * If a JSONException occurs during the creation of the request body, the {@code onError}
     * method of the provided callback is invoked on the main thread with an error message.
     *
     * @param email The email address of the user.
     * @param password The password of the user.
     * @param callback An {@link ApiCallback} to handle the success or failure of the login attempt.
     *                 The callback's {@code onSuccess} method will be invoked with an {@link AuthResponse}
     *                 containing authentication details if the login is successful.
     *                 The callback's {@code onError} method will be invoked with an error message and
     *                 status code if the login fails or an error occurs.
     */
    public void loginUser(String email, String password, ApiCallback<AuthResponse> callback) {

        executor.execute(() -> {

            // Execute the login request on a background thread
            try {

            // Create a new JSON object to hold the login data
            JSONObject jsonBody = new JSONObject();

            // Add the user's email to the JSON object
            jsonBody.put("email", email);

            // Add the user's password to the JSON object
            jsonBody.put("password", password);

            // Create a request body with the JSON data
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            // Build the HTTP POST request with the login endpoint
            Request request = new Request.Builder()
                .url(BASE_URL + "/login")
                .post(body)
                .build();

            // Handle the authentication response
            handleAuthResponse(request, callback);

            } catch (JSONException e) {

            // If JSON creation fails, post the error to the main thread
            mainThreadHandler.post(() -> callback.onError("JSON exception: " + e.getMessage(), 0));
            }
        });
    }


    /**
     * Handles the HTTP response for an authentication request.
     * <p>
     * This method executes the given HTTP {@link Request}, processes the response,
     * and invokes the appropriate callback method ({@code onSuccess} or {@code onError})
     * on the provided {@link ApiCallback}.
     * </p>
     * <p>
     * If the request is successful (HTTP status code 2xx):
     * <ul>
     *     <li>The response body is parsed as a JSON object.</li>
     *     <li>The "token", "refreshToken", and "userName" fields are extracted from the JSON.</li>
     *     <li>An {@link AuthResponse} object is created with these values.</li>
     *     <li>The {@code onSuccess} callback is invoked on the main thread with the {@link AuthResponse}.</li>
     * </ul>
     * </p>
     * <p>
     * If the request is not successful:
     * <ul>
     *     <li>An error message is constructed using the HTTP status code.</li>
     *     <li>If the response body is not empty, it attempts to parse it as JSON to extract
     *         a more specific error message from "message" or "error" fields.</li>
     *     <li>If parsing the error JSON fails or the fields are not present, the raw response body
     *         is appended to the error message.</li>
     *     <li>The {@code onError} callback is invoked on the main thread with the constructed error message
     *         and the HTTP status code.</li>
     * </ul>
     * </p>
     * <p>
     * If an {@link IOException} (e.g., network error) or {@link JSONException} (e.g., malformed JSON
     * in the success path) occurs during the process:
     * <ul>
     *     <li>The {@code onError} callback is invoked on the main thread with a generic
     *         "Network/JSON Exception" message and a status code of 0.</li>
     * </ul>
     * </p>
     * <p>
     * All callback invocations are posted to the {@code mainThreadHandler} to ensure they
     * are executed on the main UI thread.
     * </p>
     *
     */
    private void handleAuthResponse(Request request, ApiCallback<AuthResponse> callback) {

            // Execute the HTTP request and ensure the response is closed after use
        try (Response response = client.newCall(request).execute()) {
            
            // Get the response body as string or empty string if null
            String responseBodyString = response.body() != null ? response.body().string() : "";

            // Check if the response status code is in the 200-299 range
            if (response.isSuccessful()) {

            // Parse the response body as JSON
            JSONObject jsonResponse = new JSONObject(responseBodyString);

            // Extract the authentication token from the response
            String token = jsonResponse.getString("token");

            // Extract the refresh token from the response
            String refreshToken = jsonResponse.getString("refreshToken");

            // Extract the username from the response
            String userName = jsonResponse.getString("userName");

            // Post successful response to main thread with AuthResponse object
            mainThreadHandler.post(() -> callback.onSuccess(new AuthResponse(token, refreshToken,userName)));

            } else {

            // Try to parse error message from API if available
            String errorMessage = "Error " + response.code();

            // Start building error message with status code
            // Check if response body contains any content
            if (!responseBodyString.isEmpty()) {

                try {

                // Attempt to parse error response as JSON
                JSONObject jsonError = new JSONObject(responseBodyString);

                // Append message field if exists
                if (jsonError.has("message")) errorMessage += ": " + jsonError.getString("message");

                // Append error field if exists
                else if (jsonError.has("error")) errorMessage += ": " + jsonError.getString("error");

                // Fallback to raw response body if no standard fields
                else errorMessage += ": " + responseBodyString;

                } catch (JSONException e) {
                // Handle JSON parsing error by using raw response
                errorMessage += ": " + responseBodyString; // Malformed error JSON
                }
            }

            // Make error message final for use in lambda
            final String finalErrorMessage = errorMessage;

            // Post error to main thread with final error message
            mainThreadHandler.post(() -> callback.onError(finalErrorMessage, response.code()));
            }

            // Catch network or JSON parsing exceptions
        } catch (IOException | JSONException e) {

            // Post exception details to main thread with status code 0
            mainThreadHandler.post(() -> callback.onError("Network/JSON Exception: " + e.getMessage(), 0));
        }
    }



    /**
     * Retrieves all inventory items from the server.
     * This method makes an asynchronous GET request to the "/inventory-items" endpoint.
     * It requires an authentication token for authorization will be provided by auth interceptor.
     *
     *
     * @param callback  An {@link ApiCallback} to handle the asynchronous response.
     *                  The callback will be invoked with a {@code List<InventoryItem>} on success,
     *                  or with an error message and status code on failure.
     *                  The callback methods (onSuccess, onError) will be executed on the main thread.
     */
    public void getAllInventoryItems( ApiCallback<List<InventoryItem>> callback) {

        executor.execute(() -> {

            // Create a new request to fetch all inventory items
            Request request = new Request.Builder()
                .url(BASE_URL + "/inventory-items")
                .get()
                .build();

            // Execute the request and handle the response
            try (Response response = client.newCall(request).execute()) {

            // Get the response body as string or empty string if null
            String responseBodyString = response.body() != null ? response.body().string() : "";

            // Check if the response is successful
            if (response.isSuccessful()) {

                // Parse the response body as JSON array
                JSONArray itemArray = new JSONArray(responseBodyString);

                // Create a new list to store inventory items
                List<InventoryItem> items = new ArrayList<>();

                // Iterate through each item in the array
                for (int i = 0; i < itemArray.length(); i++) {

                // Parse each item and add to the list
                items.add(parseInventoryItem(itemArray.getJSONObject(i)));
                }

                // Post the result to the main thread
                mainThreadHandler.post(() -> callback.onSuccess(items));

            } else {

                // Handle error response
                handleApiError(response, responseBodyString, callback);
            }

            } catch (IOException | JSONException e) {

            // Handle network or JSON parsing errors
            mainThreadHandler.post(() -> callback.onError("Network/JSON Exception: " + e.getMessage(), 0));
            Log.e("InventoryApiService", "Error fetching inventory items: " + e.getMessage());
            }
        });
    }


    /**
     * Creates a new inventory item on the server.
     * <p>
     * This method sends an asynchronous POST request to the "/inventory-items" endpoint
     * to create a new inventory item with the specified name and quantity.
     * It requires an authentication token for authorization will be provided by auth interceptor.
     * <p>
     * The result of the operation (success or failure) is communicated back
     * via the provided {@link ApiCallback}.
     *
     * @param name The name of the inventory item to be created.
     * @param quantity The initial quantity of the inventory item.
     *@param callback The callback to be invoked with the result of the API call.
     *                 On success, {@link ApiCallback#onSuccess(Object)} will be called
     *                 with the created {@link InventoryItem}.
     *                 On failure, {@link ApiCallback#onError(String, int)} will be called
     *                 with an error message and the HTTP status code.
     */
    public void createInventoryItem(String name, int quantity, ApiCallback<InventoryItem> callback) {

        // Execute the request on a background thread
        executor.execute(() -> {

            try {
                // Create a new JSON object for the request body
                JSONObject jsonBody = new JSONObject();

                // Add the item name to the JSON object
                jsonBody.put("name", name);

                // Add the item quantity to the JSON object
                jsonBody.put("quantity", quantity);

                // Create a request body with the JSON data
                RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

                // Build the HTTP POST request with the inventory items endpoint
                Request request = new Request.Builder()
                        .url(BASE_URL + "/inventory-items")
                        .post(body)
                        .build();

                // Execute the request and ensure the response is closed after use
                try (Response response = client.newCall(request).execute()) {

                    // Get the response body as string or empty string if null
                    String responseBodyString = response.body() != null ? response.body().string() : "";

                    // Check if the response is successful (expecting HTTP 201 Created)
                    if (response.isSuccessful()) {

                        // Post successful response to main thread
                        mainThreadHandler.post(() -> {

                            try {
                                // Parse the response JSON and invoke success callback
                                callback.onSuccess(parseInventoryItem(new JSONObject(responseBodyString)));

                            } catch (JSONException e) {
                                // Handle JSON parsing error
                                callback.onError("Error parsing created item JSON: " + e.getMessage(), response.code());
                            }
                        });

                    } else {
                        // Handle API error response
                        handleApiError(response, responseBodyString, callback);
                    }
                }

            } catch (IOException | JSONException e) {
                // Handle network or JSON exceptions
                mainThreadHandler.post(() -> callback.onError("Network/JSON Exception: " + e.getMessage(),0));
            }
        });
    }

    /**
     * Updates an existing inventory item on the server.
     * This method sends a PUT request to the server with the updated item details.
     * The operation is performed asynchronously, and the result is delivered via the provided callback.
     * It requires an authentication token for authorization will be provided by auth interceptor.
     *
     * @param itemId The unique identifier of the inventory item to update.
     * @param name The new name for the inventory item.
     * @param quantity The new quantity for the inventory item.
     * @param callback The callback to be invoked when the operation completes, either successfully or with an error.
     *                 On success, {@link ApiCallback#onSuccess(Object)} is called with the updated {@link InventoryItem}.
     *                 On failure, {@link ApiCallback#onError(String, int)} is called with an error message and status code.
     */
    // Updates an existing inventory item on the server asynchronously.
    public void updateInventoryItem(String itemId, String name, int quantity, ApiCallback<InventoryItem> callback) {

        // Execute the update operation on a background thread
        executor.execute(() -> {

            try {

                // Create a new JSON object for the request body
                JSONObject jsonBody = new JSONObject();

                // Add the updated name to the JSON object
                jsonBody.put("name", name);

                // Add the updated quantity to the JSON object
                jsonBody.put("quantity", quantity);

                // Create a request body with the JSON data
                RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

                // Build the HTTP PUT request for updating the inventory item
                Request request = new Request.Builder()
                        .url(BASE_URL + "/inventory-items/" + itemId)
                        .put(body)
                        .build();

                // Execute the request and handle the response
                try (Response response = client.newCall(request).execute()) {

                    // Get the response body as a string, or empty string if null
                    String responseBodyString = response.body() != null ? response.body().string() : "";

                    // Check if the response is successful (typically HTTP 200 for update)
                    if (response.isSuccessful()) {

                        // Post the result to the main thread
                        mainThreadHandler.post(() -> {

                            try {

                                // Parse the updated item from the response and invoke success callback
                                callback.onSuccess(parseInventoryItem(new JSONObject(responseBodyString)));

                            } catch (JSONException e) {

                                // Handle JSON parsing error and invoke error callback
                                callback.onError("Error parsing updated item JSON: " + e.getMessage(), response.code());
                            }
                        });

                    } else {

                        // Handle API error response
                        handleApiError(response, responseBodyString, callback);
                    }
                }

            } catch (IOException | JSONException e) {

                // Handle network or JSON exceptions and invoke error callback
                mainThreadHandler.post(() -> callback.onError("Network/JSON Exception: " + e.getMessage(),0));
            }
        });
    }

    /**
     * Deletes an inventory item from the server.
     *
     * This method sends an asynchronous DELETE request to the /inventory-items/{itemId} endpoint.
     * It requires an authentication token for authorization will be provided by auth interceptor.
     *
     * Upon successful deletion (HTTP 204 No Content), the `onSuccess` callback is invoked
     * with a success message.
     *
     * In case of an API error (non-2xx response), the `handleApiError` method is called,
     * which will eventually invoke the `onError` callback with error details.
     *
     * If a network exception occurs during the request, the `onError` callback is invoked
     * with a "Network Exception" message and a status code of 0.
     *
     * @param itemId The unique identifier of the inventory item to be deleted.
     * @param callback The callback interface to handle the response (success or error).
     *                 - `onSuccess(String message)`: Called when the item is successfully deleted.
     *                 - `onError(String errorMessage, int statusCode)`: Called when an error occurs.
     */
    public void deleteInventoryItem(String itemId, ApiCallback<String> callback) {

        // Execute the delete operation on a background thread
        executor.execute(() -> {

            // Build the DELETE request with the item ID in the URL
            Request request = new Request.Builder()
                .url(BASE_URL + "/inventory-items/" + itemId)
                .delete()
                .build();

            // Execute the request and ensure response is closed
            try (Response response = client.newCall(request).execute()) {

            // Get response body as string or empty if null
            String responseBodyString = response.body() != null ? response.body().string() : "";

            // Check if response indicates success (204 No Content expected)
            if (response.isSuccessful()) {

                // Post success message to main thread
                mainThreadHandler.post(() -> callback.onSuccess("Item deleted successfully"));

            } else {

                // Handle API error response
                handleApiError(response, responseBodyString, callback);
            }

            } catch (IOException e) {

            // Post network error to main thread
            mainThreadHandler.post(() -> callback.onError("Network Exception: " + e.getMessage(), 0));
            }
        });
    }


    /**
     * Parses a JSONObject representing an inventory item into an InventoryItem object.
     *
     * @param itemJson The JSONObject containing the inventory item data.
     *                 It is expected to have the following keys:
     *                 <ul>
     *                     <li>"_id" (String): The unique identifier of the item.</li>
     *                     <li>"name" (String): The name of the item.</li>
     *                     <li>"quantity" (int): The quantity of the item.</li>
     *                     <li>"userId" (String): The ID of the user who owns the item.</li>
     *                     <li>"__v" (int, optional): The version key, defaults to 0 if not present.</li>
     *                 </ul>
     * @return An InventoryItem object populated with data from the JSONObject.
     * @throws JSONException If any of the required keys are missing or if there's an error parsing the JSON.
     */
    private InventoryItem parseInventoryItem(JSONObject itemJson) throws JSONException {

        return new InventoryItem(
                itemJson.getString("_id"),
                itemJson.getString("name"),
                itemJson.getInt("quantity"),
                itemJson.getString("userId"), 
                itemJson.optInt("__v", 0)
        );
    }

    /**
     * Handles API errors by parsing the response body for error messages and invoking the callback's onError method.
     * <p>
     * This method attempts to extract a meaningful error message from the API response.
     * It first checks for a standard JSON error structure with "message" or "error" keys.
     * If these keys are not found or if the response body is not valid JSON,
     * it falls back to using the raw response body as the error message.
     * <p>
     * The error handling and callback invocation are performed on the main thread
     * using the {@code mainThreadHandler}.
     *
     * @param <T>                The type of the expected successful response (not directly used in error handling but part of the ApiCallback signature).
     * @param response           The Retrofit {@link Response} object representing the HTTP response.
     * @param responseBodyString The string representation of the HTTP response body.
     * @param callback           The {@link ApiCallback} to be notified of the error.
     */
    private <T> void handleApiError(Response response, String responseBodyString, ApiCallback<T> callback) {

        // Initialize error message with the HTTP status code
        String errorMessage = "Error " + response.code();

        // Check if the response body is not empty
        if (!responseBodyString.isEmpty()) {

            try {

                // Attempt to parse the response body as JSON
                JSONObject jsonError = new JSONObject(responseBodyString);

                // Check if the JSON contains a "message" field
                if (jsonError.has("message")) {
                    // Append the message to the error message
                    errorMessage += ": " + jsonError.getString("message");
                }

                // If no "message" field, check for "error" field
                else if (jsonError.has("error")) {
                    // Append the error to the error message
                    errorMessage += ": " + jsonError.getString("error");
                }

                // If neither field exists, use the raw response body
                else {
                    errorMessage += ": " + responseBodyString;
                }

            } catch (JSONException e) {
                // If JSON parsing fails, append the raw response body
                errorMessage += ": " + responseBodyString;
            }
        }

        // Finalize the error message
        final String finalErrorMessage = errorMessage;

        // Post the error to the main thread via the callback
        mainThreadHandler.post(() -> callback.onError(finalErrorMessage, response.code()));
    }

}