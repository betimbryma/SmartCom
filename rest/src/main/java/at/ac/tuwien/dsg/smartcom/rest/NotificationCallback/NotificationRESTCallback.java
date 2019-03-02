package at.ac.tuwien.dsg.smartcom.rest.NotificationCallback;

import at.ac.tuwien.dsg.smartcom.callback.NotificationCallback;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.rest.model.MessageDTO;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestOperations;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.time.Duration;

public class NotificationRESTCallback implements NotificationCallback {

    private final String url;

    //private WebClient webClient;

    public NotificationRESTCallback(String url) {
        this.url = url;

        //this.webClient = WebClient.builder().baseUrl(url)
        //        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }

    public static void main(String[] args){
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        RestOperations restOperations = restTemplateBuilder.setReadTimeout(Duration.ofSeconds(10))
                .setConnectTimeout(Duration.ofSeconds(10)).build();
        restOperations.postForEntity("http://localhost:9080/api/piglet/test", new MessageDTO(), String.class);

    }

    @Override
    public void notify(final Message message) {
 /*
        webClient.post().uri("/piglet/test").syncBody(new MessageDTO(message))
                .retrieve().bodyToMono(Void.class);


        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    WebTarget target = client.target(url);

                    Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(new MessageDTO(message)), Response.class);

                    if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                        //log.error("Could not send message {} to notification callback \nResponse: {}", message, response);
                    }
                } catch (Exception ignored) {
                    //log.debug("Could not notify rest callback", ignored);
                }
            }
        }); */
    }

}