package pucmm.application.utils;

import io.javalin.http.Context;
import io.javalin.rendering.FileRenderer;
import org.jetbrains.annotations.NotNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.HashMap;
import java.util.Map;

public class ThymeleafConfig implements FileRenderer {

    private final TemplateEngine engine;

    public ThymeleafConfig() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        this.engine = new TemplateEngine();
        this.engine.setTemplateResolver(resolver);
    }

    @NotNull
    @Override
    public String render(@NotNull String filePath, @NotNull Map<String, ?> model, @NotNull Context ctx) {
        org.thymeleaf.context.Context thymeleafCtx = new org.thymeleaf.context.Context();
        thymeleafCtx.setVariables(new HashMap<>(model));
        // Remove .html suffix if present since resolver adds it
        String template = filePath.endsWith(".html") ? filePath.substring(0, filePath.length() - 5) : filePath;
        return engine.process(template, thymeleafCtx);
    }
}
