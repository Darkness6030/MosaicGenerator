package net.darkness.mosaic.controllers;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;

import static net.darkness.mosaic.MosaicApplication.*;


@RestController
public class ImageController {

    @RequestMapping("/image/show/{path}")
    public ResponseEntity<?> getImage(@PathVariable("path") String path) {
        try {
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);

            return new ResponseEntity<>(new InputStreamResource(new FileInputStream(new File(IMAGES_DIRECTORY, path))), headers, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
