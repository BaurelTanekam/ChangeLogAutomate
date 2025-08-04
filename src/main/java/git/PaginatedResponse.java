package git;

import org.json.JSONArray;

public class PaginatedResponse {
    private JSONArray data;
    private String nextUrl;

    public PaginatedResponse(JSONArray data, String nextUrl){
        this.data = data;
        this.nextUrl = nextUrl;
    }

    public JSONArray getData() {
        return data;
    }

    public String getNextUrl() {
        return nextUrl;
    }
}
