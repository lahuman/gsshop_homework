package lahuman.gsshop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.TextNode;
import lahuman.gsshop.vo.MessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WebpurifyService {
    private static final Logger logger = LoggerFactory.getLogger(WebpurifyService.class);

    @Autowired
    private BlackWordService blackWordService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ObjectReader objectListStringReader;

    @Value("${webpurify.base.url}")
    private String baseUrl;

    @Value("${webpurify.check.return}")
    private String checkReturnUri;

    @Value("${webpurify.blackword.list}")
    private String blackwordListUri;

    @Value("${webpurify.blackword.remove}")
    private String blackwordRemoveUri;

    @Value("${webpurify.blackword.add}")
    private String blackwordAddUri;

    public Optional<List<String>> getBlackwordList() {
        JsonNode jsonResult = apiCall(blackwordListUri, "");
        try {
            if (jsonResult.get("rsp").get("@attributes").get("stat").asText().equals("ok")) {
                List<String> blackwordReturnList = objectListStringReader.readValue(jsonResult.get("rsp").get("word"));
                return Optional.ofNullable(blackwordReturnList.stream().distinct().collect(Collectors.toList()));
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    public boolean addBlackword(String word) {
        if(processBlackword(blackwordAddUri, word))
            return blackWordService.addBlackword(word);
        return false;
    }

    public boolean removeBlackword(String word) {
        if(processBlackword(blackwordRemoveUri, word))
            return blackWordService.removeBlackword(word);
        return false;
    }

    private boolean processBlackword(String uri, String word) {
        try {
            JsonNode jsonResult = jsonResult = apiCall(uri, "word=" + URLEncoder.encode(word, "utf-8"));
            if (jsonResult.get("rsp").get("@attributes").get("stat").asText().equals("ok")) {
                return true;
            } else {
                logger.error(jsonResult.get("err").get("@attributes").get("msg").asText());
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    private JsonNode apiCall(String uri, String parameter) {
        String apiResult = restTemplate.postForObject(baseUrl + uri, parameter, String.class);
        try {
            return objectMapper.readTree(apiResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TextNode("");
    }

    public MessageVO blackWordFilterProcess(MessageVO messageVO) {
        JsonNode jsonResult = apiCall(checkReturnUri, "text=" + messageVO.getMessage());
        if (jsonResult.get("rsp").get("expletive") != null) {
            try {
                List<String> blackwordReturnList = objectListStringReader.readValue(jsonResult.get("rsp")
                        .get("expletive"));
                Optional<List<String>> blackListOptional = Optional.ofNullable(blackwordReturnList.stream().distinct().collect(Collectors.toList()));
                blackListOptional.ifPresent(blackList -> {
                    blackList.stream().forEach(b -> {
                        messageVO.setMessage(messageVO.getMessage()
                                .replaceAll("(?i)" + b, "<font color='red'>" + b + "</font>"));
                        blackWordService.sendAddBlackword(messageVO, b);
                    });
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return messageVO;
    }


}
