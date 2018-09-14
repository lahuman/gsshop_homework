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
import java.util.Arrays;
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

    @Value("${webpurify.whiteword.add}")
    private String whitewordAddUri;

    @Value("${webpurify.whiteword.remove}")
    private String whitewordRemoveUri;

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
        if (processBlackword(blackwordAddUri, whitewordRemoveUri, word))
            return blackWordService.addBlackword(word);
        return false;
    }

    public boolean removeBlackword(String word) {
        if (processBlackword(blackwordRemoveUri, whitewordAddUri, word))
            return blackWordService.removeBlackword(word);
        return false;
    }

    public boolean removeBlackwordList(String word) {
        Arrays.stream(word.split(",")).forEach(w -> {
            if (processBlackword(blackwordRemoveUri, whitewordAddUri, w))
                blackWordService.removeBlackword(w);
        });
        return true;
    }

    private boolean processBlackword(String uri, String oppositeUri,  String word) {
        try {
            apiCall(oppositeUri, "word=" + URLEncoder.encode(word, "utf-8"));
            JsonNode jsonResult = apiCall(uri, "word=" + URLEncoder.encode(word, "utf-8"));
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
        try {
            JsonNode jsonResult = apiCall(checkReturnUri, "text=" + URLEncoder.encode(messageVO.getMessage(), "utf-8"));
            if (jsonResult.get("rsp").get("expletive") != null) {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return messageVO;
    }


}
