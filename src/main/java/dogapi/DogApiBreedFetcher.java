package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException{
        // TODO Task 1: Complete this method based on its provided documentation
        //      and the documentation for the dog.ceo API. You may find it helpful
        //      to refer to the examples of using OkHttpClient from the last lab,
        //      as well as the code for parsing JSON responses.
        // return statement included so that the starter code can compile and run.

        if (breed == null || breed.trim().isEmpty()) {
            throw new BreedFetcher.BreedNotFoundException("Breed not found (empty)");
        }
        String key = breed.trim().toLowerCase(Locale.ROOT);

        String url = "https://dog.ceo/api/breed/" + key + "/list";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "DogBreedFetcher/1.0")
                .build();

        String body;
        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                throw new BreedFetcher.BreedNotFoundException("No response body from API");
            }
            body = response.body().string();
        } catch (IOException ioe) {
            throw new BreedFetcher.BreedNotFoundException("Network error calling Dog CEO API: " + ioe.getMessage());
        }

        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (Exception parseEx) {
            throw new BreedFetcher.BreedNotFoundException("Invalid JSON from API");
        }

        String status = json.optString("status", "");

        if ("error".equalsIgnoreCase(status)) {
            String message = json.optString("message", "Breed not found (main breed does not exist)");
            throw new BreedFetcher.BreedNotFoundException(message);
        }

        if ("success".equalsIgnoreCase(status)) {
            JSONArray arr = json.optJSONArray("message");
            if (arr == null) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) {
                String sub = arr.optString(i, "").trim();
                if (!sub.isEmpty()) {
                    result.add(sub);
                }
            }
            return Collections.unmodifiableList(result);
        }
        throw new BreedFetcher.BreedNotFoundException("Unexpected API status: " + status);
    }
}