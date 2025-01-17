package networking.response;

public abstract class Response<T> {
    protected T response;
    protected boolean isPending = true;

    public Response() {}

    public Response(T response) {
        this.response = response;
    }

    public abstract T getData();
}
