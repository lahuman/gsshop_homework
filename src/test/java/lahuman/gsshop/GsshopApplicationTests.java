package lahuman.gsshop;

import lahuman.gsshop.service.WebpurifyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GsshopApplicationTests {

    @Autowired
    private WebpurifyService webpurifyService;

    @Test
    public void testBlackword() {
        final String blackword = "한글";
        assertTrue(webpurifyService.addBlackword(blackword));

        Optional<List<String>> optionalList = webpurifyService.getBlackwordList();
        assertTrue(optionalList.isPresent());
        optionalList.ifPresent(list -> {
            Optional<String> result = list.stream().filter(word -> word.equals(blackword)).findFirst();
            assertTrue(result.isPresent());
            assertThat(result.get(), is(blackword));
        });

        assertTrue(webpurifyService.removeBlackword(blackword));

    }


}
