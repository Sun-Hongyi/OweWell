package owewell.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// React handles these pages in the browser, so Spring just sends back index.html
@Controller
public class SpaController {

    @GetMapping({"/group/{groupId}", "/join/{token}"})
    public String forwardToReact() {
        return "forward:/index.html";
    }
}
