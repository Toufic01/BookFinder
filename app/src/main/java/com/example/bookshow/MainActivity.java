package com.example.bookshow;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    EditText bookNameEditText, authorNameEditText, isbnEditText;
    Button searchButton;
    TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        bookNameEditText = findViewById(R.id.bookNameEditText);
        authorNameEditText = findViewById(R.id.authorNameEditText);
        isbnEditText = findViewById(R.id.isbnEditText);
        searchButton = findViewById(R.id.searchButton);
        resultTextView = findViewById(R.id.resultTextView);

        // Set a click listener for the search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBooks();
            }
        });
    }

    private void searchBooks() {
        // Get user input
        String bookName = bookNameEditText.getText().toString();
        String authorName = authorNameEditText.getText().toString();
        String isbn = isbnEditText.getText().toString();

        // Construct the query based on user input
        StringBuilder queryBuilder = new StringBuilder("q=");
        if (!bookName.isEmpty()) {
            queryBuilder.append("intitle:").append(bookName).append("&");
        }
        if (!authorName.isEmpty()) {
            queryBuilder.append("inauthor:").append(authorName).append("&");
        }
        if (!isbn.isEmpty()) {
            queryBuilder.append("isbn:").append(isbn);
        }

        String query = queryBuilder.toString();

        // Make the API request using OkHttp
        OkHttpClient client = new OkHttpClient();
        String apiUrl = "https://www.googleapis.com/books/v1/volumes?" + query;
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle network failure (same as before)
                showError("Error fetching book details. Please check your internet connection.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        if (jsonResponse.has("items")) {
                            JSONArray items = jsonResponse.getJSONArray("items");
                            if (items.length() > 0) {
                                JSONObject book = items.getJSONObject(0);
                                JSONObject volumeInfo = book.optJSONObject("volumeInfo");
                                if (volumeInfo != null) {
                                    String title = volumeInfo.optString("title", "Title not available");
                                    JSONArray authorsArray = volumeInfo.optJSONArray("authors");
                                    String author = (authorsArray != null && authorsArray.length() > 0)
                                            ? authorsArray.optString(0, "Author not available")
                                            : "Author not available";
                                    JSONArray identifiersArray = volumeInfo.optJSONArray("industryIdentifiers");
                                    String isbn = (identifiersArray != null && identifiersArray.length() > 0)
                                            ? identifiersArray.getJSONObject(0).optString("identifier", "ISBN not available")
                                            : "ISBN not available";
                                    String description = volumeInfo.optString("description", "Description not available");

                                    // Fetch book reviews
                                    String review = getReviewFromVolumeInfo(volumeInfo);

                                    // Display book details in the UI, including the review
                                    showBookDetails(title, author, isbn, description, review);
                                } else {
                                    showError("Book details not found.");
                                }
                            } else {
                                showError("Book not found.");
                            }
                        } else {
                            showError("Book details not found.");
                        }
                    } catch (JSONException e) {
                        showError("Error parsing JSON response.");
                    }
                } else {
                    // Handle non-successful response (same as before)
                    showError("Error fetching book details. Please check your internet connection.");
                }
            }
        });
    }

    // Helper method to get the review from volumeInfo or provide a default message
    private String getReviewFromVolumeInfo(JSONObject volumeInfo) {
        if (volumeInfo != null && volumeInfo.has("reviews")) {
            // Assuming the review is stored in a "reviews" field in volumeInfo
            String review = volumeInfo.optString("reviews");
            if (!review.isEmpty()) {
                return review;
            }
        }
        // If no review is found or the review is empty, return a default message
        return "No review available for this book.";
    }

    // Helper method to display an error message in the UI
    private void showError(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText(errorMessage);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to display book details in the UI
    private void showBookDetails(final String title, final String author, final String isbn, final String description, final String review) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Construct the formatted book details with HTML formatting
                String formattedDetails = "<b>Title:</b> " + title + "<br/>" +
                        "<b>Author:</b> " + author + "<br/>" +
                        "<b>ISBN:</b> " + isbn + "<br/>" +
                        "<b>Description:</b> " + description + "<br/>" +
                        "<b>Review:</b> " + review;

                // Set the formatted text in the TextView with HTML formatting
                resultTextView.setText(Html.fromHtml(formattedDetails));

                // Enable TextView to render HTML
                resultTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        });
    }
}
