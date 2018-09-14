# 작업 내역 

## 단어 설명 

금지어 : Black word 
예외어 : White word 

## 페이지
 
기본적으로 http://localhost:8080 에 접근하면 다음과 같은 2개의 링크가 표출된다. 

- Chat : 채팅으로 왼쪽에는 사용되거나 저장된 금지어가 표출되고, 오른쪽에는 대화 내용이 표시된다. 
- Blackword Manage : 금지어 관리 페이지로 추가/삭제가 가능하다 

## 특이사항 
> www.webpurify.com에서는 자체적으로 욕설에 대한 필터를 가지고 있는 듯하다. 
예를 들어 "뻑큐"라는 단어를 사용할 경우, 금지어로 지정하지 않았음에도 금지어로 확인된다. 

### 금지어 예외어 처리 프로세스
1. 대화 내용에 금지어가 있을 경우 MonogoDB에 저장한다. 
2. 저장된 금지어를 삭제할 경우 예외어에 추가로 등록한다. 
3. 금지어를 추가할 경우 예외어에서 삭제한다.


# 기술평가 과제입니다.

1.java + springboot + websocket을 사용하여 간단한 채팅 서비스를 구현해주세요.
(bower,bootstrap 적용)

2.webpurify(https://www.webpurify.com) 가입하여 API 사용 권한을 획득해주세요.

3.채팅 서비스에서 사용되는 문장에 profanity filter를 걸어주세요.

4.webpurify blacklist에 단어를 추가해주세요.

5.채팅 서비스를 사용하면서 필터링이 걸린 구문은 빨간색으로 표현해주세요.

6.필터링이 걸린 단어를 mongodb에 저장해주세요.

7.mongodb에 저장된 단어를 화면 왼쪽에 출력해주세요.

8.GitHub에 소스를 올려주세요.

* Unit Test나 TDD를 적용해주시면 더욱 좋습니다.
* java + springboot + websocket -> nodejs + express + websocket으로 하셔도 좋습니다.

감사합니다.