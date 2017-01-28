package dataengine.api;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-28T14:58:05.392Z")
public class NotFoundException extends ApiException {
    private int code;
    public NotFoundException (int code, String msg) {
        super(code, msg);
        this.code = code;
    }
}
