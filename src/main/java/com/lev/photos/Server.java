package com.lev.photos;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.file.FileSystemDirectory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

public class Server extends AbstractVerticle {

  @Override
  public void start(Future<Void> future) {
    Router router = Router.router(vertx);

    router.route().handler(CorsHandler.create("*")
      .allowedHeader("Content-Type")
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.POST)
      .allowedMethod(HttpMethod.OPTIONS)
      .allowedHeader("Access-Control-Request-Method")
      .allowedHeader("Access-Control-Allow-Credentials")
      .allowedHeader("Access-Control-Allow-Origin")
      .allowedHeader("Access-Control-Allow-Headers")
      .allowedHeader("Content-Type")
    );
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
    router.post("/form").handler(this::handleParsePhoto);
    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }

  private void handleParsePhoto(RoutingContext ctx) {
    Set<FileUpload> uploads = ctx.fileUploads();
    if (uploads.isEmpty()){
      ctx.response().end(Json.encode("No file to process"));
    } else {
      for (FileUpload f : uploads) {
        try {
          Metadata metadata = ImageMetadataReader.readMetadata(new File(f.uploadedFileName()));
          System.out.println(metadata.getDirectories());
          tagsValues(metadata);
          if (metadata.containsDirectoryOfType(ExifSubIFDDirectory.class)) {
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            ctx.response().end(Json.encode(chekingEvening(date)));
          }
          else if (metadata.containsDirectoryOfType(FileSystemDirectory.class)){
            FileSystemDirectory directory = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
            Date date = directory.getDate(FileSystemDirectory.TAG_FILE_MODIFIED_DATE);
            ctx.response().end(Json.encode(chekingEvening(date)));
          }
          else {
            ctx.response().end(Json.encode("The photo has wrong format, impossible to process"));
          }

        } catch (IOException | ImageProcessingException e) {
          ctx.response().end(Json.encode("Unknown error!"));

        }
      }
    }

  }

  private String chekingEvening(Date date) {
    if (date.getHours() > 18 && date.getHours() < 24) {
      return "The photo was taken in the evening";
    } else {
      return "The photo was taken NOT in the evening";
    }
  }

  private void tagsValues(Metadata metadata) {
    for (Directory directory : metadata.getDirectories()) {
      System.out.println(directory);
      String directoryName = directory.getName();
      // Write the directory's tags
      for (Tag tag : directory.getTags()) {
        System.out.println(tag.getTagName() + "=====" + tag.getDescription());
      }
    }
  }
}
