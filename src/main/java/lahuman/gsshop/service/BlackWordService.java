package lahuman.gsshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import lahuman.gsshop.vo.MessageVO;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class BlackWordService {

    private static final Logger logger = LoggerFactory.getLogger(BlackWordService.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongodbUri;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public boolean hasBlackWord(String blockWord) {
        try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongodbUri))) {
            MongoDatabase db = mongoClient.getDatabase("gsshop");
            if (db.getCollection("black_word").countDocuments(new Document("word", blockWord)) > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean addBlackword(MessageVO msg, String blackword) {
        try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongodbUri))) {
            MongoDatabase db = mongoClient.getDatabase("gsshop");
            db.getCollection("message").insertOne(Document.parse(objectMapper.writeValueAsString(msg))
                    .append("date", new Date()));
            return addBlackword(blackword);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    public boolean addBlackword(String blackword) {
        try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongodbUri))) {
            MongoDatabase db = mongoClient.getDatabase("gsshop");
            if (!hasBlackWord(blackword)) {
                db.getCollection("black_word").insertOne(new Document("word", blackword).append("date", new Date()));
                return true;
            }
        }
        return false;
    }

    public boolean removeBlackword(String blackword)  {
        try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongodbUri))) {
            MongoDatabase db = mongoClient.getDatabase("gsshop");
            if (hasBlackWord(blackword)) {
                db.getCollection("black_word").deleteOne(new Document("word", blackword));
                return true;
            }
        }
        return false;
    }

    public HashSet<String> getBlackWordList() {
        try (MongoClient mongoClient = new MongoClient(new MongoClientURI(mongodbUri))) {
            MongoDatabase db = mongoClient.getDatabase("gsshop");
            return new HashSet<String>(db.getCollection("black_word").find().into(new ArrayList<Document>())
                    .stream().map(d -> d.getString("word")).collect(Collectors.toList()));
        }
    }


    public void sendAddBlackword(MessageVO messageVO, String b) {
        if (addBlackword(messageVO, b)) {
            MessageVO blackWord = new MessageVO();
            blackWord.setType(MessageVO.MessageType.ADD_BLACK);
            blackWord.setMessage(b);
            blackWord.setUserName(messageVO.getUserName());
            messagingTemplate.convertAndSend("/topic/black", blackWord);
        }
    }

    public void sendRemoveBlackword(String blackword){
        MessageVO blackWord = new MessageVO();
        blackWord.setType(MessageVO.MessageType.REMOVE_BLACK);
        blackWord.setMessage(blackword);
        messagingTemplate.convertAndSend("/topic/black", blackWord);
    }
}
