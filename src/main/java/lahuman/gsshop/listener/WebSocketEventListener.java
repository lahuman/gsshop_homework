package lahuman.gsshop.listener;

import lahuman.gsshop.service.BlackWordService;
import lahuman.gsshop.vo.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        System.out.println("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            System.out.println("User Disconnected : " + username);

            MessageVO chatMessage = new MessageVO();
            chatMessage.setType(MessageVO.MessageType.LEAVE);
            chatMessage.setUserName(username);

            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}
