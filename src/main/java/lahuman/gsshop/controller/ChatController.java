package lahuman.gsshop.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lahuman.gsshop.service.BlackWordService;
import lahuman.gsshop.service.WebpurifyService;
import lahuman.gsshop.vo.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.stream.Collectors;

@Controller
public class ChatController {

    @Autowired
    private BlackWordService blackWordService;

    @Autowired
    private WebpurifyService webpurifyService;

    @GetMapping("/blackWordList")
    public @ResponseBody MessageVO blackWordList() {
        MessageVO blackMessage = new MessageVO();
        blackMessage.setType(MessageVO.MessageType.ADD_BLACK);
        blackMessage.setMessage(blackWordService.getBlackWordList().stream().collect(Collectors.joining(",")));
        return blackMessage;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public MessageVO sendMessage(@Payload MessageVO messageVO) {

        return webpurifyService.blackWordFilterProcess(messageVO);
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public MessageVO addUser(@Payload MessageVO messageVO,
                               SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", messageVO.getUserName());
        return messageVO;
    }
}
