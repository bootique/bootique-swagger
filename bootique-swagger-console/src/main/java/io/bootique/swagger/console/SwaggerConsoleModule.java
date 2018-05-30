package io.bootique.swagger.console;

import com.google.inject.Binder;
import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.jetty.JettyModule;

import javax.validation.constraints.NotNull;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Sins 0.26
 */
public class SwaggerConsoleModule extends ConfigModule {

    public static final String RESOURCE_BASE = "bq.jetty.servlets.swagger-ui.params.resourceBase";

    @Override
    public void configure(@NotNull final Binder binder) {
        loadConsole(binder);
        JettyModule.extend(binder).addStaticServlet("swagger-ui", "/swagger-ui/*");

    }

    /**
     * Loads the swagger-ui console files.
     *
     * @param binder the guice binder
     */
    private void loadConsole(@NotNull final Binder binder) {
        final File currentJavaJarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        String prefix = "";
        try {
            prefix = getPrefix(currentJavaJarFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String postfix = "/";
        if(!prefix.isEmpty()) {
            postfix = "!" + postfix;
        }

        BQCoreModule.extend(binder).setProperty(RESOURCE_BASE, prefix + currentJavaJarFile.getPath() + postfix);

    }

    private String getPrefix(@NotNull final File file) throws IOException {
        if(file.isDirectory()) {
            return "";
        }
        if(!file.canRead()) {
            throw new IOException("Cannot read file " + file.getAbsolutePath());
        }
        if(file.length() < 4) {
            return "";
        }
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        int test = in.readInt();
        in.close();
        if(test == 0x504b0304) {
            return "jar:file:";
        }

        return "file:";
    }
}
