package br.slamine.webflux.exception;

import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

import static org.springframework.boot.web.error.ErrorAttributeOptions.*;

@Component//GlocalExceptionHandler need to be spring "Bean" so spring container can Inject it
@Order(-2)//Add priority to GlobalExceptionHandler opposite to AbstractErrorWebExceptionHandler which is defined to priority -1
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(
            ErrorAttributes errorAttributes,
            ResourceProperties resourceProperties,
            ApplicationContext applicationContext,
            ServerCodecConfigurer codecConfigurer) {
        super(errorAttributes, resourceProperties, applicationContext);
        this.setMessageWriters(codecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::formatErrorResponse);//Apply the exception handler to all request regardless the protocol used
    }

    private Mono<ServerResponse> formatErrorResponse(ServerRequest request){
        String query = request.uri().getQuery();
        ErrorAttributeOptions errorAttributeOptions = isTraceEnabled(query) ? of(Include.STACK_TRACE) : defaults();

        Map<String, Object> errorAttributesMap = getErrorAttributes(request, errorAttributeOptions);
        int status = (int) Optional.of(errorAttributesMap.get("status")).orElse(500);

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorAttributesMap));
    }

    private boolean isTraceEnabled(String query){
        return !StringUtils.isEmpty(query) && query.contains("trace=true");
    }
}
