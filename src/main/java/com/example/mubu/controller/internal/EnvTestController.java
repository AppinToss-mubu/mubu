import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnvTestController {

    @Value("${gemini.api-key}")
    private String geminiKey;

    @GetMapping("/env/test")
    public String test() {
        return geminiKey.substring(0, 5);
    }
}