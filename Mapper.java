Error uvError =
    objectMapper.readValue(
        ex.getResponseBodyAsString(),
        Error.class);

throw new UVCustomerException(
        uvError,
        HttpStatus.NOT_FOUND);


@Getter
public class UVCustomerException extends RuntimeException {

    private final Error error;
    private final HttpStatus httpStatus;

    public UVCustomerException(
            Error error,
            HttpStatus httpStatus) {

        super(error.getDetail());
        this.error = error;
        this.httpStatus = httpStatus;
    }
}

@ExceptionHandler(UVCustomerException.class)
public ResponseEntity<List<Error>> handleUVCustomerException(
        UVCustomerException e) {

    return new ResponseEntity<>(
            Collections.singletonList(e.getError()),
            e.getHttpStatus());
}