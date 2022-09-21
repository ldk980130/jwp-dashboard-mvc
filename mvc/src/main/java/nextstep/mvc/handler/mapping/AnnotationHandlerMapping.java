package nextstep.mvc.handler.mapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import nextstep.mvc.handler.HandlerExecution;
import nextstep.mvc.handler.HandlerKey;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackages;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final Object... basePackages) {
        this.basePackages = basePackages;
        this.handlerExecutions = new HashMap<>();
    }

    @Override
    public void initialize() {
        scanHandlersInBasePackages();
        log.info("Initialized AnnotationHandlerMapping!");
    }

    private void scanHandlersInBasePackages() {
        Reflections reflections = new Reflections(basePackages);
        reflections.getTypesAnnotatedWith(Controller.class)
            .forEach(this::scanHandlersInController);
    }

    private void scanHandlersInController(Class<?> controller) {
        for (Method method : controller.getMethods()) {
            Optional.ofNullable(method.getAnnotation(RequestMapping.class))
                .ifPresent(registerHandler(method));
        }
    }

    private Consumer<RequestMapping> registerHandler(Method method) {
        return requestMapping -> addHandlerExecution(requestMapping, new HandlerExecution(method));
    }

    private void addHandlerExecution(RequestMapping requestMapping, HandlerExecution execution) {
        String url = requestMapping.value();
        for (RequestMethod requestMethod : requestMapping.method()) {
            handlerExecutions.put(new HandlerKey(url, requestMethod), execution);
        }
    }

    @Override
    public Optional<Object> getHandler(final HttpServletRequest request) {
        return Optional.ofNullable(handlerExecutions.get(HandlerKey.fromRequest(request)));
    }
}
