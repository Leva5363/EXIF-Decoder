import io.vertx.core.Vertx;

import static io.vertx.core.Vertx.*;

public class Main {
     public static void main(String[] args) {
         Vertx vertx = vertx();
         vertx.deployVerticle(new Server());
     }

}

