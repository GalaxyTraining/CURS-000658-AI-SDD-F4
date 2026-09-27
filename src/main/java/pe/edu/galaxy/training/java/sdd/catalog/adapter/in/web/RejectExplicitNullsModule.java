package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.RegisterProductRequest;
import org.springframework.stereotype.Component;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.NopAnnotationIntrospector;
import tools.jackson.databind.module.SimpleModule;

/**
 * ADR-0009: an explicit null in any generated *Request fails deserialization, which is answered as
 * 400 VALIDATION_ERROR. Omitted fields are still allowed.
 */
@Component
public class RejectExplicitNullsModule extends SimpleModule {

    private static final Package REQUEST_PACKAGE = RegisterProductRequest.class.getPackage();

    public RejectExplicitNullsModule() {
        super("reject-explicit-nulls");
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.insertAnnotationIntrospector(new NopAnnotationIntrospector() {
            @Override
            public JsonSetter.Value findSetterInfo(MapperConfig<?> config, Annotated annotated) {
                if (annotated instanceof AnnotatedMember member && isRequest(member.getDeclaringClass())) {
                    return JsonSetter.Value.forValueNulls(Nulls.FAIL);
                }
                return JsonSetter.Value.empty();
            }
        });
    }

    private static boolean isRequest(Class<?> type) {
        return REQUEST_PACKAGE.equals(type.getPackage()) && type.getSimpleName().endsWith("Request");
    }
}
