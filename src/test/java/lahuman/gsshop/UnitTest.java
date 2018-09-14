package lahuman.gsshop;

import org.apache.commons.text.StringEscapeUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class UnitTest {

    @Test
    public void unicode2String(){
        Assert.assertThat(StringEscapeUtils.unescapeJson("\uc528\ubd80\ub784"), CoreMatchers.is("씨부랄"));
    }
}
