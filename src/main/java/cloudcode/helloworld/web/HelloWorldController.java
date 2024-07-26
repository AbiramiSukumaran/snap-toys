package cloudcode.helloworld.web;
/*
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.RestController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
*/

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import java.util.HashMap;
import java.util.ArrayList;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.generativeai.preview.ContentMaker;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.PartMaker;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import java.io.*;
import java.nio.file.Paths;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import jakarta.xml.bind.DatatypeConverter;


/** Defines a controller to handle HTTP requests */
@RestController
public class HelloWorldController {

  private static final Logger logger = Logger.getLogger(HelloWorldController.class.getName());
  public static final String PROJECT_ID = "YOUR_PROJECT_ID";
  public static final String LOCATION = "us-central1";

  /**
   * Create an endpoint for the landing page
   *
   */
  @GetMapping("/")
    public ModelAndView home(ModelMap map, Prompt prompt) throws Exception{
      map.addAttribute("response", "");
      map.addAttribute("description", "");
      return new ModelAndView("index", map);
    }
    

  @PostMapping("/describepic")
  public ModelAndView descPic(ModelMap map, Prompt prompt) throws Exception {
    String response = prompt.getResponse();
    String description = "";
    System.out.println("RESPONSE: " + response);
    if(!response.equals("")){
      response = response.replace("data:image/jpeg;base64,", "");
      // write java code to convert the base64 image string "response" to image blob file and upload it to cloud storage bucket
      String base64Image = response;
      byte[] decodedImage = DatatypeConverter.parseBase64Binary(base64Image);
      /* InputStream inputStream = new ByteArrayInputStream(decodedImage);
      File file = new File("image.jpg");
      OutputStream outputStream = new FileOutputStream(file);
      IOUtils.copy(inputStream, outputStream);
      inputStream.close();
      outputStream.close(); */

      description = validate(decodedImage);
      System.out.println(description);
    }

    map.addAttribute("description", description);
    return new ModelAndView("index", map);
  }

   /* Method that is invoked when the user clicks the describe picture button.
        */
        public String validate(byte[] baseline_url) throws IOException{
          String res = "";
            try (VertexAI vertexAi = new VertexAI(PROJECT_ID, LOCATION); ) {
              GenerationConfig generationConfig =
                  GenerationConfig.newBuilder()
                      .setMaxOutputTokens(2048)
                      .setTemperature(0.4F)
                      .setTopK(32)
                      .setTopP(1)
                      .build();
                      
            GenerativeModel model = new GenerativeModel("gemini-pro-vision", generationConfig, vertexAi);
            String context = 
            "The attached image is a picture of a toy in the foreground. The image may contain other details in the background and also a hand holding the toy. Ignore the hand and other background details of the image and only describe the toy that should be the main front focus of the image. If the image does not show a toy or a toy-like object, please ask the user to stick to the context of toys. Describe the image as it is without any prefix, you don't need to start with 'a photo of a' or 'a picture of a'. Just describe it. Do not make up description on your own, only describe if there is a toy or a toy-like object in the picture. Example: A white stuffed bear toy with yellow sweater.";    
            Content content = ContentMaker.fromMultiModalData(
             context,
             PartMaker.fromMimeTypeAndData("image/png", readImageFile_bytes(baseline_url))
            );

            logger.warning("Context: " + context); 

              
             GenerateContentResponse response = model.generateContent(content);
             res = ResponseHandler.getText(response);
            
             //System.out.println();
          
            
          }catch(Exception e){
            System.out.println(e);
          }
          return res;
        }
        
  public static byte[] readImageFile_bytes(byte[] url) throws IOException {
    return url;
  }
   

}
