package response;

public abstract class Response {
    String response;

    public Response(String response) {
        this.response = response;
    }

    public abstract String getData();
}
