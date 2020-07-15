package br.slamine.webflux.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class CustomAttribute extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = getErrorAttributes(webRequest, options);
        Throwable throwable = getError(webRequest);
        if(throwable instanceof ResponseStatusException){
            ResponseStatusException ex = (ResponseStatusException) throwable;
            errorAttributes.put("message", ex.getMessage());
            errorAttributes.put("developerMessage", "A ResponseStatusException happened");
        }
        return errorAttributes;
    }
}
