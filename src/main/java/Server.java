import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;

public class Server extends AbstractVerticle {
    @Override
    public void start(Future<Void> future) throws Exception {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads").setDeleteUploadedFilesOnEnd(true));
        router.route("/").handler(routingContext -> {
            routingContext.response().putHeader("content-type", "text/html").end(
                    "<form action=\"/form\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                            "    <div>\n" +
                            "        <label for=\"name\">Select a file:</label>\n" +
                            "        <input type=\"file\" name=\"file\" />\n" +
                            "    </div>\n" +
                            "    <div class=\"button\">\n" +
                            "        <button type=\"submit\">Send</button>\n" +
                            "    </div>" +
                            "</form>"
            );
        });

        // handle the form
        router.post("/form").handler(ctx -> {
            ctx.response().putHeader("Content-Type", "text/plain");

            ctx.response().setChunked(true);

            for (FileUpload f : ctx.fileUploads()) {
                ctx.response().write("Filename: " + f.fileName());
                ctx.response().write("\n");
                ctx.response().write("Size: " + f.size());
                ctx.response().write("\n");
                ctx.response().write("***********");
                ctx.response().write("\n");

            }


            ctx.response().end();
        });

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

}


